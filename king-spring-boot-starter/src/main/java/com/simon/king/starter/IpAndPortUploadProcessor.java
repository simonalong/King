package com.simon.king.starter;

import com.alibaba.fastjson.JSON;
import com.simon.king.common.KingConstant;
import com.simon.king.common.util.ZookeeperClient;
import com.simon.king.core.service.ZkParserService;
import com.simon.neo.NeoMap;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
    private String servePort;

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
        this.servePort = String.valueOf(event.getWebServer().getPort());

        // spring启动完成之后创建分布式任务调度中的命名空间
        ZookeeperClient zkClient = ZookeeperClient.getInstance();
        zkClient.connect(zkParserService.getZkAddress()).addRoot(KingConstant.ROOT_PATH);
        String ipAndPortJson = JSON.toJSONString(NeoMap.of("ip", getIp(), "port", getServePort()));

        String nameSpace = kingNameProperties.getNamespace();
        zkClient.addPersistentNode(KingConstant.CLIENT_PATH + "/" + nameSpace);

        String node = zkClient.addEphemeralSeqNode(KingConstant.CLIENT_PATH + "/" + nameSpace + "/c_", ipAndPortJson);
        log.info("进程启动完成：注册命名空间" + nameSpace + "到分布式任务调度中心完成, 当前节点为" + node);
    }
}