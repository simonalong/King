package com.simon.king.core.service;

import com.alibaba.fastjson.JSON;
import com.simon.king.core.dao.TaskDao;
import com.simon.king.core.meta.StatusEnum;
import com.simon.king.core.meta.TaskEnum;
import com.simon.neo.Neo;
import com.simon.neo.NeoMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 任务调度服务
 *
 * @author zhouzhenyong
 * @since 2019/1/14 上午11:03
 */
@Service
@Slf4j
public class TaskService extends BaseService {

    @Autowired
    private TaskDao taskDao;
    @Autowired
    private Neo neo;
    @Autowired
    private ParserService parserService;

    @Override
    protected Neo getNeo() {
        return neo;
    }

    @Override
    protected String getTableName() {
        return "t_task";
    }

    public NeoMap one(String taskGroup, String taskName){
        return taskDao.one(taskGroup, taskName);
    }

    public List<String> getCodeList(){
        return getNeo().values(getTableName(), "task_group");
    }

    public NeoMap disable(Long taskId){
        if(null != taskId){
            NeoMap taskRecord = taskDao.oneIgnoreStatus(taskId);
            taskRecord.put("status", StatusEnum.N.getValue());
            return update(taskRecord);
        }
        return NeoMap.of();
    }

    public NeoMap enable(Long taskId){
        if(null != taskId){
            NeoMap taskRecord = taskDao.oneIgnoreStatus(taskId);
            taskRecord.put("status", StatusEnum.Y.getValue());
            return update(taskRecord);
        }
        return NeoMap.of();
    }

    /**
     * 手动运行一次，不关心结果
     * @param record 两个key：type, data
     */
    public Object handRun(NeoMap record){
        String type = record.getStr("type");
        if (type.equals(TaskEnum.GROOVY.getName())){
            String data = record.getStr("data");
            return parserService.parse(data, "ok");
        }
        return null;
    }

    /**
     * 启动运行，关心结果
     * @param record 两个key：type, data
     */
    public String run(NeoMap record){
        String type = record.getStr("type");
        if (type.equals(TaskEnum.GROOVY.getName())){
            String data = record.getStr("data");
            Object result = parserService.parseAndResult(data, "ok");
            return JSON.toJSONString(result);
        }
        return null;
    }
}