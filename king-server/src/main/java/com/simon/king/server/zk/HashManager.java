package com.simon.king.server.zk;

import com.alibaba.fastjson.JSON;
import com.simon.king.common.KingConstant;
import com.simon.king.common.util.ZookeeperClient;
import com.simon.king.server.zk.ConsistentHashUtil.ControlRange;
import com.simon.king.server.zk.ConsistentHashUtil.ServerNode;
import com.simon.neo.NeoMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.util.Pair;
import javax.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 一致性哈希服务管理器
 *
 * 通过zk客户端和一致性哈希算法结合实现一致性哈希的业务管理
 *
 * @author zhouzhenyong
 * @since 2019/5/19 下午6:05
 */
@Slf4j
public class HashManager {

    private static final String LOG_PRE = "[king_hash]：";

    /**
     * 当前服务的名称
     */
    private String currentServiceName;
    /**
     * 一致性哈希工具
     */
    private ConsistentHashUtil hash = ConsistentHashUtil.getInstance();

    @Getter
    private ZookeeperClient zkClient = ZookeeperClient.getInstance();

    private static HashManager instance = new HashManager();

    public static HashManager getInstance(){
        return instance;
    }

    /**
     * 作为哨兵节点的回调
     */
    private Runnable guardHook;

    /**
     * 命名空间中的节点新增回调
     */
    private Consumer<NeoMap> addNodeCallback;
    /**
     * 命名空间中的节点删除回调
     */
    private Consumer<NeoMap> rmrNodeCallback;

    /**
     * 初始化zk的链接，并创建两个永久节点：服务端的永久节点/king/server和客户端的永久节点/king/client
     * @param zkAddress zk的地址
     * @return 返回一致性哈希服务类
     */
    public HashManager initZookeeper(String zkAddress) {
        zkClient.connect(zkAddress).addRoot(KingConstant.ROOT_PATH);
        zkClient.addPersistentNode(KingConstant.SERVER_PATH);
        zkClient.addPersistentNode(KingConstant.CLIENT_PATH);

        // 添加我们关注的路径及其所有的子节点
        zkClient.addWatchPath(KingConstant.SERVER_PATH, KingConstant.CLIENT_PATH);

        initHash(KingConstant.SERVER_PATH);

        initService();
        return this;
    }

    /**
     * 注册一致性服务的扩充和移除
     * @param aHook 用于服务的控制范围新增，其中value为新增的控制范围
     * @param rHook 用于服务的控制范围的删除，其中value为减少的控制范围
     */
    public HashManager registerAddAndRemove(BiConsumer<String, ControlRange> aHook, BiConsumer<String, ControlRange> rHook){
        // 向哈希工具中注册回调
        hash.registerMergeHook((pair, controlRange) -> {
            if (pair.getKey().equals(currentServiceName)) {
                updateZKNodeData(pair.getKey());
                log.info(LOG_PRE + "服务" + pair.getKey() + "（合并后）接手服务"+ pair.getValue() + "（删除）的范围：" + controlRange.toString());
                CompletableFuture.runAsync(()->aHook.accept(currentServiceName, controlRange));
            }

            // 如果接手其他节点（断开的节点）任务后，最小节点变成自己了，则作为哨兵节点启动哨兵的业务
            if (null != guardHook && hash.isMinNode(currentServiceName)){
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
        return instance;
    }

    /**
     * 注册客户端的节点新增时候的回调
     */
    public void registerClientAddNodeCall(Consumer<NeoMap> consumer){
        this.addNodeCallback = consumer;
    }

    /**
     * 注册客户端的节点删除时候的回调
     */
    public void registerClientRmrNodeCall(Consumer<NeoMap> consumer){
        this.rmrNodeCallback = consumer;
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
    public void registerGuardHook(Runnable guardHook){
        this.guardHook = guardHook;
    }

    @PreDestroy
    public void close(){
        zkClient.close();
    }

    /**
     * 初始化服务，主要一下三个方面
     * 1.创建本地服务节点
     * 2.注册zk的节点增删回调
     * 3.刷新本地节点缓存
     */
    private void initService() {
        // 创建当前节点
        this.currentServiceName = zkClient.addEphemeralSeqNode(KingConstant.SERVER_PATH + "/s_");
        if (!StringUtils.isEmpty(currentServiceName)) {
            // 将服务注册到哈希服务中
            hash.registerServer(currentServiceName);
        }

        // 添加节点的删除和新增回调
        zkClient.registerNodeAddAndDeleteHook((path, pair) -> {
            if (path.startsWith(KingConstant.SERVER_PATH)) {
                String key = pair.getKey();
                String value = pair.getValue();
                log.info(LOG_PRE + "server端：节点变更 ==> 新增的节点" + key + ", 删除的节点：" + value);

                // 对于是自己的则不让其在这里进行注册
                if (null != key && !key.equals(currentServiceName)) {
                    hash.registerServer(key);
                }

                if (null != value && !value.equals(currentServiceName)) {
                    hash.deleteServer(value);
                }
            } else if (path.startsWith(KingConstant.CLIENT_PATH)) {
                Pair<String, String> addAndDelete = zkClient.getAddAndDeleteSet(path);
                String addNode = addAndDelete.getKey();
                String rmrNode = addAndDelete.getValue();
                log.info(LOG_PRE + "client端：节点变更 ==> path = " + path + ", 新增的节点" + addNode + ", 删除的节点：" + rmrNode);

                // 对于新增的，若等于客户端的root路径，则为新增命名空间，需要对新增的添加监控和刷新新节点下面的子节点
                if(path.equals(KingConstant.CLIENT_PATH)){
                    if (null != addNode) {
                        zkClient.refreshNode(addNode);
                        zkClient.addWatchChildren(addNode);

                        zkClient.getChildrenPathList(addNode).forEach(r->{
                            if (null != addNodeCallback) {
                                addNodeCallback.accept(NeoMap.of("path", addNode, "node", r, "data", zkClient.readData(r)));
                            }
                        });
                    }

                    // 这里删除只是为了以后对命名空间内部没有数据时候进行删除用，这里暂时遗留
                    if(null != rmrNode){
                        zkClient.rmrNode(rmrNode);
                    }
                }else {
                    // 对于旧的命名空间，这个时候变更，应该是空间内的节点新增和删除
                    if (null != addNodeCallback && null != addNode) {
                        addNodeCallback.accept(NeoMap.of("path", path, "node", addNode, "data", zkClient.readData(addNode)));
                    }

                    if (null != rmrNodeCallback && null != rmrNode) {
                        rmrNodeCallback.accept(NeoMap.of("path", path, "node", rmrNode));
                    }
                }
            }
        });

        // 更新本地用户服务集合
        zkClient.refreshNode(KingConstant.SERVER_PATH, hash.getServerNameSet());
    }

    /**
     * 初始化哈希数据
     */
    private void initHash(String path){
        List<String> allServerList = zkClient.getChildrenPathList(path);
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
     * 根据服务名获取服务中的范围数据
     */
    private ServerNode getServerDataFromZk(String serverName){
        if (!StringUtils.isEmpty(serverName)){
            String data = zkClient.readData(serverName);
            if(!StringUtils.isEmpty(data)){
                return JSON.parseObject(data, ServerNode.class);
            }
        }
        return null;
    }


    /**
     * 运行作为哨兵节点时候应该做的
     */
    private void runGuardHook(){
        if(null != guardHook) {
            CompletableFuture.runAsync(() -> guardHook.run());
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
     * 转换服务名为在zk中的全路径名
     * 比如服务名：s_000003 -> /king/server/s_000003
     */
    private String getAllPath(String serverName){
        return KingConstant.SERVER_PATH + "/" + serverName;
    }

    /**
     * 将服务在zk中的全路径转换为服务的名字
     * 比如服务名：/king/server/s_000003 -> s_000003
     */
    private String getServerName(String realServerPath){
        return realServerPath.substring((KingConstant.SERVER_PATH + "/s_").length());
    }

    /**
     * 更新指定节点数据内容
     *  @param serverName 节点path
     * @param data 数据内容
     */
    private void writeData(String serverName, String data) {
        this.zkClient.writeNodeData(serverName, data);
    }
}
