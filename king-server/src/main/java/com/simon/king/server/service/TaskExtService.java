package com.simon.king.server.service;

import com.simon.king.core.dao.TaskDao;
import com.simon.king.core.meta.TaskChgEnum;
import com.simon.king.core.meta.TimingTypeEnum;
import com.simon.king.core.mq.TaskChgMsg;
import com.simon.king.core.service.TaskService;
import com.simon.king.server.cache.TaskCacheHelper;
import com.simon.king.server.task.Task;
import com.simon.king.server.task.TaskManager;
import com.simon.king.server.task.TaskScheduler;
import com.simon.neo.NeoMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author zhouzhenyong
 * @since 2019/1/14 上午11:03
 */
@Service
@Slf4j
public class TaskExtService extends TaskService {

    @Autowired
    private TaskDao taskDao;
    @Autowired
    private TaskScheduler taskScheduler;
    @Autowired
    private TaskCacheHelper taskCacheHelper;
    @Autowired
    private TaskManager taskManager;

    private static final String TIMING_TYPE_COLUMN = "timing_type";

    public void accept(TaskChgMsg taskChgMsg) {
        if (null != taskChgMsg) {
            NeoMap record = getRecordFromMsg(taskChgMsg);

            // 对于非当前任务处理的数据，则不进行处理
            if (!taskManager.taskBelongToSelf(record)) {
                return;
            }
            switch (taskChgMsg.getAction()) {
                case ACTIVE: {
                    log.info("激活任务：taskId={}", record.getLong("id"));
                    taskScheduler.submit(Task.build(record));
                }
                break;
                case DELETE:
                case DE_ACTIVE: {
                    log.info("禁用任务：taskId={}", record.getLong("id"));
                    taskScheduler.shutdown(Task.build(record));
                    taskCacheHelper.delete(record);
                }
                break;
                case RELOAD: {
                    log.info("重新加载任务：taskId={}", record.getLong("id"));
                    // 先删除掉当前的
                    taskScheduler.shutdown(Task.build(record));
                    taskCacheHelper.delete(record);

                    // 将任务重新加载
                    NeoMap newStatusTask = taskDao.one(record.getLong("id"));
                    taskScheduler.submit(Task.build(newStatusTask));
                }
                break;
                default:
                    break;
            }
        }
    }

    @Override
    public NeoMap one(Long id) {
        return taskCacheHelper.get(id);
    }

    @Override
    public NeoMap one(String taskGroup, String taskName) {
        return taskCacheHelper.get(taskGroup, taskName);
    }

    /**
     * 针对删除的动作，传递过来的是实体数据进行特殊处理，因为删除操作，DB中已经灭有数据了
     */
    @SuppressWarnings("unchecked")
    private NeoMap getRecordFromMsg(TaskChgMsg taskChgMsg){
        if (null != taskChgMsg){
           if (taskChgMsg.getAction().equals(TaskChgEnum.DELETE)){
               return NeoMap.of((Map)taskChgMsg.getTaskData());
           }else {
               return taskDao.oneIgnoreStatus(Long.valueOf(String.valueOf(taskChgMsg.getTaskData())));
           }
        }
        return NeoMap.of();
    }

    /**
     * 根据分组和任务名获取下一次要执行的时间戳
     */
    public Long getNextScheduleTime(NeoMap configItem) {
        if (configItem.getStr(TIMING_TYPE_COLUMN).equals(TimingTypeEnum.PERM_AVAILABLE.getValue())){
            return null;
        }
        String taskGroup = configItem.getStr("task_group");
        String taskName = configItem.getStr("task_name");
        if (!StringUtils.isEmpty(taskGroup) && !StringUtils.isEmpty(taskName)) {
            NeoMap record = one(taskGroup, taskName);
            if (!NeoMap.isEmpty(record)) {

                // 定期失效的配置调度用schedule 来获取下次失效时间
                if(record.getStr(TIMING_TYPE_COLUMN).equals(TimingTypeEnum.SCHEDULE_UNAVAILABLE.getValue())) {
                    return taskScheduler.nextScheduleTime(record.getStr("cron")).getTime();
                }

                // 指定失效时间的配置，则下次失效时间为失效时间
                else if(record.getStr(TIMING_TYPE_COLUMN).equals(TimingTypeEnum.ASSIGN_UNAVAILABLE.getValue())){
                    return new Date(Long.valueOf(record.getStr("expire_time"))).getTime();
                }
            }
        }
        return null;
    }

    /**
     * 获取对应的配置是否需要重新加载，永久配置不需要判断这个，返回null，固定失效配置返回true，指定时间失效配置返回false
     * 当前版本：指定时间失效配置暂时不支持，做遗留
     */
    public Boolean getReLoadFlag(NeoMap configItem) {
        String taskGroup = configItem.getStr("task_group");
        String taskName = configItem.getStr("task_name");
        if (!StringUtils.isEmpty(taskGroup) && !StringUtils.isEmpty(taskName)) {
            NeoMap record = one(taskGroup, taskName);
            if (!NeoMap.isEmpty(record)) {
                if (configItem.getStr(TIMING_TYPE_COLUMN).equals(TimingTypeEnum.ASSIGN_UNAVAILABLE.getValue())) {
                    return false;
                } else if (configItem.getStr(TIMING_TYPE_COLUMN).equals(TimingTypeEnum.SCHEDULE_UNAVAILABLE.getValue())) {
                    return true;
                }
            }
        }
        return null;
    }
    /**
     * 获取在1024中的控制范围，在数据库中对应的数据量
     */
    public Integer getRangeCount(Integer from, Integer to) {
        return getNeo().exeCount("select count(1) from %s where (id & 1023) BETWEEN ? and ? and status = 'Y'",
                getTableName(), from, to);
    }

    /**
     * 获取在1024中的控制范围，在数据库中对应的数据量
     */
    public List<NeoMap> getRangeList(Integer from, Integer to, Integer pageStart, Integer pageSize) {
        return getNeo().exeList("select * from %s where (id & 1023) BETWEEN ? and ? and status = 'Y' limit ?,?",
            getTableName(), from, to, pageStart, pageSize);
    }
}
