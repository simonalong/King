package com.simon.king.server.task;

import static org.quartz.JobBuilder.newJob;

import com.simon.king.server.exception.CronParseException;
import com.simon.neo.NeoMap;
import java.text.MessageFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.util.StringUtils;

/**
 * 调度器的任务实体
 * @author zhouzhenyong
 * @since 2019/1/16 下午7:36
 */
@Getter
@AllArgsConstructor
public class Task {

    private JobDetail jobDetail;
    private Trigger trigger;

    public static Task build(NeoMap record) {
        try {
            if (!record.isEmpty()) {
                JobDetail job = buildJob(record);
                Trigger trigger = null;

                trigger = buildTrigger(record, job);
                if (null != job && null != trigger) {
                    return new Task(job, trigger);
                }
            }
        } catch (CronParseException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private static JobDetail buildJob(NeoMap record) {
        if (!NeoMap.isEmpty(record)) {
            JobBuilder jobBuilder = newJob(TaskJob.class).storeDurably();

            String taskGroup = record.getStr("task_group");
            if (!StringUtils.isEmpty(taskGroup)) {
                jobBuilder.withIdentity(buildJobKey(taskGroup), taskGroup)
                    .usingJobData("group", taskGroup);
            }
            return jobBuilder.build();
        }
        return null;
    }

    private static Trigger buildTrigger(NeoMap record, JobDetail jobDetail) throws CronParseException {
        if (!NeoMap.isEmpty(record)) {
            TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();

            String taskGroup = record.getStr("task_group");
            String taskName = record.getStr("task_name");
            Long id = record.getLong("id");
            String cron = record.getStr("cron");
            if (!StringUtils.isEmpty(taskGroup) && !StringUtils.isEmpty(taskName) && !StringUtils.isEmpty(cron)) {
                CronScheduleBuilder cronScheduleBuilder;
                try {
                    cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cron);
                } catch (Exception e) {
                    throw new CronParseException(MessageFormat.format("cron表达式quartz解析异常：{}", cron));
                }

                triggerBuilder.withIdentity(buildTriggerKey(taskName), taskGroup)
                    .usingJobData("id", id)
                    .forJob(jobDetail)
                    .withSchedule(cronScheduleBuilder);
            }
            return triggerBuilder.build();
        }

        return null;
    }

    private static String buildJobKey(String name){
        return MessageFormat.format("job_{0}", name);
    }

    private static String buildTriggerKey(String name){
        return MessageFormat.format("trigger_{0}", name);
    }
}
