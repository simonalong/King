package com.simon.king.server.zk;

import com.alibaba.fastjson.JSON;
import com.simon.king.server.zk.ConsistentHash.ControlRange;
import com.simon.king.server.zk.ConsistentHash.ServerNode;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import javafx.util.Pair;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * ZooKeeper 客户端服务
 * @author zhouzhenyong
 * @since 2019/1/27 下午9:00
 */
@Slf4j
public class ZooKeeperHelper implements Watcher {

    private static final String LOG_PRE = "[Tina-zk]：";
    /**
     * 根节点路径
     */
    @Setter
    private String rootPath = "/king";
    /**
     * 服务前缀路径，所有服务一律采用临时有序节点
     */
    private static final String SERVER_PRE_PATH = "/king/server";
    private ZooKeeper zk = null;
    /**
     * 当前服务的名称
     */
    private String currentServiceName;
    /**
     * 当前集群中所有服务的名字列表
     */
    private TreeSet<String> serverNameSet = new TreeSet<>();
    /**
     * 用于服务启动的同步
     */
    private CountDownLatch countDown = new CountDownLatch(1);
    /**
     * 哈希服务
     */
    private ConsistentHash hash = ConsistentHash.getInstance();
    private static volatile ZooKeeperHelper instance = null;

    /**
     * 链接的zk配置，用于重连时候用
     */
    private String connectString;
    private Integer sessionTimeout;
    /**
     * 是否已经设置为哨兵节点（只启动一次）
     */
    private Boolean haveSetGuard = false;
    /**
     * 作为哨兵节点的回调
     */
    private Runnable guardHook;

    /**
     * 私有构造器，用于单例化
     * @param aHook 用于服务的控制范围新增，其中value为新增的控制范围
     * @param rHook 用于服务的控制范围的删除，其中value为减少的控制范围
     */
    private ZooKeeperHelper(BiConsumer<String, ControlRange> aHook, BiConsumer<String, ControlRange> rHook){
        // 向哈希工具中注册回调
        hash.registerMergeHook((pair, controlRange) -> {
            if (pair.getKey().equals(currentServiceName)) {
                updateZKNodeData(pair.getKey());
                log.info(LOG_PRE + "服务" + pair.getKey() + "（合并后）接手服务"+ pair.getValue() + "（删除）的范围：" + controlRange.toString());
                CompletableFuture.runAsync(()->aHook.accept(currentServiceName, controlRange));
            }

            // 如果接手其他节点（断开的节点）任务后，最小节点变成自己了，则作为哨兵节点启动哨兵的业务
            if (!haveSetGuard && hash.isMinNode(currentServiceName)){
                runGuardHook();
            }
        });
        hash.registerSplitHook((pair, controlRange) -> {
            String key = pair.getKey();
            String value = pair.getValue();
            if (key.equals(currentServiceName) && null == value){
                // 用于服务初始化时候哈希没有数据
                updateZKNodeData(currentServiceName);
                log.info(LOG_PRE + "服务（自己）" + currentServiceName + "（新增）接手第一次接手全局范围：" + controlRange.toString());
                CompletableFuture.runAsync(()->aHook.accept(currentServiceName, controlRange));
                // 运行哨兵的业务
                runGuardHook();
            } else if(key.equals(currentServiceName)){
                // 用于服务初始化时候哈希已经有数据
                updateZKNodeData(currentServiceName);
                log.info(LOG_PRE + "服务（自己）" + pair.getKey() + "（新增）接手服务"+ pair.getValue() + "（拆分）的范围：" + controlRange.toString());
                CompletableFuture.runAsync(()->aHook.accept(currentServiceName, controlRange));
            } else if(value.equals(currentServiceName)){
                // 用于服务运行中间，其他服务的新增
                updateZKNodeData(currentServiceName);
                log.info(LOG_PRE + "服务" + pair.getKey() + "（新增）接手服务"+ pair.getValue() + "（自己被拆分）的范围：" + controlRange.toString());
                CompletableFuture.runAsync(()->rHook.accept(currentServiceName, controlRange));
            }
        });
    }

    /**
     * 单例化
     */
    public static ZooKeeperHelper getInstance(BiConsumer<String, ControlRange> aHook,
        BiConsumer<String, ControlRange> rHook) {
        if (null == instance) {
            synchronized (ZooKeeperHelper.class) {
                if (null == instance) {
                    instance = new ZooKeeperHelper(aHook, rHook);
                    return instance;
                }
            }
        }
        return instance;
    }

    /**
     * 收到来自Server的Watcher通知后的处理。
     */
    @Override
    public void process(WatchedEvent event) {
        if (event == null) {
            return;
        }
        log.info(LOG_PRE + "----------------------start-------------------");
        // 连接状态
        KeeperState keeperState = event.getState();
        // 事件类型
        EventType eventType = event.getType();

        log.info(LOG_PRE + "收到Watcher通知");
        log.info(LOG_PRE + "连接状态:\t" + keeperState.toString());
        log.info(LOG_PRE + "事件类型:\t" + eventType.toString());
        log.info(LOG_PRE + "path:\t" + event.getPath());

        if (KeeperState.SyncConnected == keeperState) {
            // 成功连接上ZK服务器
            if (EventType.None == eventType) {
                log.info(LOG_PRE + "成功连接上ZK服务器");
                countDown.countDown();
            }
            //更新子节点
            else if (EventType.NodeChildrenChanged == eventType) {
                log.info(LOG_PRE + "子节点变更");
                childrenFresh();
            }
        }
        else if (KeeperState.Disconnected == keeperState) {
            log.info(LOG_PRE + "与ZK服务器断开连接");
        }
        else if (KeeperState.Expired == keeperState) {
            log.warn(LOG_PRE + "会话失效");

            // 重新创建连接
            createConnection(connectString, sessionTimeout);
        }
        log.info(LOG_PRE + "---------------------end-------------------");
    }

    /**
     * 判断控制的id是否是当前服务管理
     * @param id 外部数据的实际数据id
     */
    public boolean idIsControlled(Long id){
        if(null == id){
            return false;
        }

        return hash.judgeBelongTo(currentServiceName, id);
    }

    /**
     * 注册哨兵节点
     * 注意：
     * 这里只有节点名字最小会自动作为哨兵节点（哨兵节点用于管理其他节点的一些相关服务），也就是只有注册名字最小的才会进行回调
     * @param guardHook 哨兵节点的回调
     */
    public void registerInitGuardHook(Runnable guardHook){
        this.guardHook = guardHook;
    }

    /**
     * 运行作为哨兵节点时候应该做的
     */
    private void runGuardHook(){
        if(null != guardHook) {
            haveSetGuard = true;
            CompletableFuture.runAsync(() -> guardHook.run());
        }
    }

    /**
     * 转换服务名为在zk中的全路径名
     * 比如服务名：server000003 -> /king/server000003
     */
    private String getAllPath(String serverName){
        return rootPath + "/" + serverName;
    }

    /**
     * 将服务在zk中的全路径转换为服务的名字
     * 比如服务名：/king/server000003 -> server000003
     */
    private String getServerName(String realServerPath){
        return realServerPath.substring("/king/".length());
    }

    /**
     * 读取指定节点数据内容
     *
     * @param serverName 服务名字
     */
    private String readData(String serverName) {
        try {
            log.info(LOG_PRE + "获取数据成功，path：" + serverName);
            return new String(this.zk.getData(getAllPath(serverName), false, null));
        } catch (KeeperException e) {
            log.info(LOG_PRE + "读取数据失败，发生KeeperException，serverName: " + serverName);
            e.printStackTrace();
            return "";
        } catch (InterruptedException e) {
            log.info(LOG_PRE + "读取数据失败，发生 InterruptedException，serverName: " + serverName);
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 更新指定节点数据内容
     *  @param serverName 节点path
     * @param data 数据内容
     */
    private void writeData(String serverName, String data) {
        try {
            this.zk.setData(getAllPath(serverName), data.getBytes(), -1);
        } catch (KeeperException e) {
            log.info(LOG_PRE + "更新数据失败，发生KeeperException，serverName: " + serverName);
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.info(LOG_PRE + "更新数据失败，发生 InterruptedException，serverName: " + serverName);
            e.printStackTrace();
        }
    }

    /**
     * 根据服务名直接更新zk节点中的数据
     * @param serverName 服务节点名字
     */
    private void updateZKNodeData(String serverName){
        if(StringUtils.isEmpty(serverName)){
            return;
        }
        writeData(serverName, JSON.toJSONString(hash.getServerNode(serverName)));
    }

    /**
     * 根据服务名获取服务中的范围数据
     */
    private ServerNode getServerDataFromZk(String serverName){
        if (!StringUtils.isEmpty(serverName)){
            String data = readData(serverName);
            if(!StringUtils.isEmpty(data)){
                return JSON.parseObject(data, ServerNode.class);
            }
        }
        return null;
    }

    /**
     * 初始化哈希数据
     */
    private void initHash(){
        List<String> allServerList = getChildrenList();
        Map<String, ServerNode> nodeMap = new TreeMap<>();
        if (!CollectionUtils.isEmpty(allServerList)){
            allServerList.forEach(s-> {
                ServerNode node = getServerDataFromZk(s);
                if (null != node) {
                    nodeMap.put(s, node);
                }
            });
        }
        hash.initServerRange(nodeMap);
        log.info(LOG_PRE + "哈希初始化完成");
    }

    /**
     * 添加子节点监控
     */
    private void addChildrenWatch(){
        try {
            zk.getChildren(rootPath, true);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取子节点数据
     */
    private List<String> getChildrenList(){
        try {
            return zk.getChildren(rootPath, false);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * 获取子节点数据
     */
    private void childrenFresh() {
        List<String> activeService = getChildrenList();
        log.info(LOG_PRE + "节点服务：" + activeService);

        TreeSet<String> serverSet = new TreeSet<>();
        if (!CollectionUtils.isEmpty(activeService)) {
            serverSet = new TreeSet<>(activeService);
        }

        // 用于其他服务有变更时候的回调
        serverListChg(serverSet);

        log.info(LOG_PRE + "当前激活的服务：" + currentServiceName);
    }

    /**
     * 创建ZK连接
     *
     * @param connectString ZK服务器地址列表
     * @param sessionTimeout Session超时时间
     */
    public void createConnection(String connectString, int sessionTimeout) {
        this.releaseConnection();
        try {
            this.connectString = connectString;
            this.sessionTimeout = sessionTimeout;

            zk = new ZooKeeper(connectString, sessionTimeout, this);
            countDown.await();
            initService();
        } catch (InterruptedException e) {
            log.info(LOG_PRE + "连接创建失败，发生 InterruptedException");
            e.printStackTrace();
        } catch (IOException e) {
            log.info(LOG_PRE + "连接创建失败，发生 IOException");
            e.printStackTrace();
        }
    }

    /**
     * 关闭ZK连接
     */
    public void releaseConnection() {
        if (null != this.zk) {
            try {
                this.zk.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建服务节点
     *
     * @param data 初始数据内容
     * @return 返回节点的实际的名字
     */
    private String createServiceNode(String data) {
        try {
            String realServicePath = this.zk.create(SERVER_PRE_PATH, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            log.info(LOG_PRE + "节点创建成功, Path: " + realServicePath + ", content: " + data);
            return getServerName(realServicePath);
        } catch (KeeperException e) {
            log.info(LOG_PRE + "节点创建失败，发生KeeperException");
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.info(LOG_PRE + "节点创建失败，发生 InterruptedException");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建节点
     *
     * @param data root节点内容
     */
    private boolean createRootNode(String data) {
        try {
            if (null == this.zk.exists(rootPath, false)) {
                String realPath = zk.create(rootPath, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.info(LOG_PRE + "节点创建成功, Path: " + realPath);
            } else {
                log.info(LOG_PRE + "节点" + rootPath + "已经存在，则初始化内存中的哈希数据");

                // 初始化哈希数据
                initHash();

                // 更新本地服务列表
                serverNameSet = hash.getServerNameSet();
            }
        } catch (KeeperException e) {
            log.info(LOG_PRE + "节点创建失败，发生KeeperException");
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.info(LOG_PRE + "节点创建失败，发生 InterruptedException");
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 在服务节点发生变更的时候，更新本地哈希数据和任务调度
     * @param newServerSet 新的服务列表
     */
    private void serverListChg(TreeSet<String> newServerSet){
        log.info(LOG_PRE + "本地服务序列："+serverNameSet);
        log.info(LOG_PRE + "所有子节点："+newServerSet);
        Pair<String, String> addDeletePair = getAddAndDeleteSet(serverNameSet, newServerSet);

        log.info(LOG_PRE + "节点的变更：" + addDeletePair);
        String key = addDeletePair.getKey();
        String value = addDeletePair.getValue();

        // 对于是自己的则不让其在这里进行注册
        if (null != key && !key.equals(currentServiceName) || null != value && !value.equals(currentServiceName)) {
            hash.registerServer(addDeletePair.getKey());
            hash.deleteServer(addDeletePair.getValue());
        }

        // 更新本地用户服务集合
        serverNameSet = hash.getServerNameSet();

        addChildrenWatch();
    }

    /**
     * 获取新旧服务名字对比，查看哪些是新增的，哪些是删除的
     * @param oldServerSet 旧的服务名字列表
     * @param newServerSet 新的服务名字列表
     * @return 删除的服务集合和新增的服务集合
     */
    private Pair<String, String> getAddAndDeleteSet(TreeSet<String> oldServerSet, TreeSet<String> newServerSet){
        String addStr = null;
        String removeStr = null;
        if (null != oldServerSet && null != newServerSet){
            if(newServerSet.containsAll(oldServerSet)){
                addStr = newServerSet.stream().filter(s->!oldServerSet.contains(s)).findFirst().get();
            } else if(oldServerSet.containsAll(newServerSet)){
                removeStr = oldServerSet.stream().filter(s->!newServerSet.contains(s)).findFirst().get();
            }
        } else {
            if (null != newServerSet) {
                addStr = newServerSet.first();
            }

            if(null != oldServerSet){
                removeStr = newServerSet.first();
            }
        }
        return new Pair<>(addStr, removeStr);
    }

    /**
     * 首先创建父节点，然后获取父节点下面的所有数据并创建自己的服务节点，并监控所有的子节点
     */
    private void initService() {
        // 创建root节点
        if (createRootNode("")) {
            // 添加子节点监控
            addChildrenWatch();

            // 创建自己的服务节点，节点数据暂时没有
            currentServiceName = createServiceNode("");
            if (!StringUtils.isEmpty(currentServiceName)) {
                // 将服务注册到哈希服务中
                hash.registerServer(currentServiceName);

                // 更新本地服务列表
                serverNameSet = hash.getServerNameSet();
            }
        }
    }
}
