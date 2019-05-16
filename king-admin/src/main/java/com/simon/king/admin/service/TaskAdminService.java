package com.simon.king.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouzhenyong
 * @since 2019/5/16 上午11:34
 */
@Service
public class TaskAdminService extends TaskService {

    @Autowired
    private ProducerBean tinaProducer;
    @Autowired
    private OnsProperties onsProperties;
    @Autowired
    private UUIDService uuidService;
    @Autowired
    private TaskDao taskDao;

    @Override
    public Integer insert(Record record) {
        Integer result = super.insert(record);
        if (1 == result) {
            if (judgeEnable(record)) {
                sendTaskActive(record);
            }
        }
        return result;
    }

    @Override
    public Integer delete(Long id) {
        Record record = one(id);
        Integer result = super.delete(id);
        if (1 == result) {
            sendTaskDelete(record);
        }
        return result;
    }

    @Override
    public Integer update(Record record) {
        Record recordBefore = taskDao.oneIgnoreStatus(record.getLong("id"));
        Integer result = super.update(record);
        if (1 == result) {
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
    private boolean judgeDisable(Record recordBefore, Record recordAfter) {
        if (!RecordUtils.isEmpty(recordBefore) && !RecordUtils.isEmpty(recordAfter)) {
            return recordBefore.getStr("status").equals(StatusEnum.YES.getName())
                && recordAfter.getStr("status").equals(StatusEnum.NO.getName());
        }
        return false;
    }

    /**
     * 判断更新是否是启用当前的任务
     */
    private boolean judgeEnable(Record recordBefore, Record recordAfter) {
        if (!RecordUtils.isEmpty(recordBefore) && !RecordUtils.isEmpty(recordAfter)) {
            return recordBefore.getStr("status").equals(StatusEnum.NO.getName())
                && recordAfter.getStr("status").equals(StatusEnum.YES.getName());
        }
        return false;
    }

    /**
     * 判断当前任务是否是激活状态
     */
    private boolean judgeEnable(Record record){
        if(!RecordUtils.isEmpty(record)){
            return record.getStr("status").equals(StatusEnum.YES.getName());
        }
        return false;
    }

    private void sendMsg(TaskChgEnum taskChgEnum, Record record){
        TaskChgMsg chgMsg = new TaskChgMsg();
        chgMsg.setAction(taskChgEnum);
        // 删除情况下有点特殊，因为DB中已经没有改数据了，因此不能再通过ID进行获取数据
        if (taskChgEnum.equals(TaskChgEnum.DELETE)) {
            chgMsg.setTaskData(record);
        } else{
            chgMsg.setTaskData(String.valueOf(record.getLong("id")));
        }
        chgMsg.setId(String.valueOf(uuidService.getUUID()));
        chgMsg.setTag(TinaConstant.TASK_CHG_TAG);
        chgMsg.setBusinessType(TinaConstant.APP_NAME);

        SendResult result = tinaProducer.send(OnsUtil.buildOnsMessage(onsProperties.getTopic(), chgMsg));

        log.info("任务状态变更，发送mq: msg={}, result={}", JSON.toJSONString(chgMsg), result);
    }

    private void sendTaskActive(Record record){
        sendMsg(TaskChgEnum.ACTIVE, record);
    }

    private void sendTaskDeActive(Record record){
        sendMsg(TaskChgEnum.DE_ACTIVE, record);
    }

    private void sendTaskReload(Record record){
        sendMsg(TaskChgEnum.RELOAD, record);
    }

    private void sendTaskDelete(Record record){
        sendMsg(TaskChgEnum.DELETE, record);
    }
}
