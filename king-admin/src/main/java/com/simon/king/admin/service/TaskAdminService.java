package com.simon.king.admin.service;

import com.alibaba.fastjson.JSON;
import com.simon.king.common.KingConstant;
import com.simon.king.core.dao.TaskDao;
import com.simon.king.core.meta.StatusEnum;
import com.simon.king.core.meta.TaskChgEnum;
import com.simon.king.core.meta.TaskEntity;
import com.simon.king.core.mq.TaskChgMsg;
import com.simon.king.core.service.TaskService;
import com.simon.neo.NeoMap;
import com.simon.neo.NeoMap.NamingChg;
import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouzhenyong
 * @since 2019/5/16 上午11:34
 */
@Slf4j
@Service
public class TaskAdminService extends TaskService {

    @Autowired
    private TaskDao taskDao;
    @Autowired
    private AmqpTemplate rabbitTemplate;

    @Override
    public NeoMap insert(NeoMap record) {
        NeoMap result = super.insert(record);
        if (!NeoMap.isEmpty(result)) {
            if (judgeEnable(record)) {
                sendTaskActive(record);
            }
        }
        return result;
    }

    @Override
    public Integer delete(Long id) {
        NeoMap record = one(id);
        Integer result = super.delete(id);
        if (1 == result) {
            sendTaskDelete(record);
        }
        return result;
    }

    @Override
    public NeoMap update(NeoMap record) {
        NeoMap recordBefore = taskDao.oneIgnoreStatus(record.getLong("id"));
        NeoMap result = super.update(record);
        if (!NeoMap.isEmpty(result)) {
            if (judgeEnable(recordBefore, record)) {
                // 任务启用
                sendTaskActive(record);
            } else if (judgeDisable(recordBefore, record)) {
                // 任务禁用
                sendTaskDeActive(record);
            } else if(judgeEnable(recordBefore) && judgeEnable(record)){
                // 任务激活状态下其他配置的一些修改，需要重新激活
                sendTaskReload(record);
            }
        }
        return result;
    }

    /**
     * 判断更新是否是禁用当前的任务
     */
    private boolean judgeDisable(NeoMap recordBefore, NeoMap recordAfter) {
        if (!NeoMap.isEmpty(recordBefore) && !NeoMap.isEmpty(recordAfter)) {
            return recordBefore.getStr("status").equals(StatusEnum.Y.getValue())
                && recordAfter.getStr("status").equals(StatusEnum.N.getValue());
        }
        return false;
    }

    /**
     * 判断更新是否是启用当前的任务
     */
    private boolean judgeEnable(NeoMap recordBefore, NeoMap recordAfter) {
        if (!NeoMap.isEmpty(recordBefore) && !NeoMap.isEmpty(recordAfter)) {
            return recordBefore.getStr("status").equals(StatusEnum.N.getValue())
                && recordAfter.getStr("status").equals(StatusEnum.Y.getValue());
        }
        return false;
    }

    /**
     * 判断当前任务是否是激活状态
     */
    private boolean judgeEnable(NeoMap record){
        if(!NeoMap.isEmpty(record)){
            return record.getStr("status").equals(StatusEnum.Y.getValue());
        }
        return false;
    }

    private void sendMsg(TaskChgMsg taskChgMsg){
        this.rabbitTemplate.convertAndSend("task_chg", JSON.toJSONString(taskChgMsg));
    }

    private void sendTaskActive(NeoMap record){
        sendMsg(new TaskChgMsg().setAction(TaskChgEnum.ACTIVE).setTaskData(record.as(TaskEntity.class, NamingChg.UNDERLINE)));
    }

    private void sendTaskDeActive(NeoMap record){
        sendMsg(new TaskChgMsg().setAction(TaskChgEnum.DE_ACTIVE).setTaskData(record.as(TaskEntity.class, NamingChg.UNDERLINE)));
    }

    private void sendTaskReload(NeoMap record){
        sendMsg(new TaskChgMsg().setAction(TaskChgEnum.RELOAD).setTaskData(record.as(TaskEntity.class, NamingChg.UNDERLINE)));
    }

    private void sendTaskDelete(NeoMap record){
        sendMsg(new TaskChgMsg().setAction(TaskChgEnum.DELETE).setTaskData(record.as(TaskEntity.class, NamingChg.UNDERLINE)));
    }
}
