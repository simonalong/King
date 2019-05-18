package com.simon.king.server.listener;

import com.alibaba.fastjson.JSON;
import com.simon.king.common.util.Idempotency;
import com.simon.king.core.mq.TaskChgMsg;
import com.simon.king.server.service.TaskExtService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouzhenyong
 * @since 2019/2/14 下午1:56
 */
@Slf4j
@Service
public class TaskChgReceiver {

    @Autowired
    private TaskExtService taskExtService;
    private TaskChgReceiver(){}

    @RabbitListener(queues = "task_chg")
    public void process(String message) {
        TaskChgMsg taskChgMsg = JSON.parseObject(message, TaskChgMsg.class);
        log.info("consumer 接收消息: {}", JSON.toJSONString(taskChgMsg));
        if(null != taskChgMsg) {
            // 单机消息幂等设置
            if (Idempotency.getInstance().setExpire(10, TimeUnit.SECONDS).contain(taskChgMsg)) {
                // 10s内如果有重复消息发送过来，则返回已处理
                return;
            }
            taskExtService.accept(taskChgMsg);
        }
    }
}
