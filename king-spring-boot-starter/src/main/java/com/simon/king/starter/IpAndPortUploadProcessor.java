package com.simon.king.starter;

import com.alibaba.fastjson.JSON;
import com.simon.king.common.KingConstant;
import com.simon.king.common.util.ZookeeperClient;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author zhouzhenyong
 * @since 2019/5/20 下午3:10
 */
@Slf4j
@Getter
@Component
@EnableConfigurationProperties(KingNameProperties.class)
public class IpAndPortUploadProcessor implements ApplicationListener<WebServerInitializedEvent> {

    @Autowired
    private ZkParserService zkParserService;
    @Autowired
    private KingNameProperties kingNameProperties;

    public String getIp(){
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "ip get error";
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        // spring启动完成之后创建分布式任务调度中的命名空间
        ZookeeperClient zkClient = ZookeeperClient.getInstance();
        zkClient.connect(zkParserService.getZkAddress()).addRoot(KingConstant.ROOT_PATH);

        String nameSpace = kingNameProperties.getNamespace();
        zkClient.addPersistentNode(KingConstant.CLIENT_PATH + "/" + nameSpace);

        Map<String, Object> dataMap = new HashMap<>(2);
        dataMap.put("ip", getIp());
        dataMap.put("port", String.valueOf(event.getWebServer().getPort()));
        String node = zkClient.addEphemeralSeqNode(KingConstant.CLIENT_PATH + "/" + nameSpace + "/c_", JSON.toJSONString(dataMap));
        log.info("进程启动完成：注册命名空间" + nameSpace + "到分布式任务调度中心完成, 当前节点为" + node);
    }
}