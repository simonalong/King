package com.simon.king.core.mq;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MQPullConsumer;
import org.apache.rocketmq.client.consumer.MQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;

/**
 * @author zhouzhenyong
 * @since 2019/5/13 下午6:16
 */
@UtilityClass
public class ConsumerFactory {

    private Map<String, MQPullConsumer> pullConsumerMap = new HashMap<>(12);
    private Map<String, MQPushConsumer> pushConsumerMap = new HashMap<>(12);

    /**
     * 获取消费者
     * @param consumerGroup 消费组
     * @param namesrvAddr 命名空间
     * @param topic 话题
     * @param subExpression 子表达式
     * @return 消费者实例
     */
    public MQPushConsumer getPushConsumer(String consumerGroup, String namesrvAddr, String topic, String subExpression){
        return pushConsumerMap.computeIfAbsent(consumerGroup, k -> {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(k);
            consumer.setNamesrvAddr(namesrvAddr);
            try {
                if (null == subExpression) {
                    consumer.subscribe(topic, "*");
                } else {
                    consumer.subscribe(topic, subExpression);
                }
            } catch (MQClientException e) {
                e.printStackTrace();
            }
            return consumer;
        });
    }
}
