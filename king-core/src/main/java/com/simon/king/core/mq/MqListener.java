package com.simon.king.core.mq;

import java.util.List;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author zhouzhenyong
 * @since 2019/5/14 上午9:44
 */
public interface MqListener {

    ConsumeConcurrentlyStatus accept(List<MessageExt> msgs, ConsumeConcurrentlyContext context);
}
