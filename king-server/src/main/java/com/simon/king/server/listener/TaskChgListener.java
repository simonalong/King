package com.simon.king.server.listener;

import com.alibaba.fastjson.JSON;
import com.simon.king.common.util.Idempotency;
import com.simon.king.core.meta.ConfigItemEntity;
import com.simon.king.core.mq.MqListener;
import com.simon.king.core.mq.TaskChgMsg;
import com.simon.king.core.mq.entity.MqMessage;
import com.simon.king.server.service.TaskExtService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouzhenyong
 * @since 2019/2/14 下午1:56
 */
@Slf4j
@Service
public class TaskChgListener implements MqListener {

    @Autowired
    private TaskExtService taskExtService;
    private TaskChgListener(){}

    @Override
    public ConsumeConcurrentlyStatus accept(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        MessageExt messageExt = msgs.get(0);
        MqMessage message = MqMessage.build(messageExt);
        if (null == message) {
            log.warn("consumer 接收消息为空");
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        log.info("consumer 接收消息: {}", JSON.toJSONString(message));

        try {
            String tag = message.getTag();
            switch (tag){
                case "task_chg":{
                    log.debug("任务变更");
                    TaskChgMsg taskChgMsg = TaskChgMsg.build(message);
                    // 单机消息幂等设置
                    if (Idempotency.getInstance().setExpire(10, TimeUnit.SECONDS).contain(taskChgMsg)) {
                        // 10s内如果有重复消息发送过来，则返回已处理
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    if (null != taskChgMsg) {
                        taskExtService.accept(taskChgMsg);
                    }
                }break;
                default: break;
            }

            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        } catch (Exception e){
            log.error("消费异常：{}", e);
        }
        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
    }
}
