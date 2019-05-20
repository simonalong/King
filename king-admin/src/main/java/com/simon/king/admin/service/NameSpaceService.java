package com.simon.king.admin.service;

import com.alibaba.fastjson.JSON;
import com.simon.king.groovy.NameSpaceInterface;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouzhenyong
 * @since 2019/5/20 下午6:21
 */
@Slf4j
@Service
public class NameSpaceService implements NameSpaceInterface {

    @Autowired
    private AmqpTemplate rabbitTemplate;
    private CountDownLatch latch = new CountDownLatch(1);
    private String namespaceToIp = null;

    /**
     * 调用这里的主要是用于界面测试，因此没必要做rpc，做一个简单的消息发送和接收即可
     * @param namespace 命名空间
     */
    @Override
    public String getIpAndPort(String namespace) {
        this.rabbitTemplate.convertAndSend("namespace_ip_send", namespace);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return namespaceToIp;
    }

    @RabbitListener(queues = "namespace_ip_receive")
    public void namespaceIp(String message) {
        log.info("consumer 接收消息: {}", message);
        if(null != message) {
            namespaceToIp = message;
            latch.countDown();
        }
    }
}
