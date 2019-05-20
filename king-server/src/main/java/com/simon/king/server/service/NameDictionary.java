package com.simon.king.server.service;

import com.simon.king.common.KingConstant;
import com.simon.king.common.util.ZookeeperClient;
import com.simon.king.server.zk.HashManager;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * 命名空间的字典，用于命名空间和内部节点数据的关联映射
 *
 * @author zhouzhenyong
 * @since 2019/5/20 上午11:36
 */
@Slf4j
@Service
public class NameDictionary implements ApplicationListener<ContextRefreshedEvent> {

    private static final String LOG_PRE = "[king_dictionary]：";
    /**
     * key为path，比如：/king/client/name1, value：为一个map，里面的key为节点名字，比如:c_0000000003, value为对应节点中的数据
     */
    private Map<String, List<NodeAndData>> nodeNamePathMap = new HashMap<>();
    /**
     * 一致性哈希服务的hash管理器
     */
    private HashManager manager = HashManager.getInstance();

    /**
     * 对应命名空间中添加对应的节点名字
     * @param path zk中的路径，比如：/king/client/name1
     * @param nodeName 对应节点的名字，比如：c_0000000003
     * @param data 对应节点中的数据
     */
    public void add(String path, String nodeName, String data) {
        nodeNamePathMap.compute(path, (k, v) -> {
            if (null == v) {
                List<NodeAndData> dataList = new LinkedList<>();
                dataList.add(new NodeAndData().setNodeName(nodeName).setIpAndPortJson(data));
                return dataList;
            } else {
                v.add(new NodeAndData().setNodeName(nodeName).setIpAndPortJson(data));
                return v;
            }
        });
    }

    public void remove(String path, String nodeName) {
        if (nodeNamePathMap.containsKey(path)){
            List<NodeAndData> dataList = nodeNamePathMap.get(path);
            dataList = dataList.stream().filter(r->!r.getNodeName().equals(nodeName)).collect(Collectors.toList());
            nodeNamePathMap.put(path, dataList);
        }
    }

    /**
     * 根据命名空间获取对应的
     * @param namespace 命名空间，其实也就是/king/client/name1中的name1
     * @return 返回该命名空间中的所有的数据, key为节点名字，value为对应的数据
     */
    public List<String> getNodeList(String namespace) {
        String pathKey = KingConstant.CLIENT_PATH + "/" + namespace;
        if (nodeNamePathMap.containsKey(pathKey)) {
            return nodeNamePathMap.get(pathKey).stream().map(NodeAndData::getIpAndPortJson).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 初始化命名空间中的诸多数据
     */
    private void initMap() {
        ZookeeperClient zkClient = manager.getZkClient();
        List<String> namespaceList = zkClient.getChildrenPathList(KingConstant.CLIENT_PATH);
        namespaceList.forEach(namespace -> zkClient.getChildrenPathList(namespace)
            .forEach(node -> add(namespace, node, zkClient.readData(node))));
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 首先初始化命名空间中的节点数据
        initMap();

        // 注册客户端的节点新增和删除回调
        manager.registerClientAddNodeCall(d-> {
            log.info(LOG_PRE + "接收到新的名称注册：msg = " + d.toString());
            add(d.getStr("path"), d.getStr("node"), d.getStr("data"));

            log.info("现在全部的数据" + nodeNamePathMap.toString());
        });
        manager.registerClientRmrNodeCall(d-> {
            log.info(LOG_PRE + "接收到新的节点删除：msg = " + d.toString());
            remove(d.getStr("path"), d.getStr("node"));

            log.info("现在全部的数据" + nodeNamePathMap.toString());
        });

        log.info("现在全部的数据" + nodeNamePathMap.toString());
    }


    @Data
    @Accessors(chain = true)
    public class NodeAndData{

        private String nodeName;
        private String ipAndPortJson;
    }
}
