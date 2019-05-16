package com.simon.king.server.task;

import com.simon.king.server.service.TaskExtService;
import com.simon.neo.NeoMap;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronExpression;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 任务调度器
 * @author zhouzhenyong
 * @since 2019/1/16 下午7:35
 */
@Slf4j
@Service
public class TaskScheduler{

    @Autowired
    private Scheduler scheduler;
    @Autowired
    private TaskExtService taskExtService;
    private static final Integer PAGE_SIZE = 1000;

    void start(){
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
            log.error("启动异常："+e.getMessage());
        }
    }

    /**
     * 新增控制范围
     * @param from 跟1024与上之后的起点
     * @param to 跟1024与上之后的终点
     */
    public void submit(Integer from, Integer to){
        Integer taskCount = taskExtService.getRangeCount(from, to);
        Integer index = 0;
        while ((index * PAGE_SIZE) < taskCount) {
            List<NeoMap> dataList = taskExtService.getRangeList(from, to, (index * PAGE_SIZE), PAGE_SIZE);
            if (!CollectionUtils.isEmpty(dataList)) {
                dataList.forEach(r -> this.submit(Task.build(r)));
            }
            index++;
        }
    }

    /**
     * 减少控制范围
     * @param from 跟1024与上之后的起点
     * @param to 跟1024与上之后的终点
     */
    public void shutdown(Integer from, Integer to){
        Integer taskCount = taskExtService.getRangeCount(from, to);
        Integer index = 0;
        while ((index * PAGE_SIZE) < taskCount) {
            List<NeoMap> dataList = taskExtService.getRangeList(from, to, (index * PAGE_SIZE), PAGE_SIZE);
            if (!CollectionUtils.isEmpty(dataList)) {
                dataList.forEach(r -> this.shutdown(Task.build(r)));
            }
            index++;
        }
    }

    public void submit(Task task) {
        try {
            if (null != task) {
                addJob(task.getJobDetail());
                addTrigger(task.getTrigger());
            }else{
                log.error("添加任务失败, task为null");
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            log.error("任务启动失败：" + e.getMessage());
        }
    }

    public void shutdown(Task task) {
        if (null != task) {
            try {
                stop(task.getTrigger());
            } catch (SchedulerException e) {
                e.printStackTrace();
                log.error("任务关闭失败：" + e.getMessage());
            }
        }
    }

    /**
     * 开启一列任务
     * @param taskList 任务列表
     */
    public void submitList(List<Task> taskList) {
        if (!CollectionUtils.isEmpty(taskList)) {
            taskList.forEach(this::submit);
        }
    }

    /**
     * 关闭一列任务
     * @param taskList 任务列表
     */
    public void shutdownList(List<Task> taskList){
        if (!CollectionUtils.isEmpty(taskList)) {
            taskList.forEach(this::shutdown);
        }
    }

    public Date nextScheduleTime(String cron){
        if(!StringUtils.isEmpty(cron)) {
            try {
                CronExpression cronExpression = new CronExpression(cron);
                return cronExpression.getNextValidTimeAfter(new Date());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return new Date();
    }

    private void addJob(JobDetail jobDetail) throws SchedulerException {
        if(null != jobDetail){
            if(!scheduler.checkExists(jobDetail.getKey())){
                scheduler.addJob(jobDetail, false);
            }
        }
    }

    private void addTrigger(Trigger trigger) throws SchedulerException {
        if(null != trigger){
            if(!scheduler.checkExists(trigger.getKey())) {
                scheduler.scheduleJob(trigger);
            }
        }
    }

    private void stop(Trigger trigger) throws SchedulerException {
        if(null != trigger){
            if(scheduler.checkExists(trigger.getKey())) {
                scheduler.unscheduleJob(trigger.getKey());
            }
        }
    }
}