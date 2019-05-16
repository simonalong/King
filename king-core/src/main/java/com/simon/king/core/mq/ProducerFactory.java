package com.simon.king.core.mq;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MQProducer;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午5:53
 */
@UtilityClass
public class ProducerFactory {

    private static Map<String, MQProducer> producerMap = new HashMap<>(12);

    /**
     * 获取生产者
     * @param producerGroup 生产者组
     * @param namesrvAddr 服务名地址
     * @return 获取生产者实例
     */
    public MQProducer getProducer(final String producerGroup, String namesrvAddr) {
        return producerMap.computeIfAbsent(producerGroup, k -> {
            DefaultMQProducer producer = new DefaultMQProducer(k);
            producer.setNamesrvAddr(namesrvAddr);
            return producer;
        });
    }

    /**
     * 异步情况下设置的获取生产者
     * @param producerGroup 生产者组
     * @param namesrvAddr 服务名地址
     * @param retryTimesWhenSendAsyncFailed 异步模式下失败后重试的次数
     * @return 获取生产者实例
     */
    public MQProducer getProducer(final String producerGroup, String namesrvAddr, final int retryTimesWhenSendAsyncFailed) {
        return producerMap.computeIfAbsent(producerGroup, k -> {
            DefaultMQProducer producer = new DefaultMQProducer(k);
            producer.setNamesrvAddr(namesrvAddr);
            return producer;
        });
    }
}
