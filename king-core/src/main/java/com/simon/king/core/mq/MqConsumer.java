package com.simon.king.core.mq;

import com.simon.king.core.mq.entity.MqConsumerConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午5:46
 */
@Slf4j
public class MqConsumer {

    private MQPushConsumer consumer;

    public MqConsumer(MqConsumerConfig config){
        this.consumer = ConsumerFactory.getPushConsumer(config.getConsumerGroup(), config.getNamesrvAddr(),
            config.getTopic(), config.getSubExpression());
    }

    public void addListener(MqListener listener) {
        consumer.registerMessageListener(listener::accept);
        try {
            consumer.start();
        } catch (MQClientException e) {
            log.error("启动消费者异常， e={}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void shutdown(){
        consumer.shutdown();
    }
}
