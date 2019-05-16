package com.simon.king.server.task.monitor;

import com.simon.neo.NeoMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author zhouzhenyong
 * @since 2019/2/27 下午2:42
 */
@Accessors(chain = true)
@ToString
public class TaskMonitorEntity {
    @Getter
    @Setter
    private Long taskId;
    @Getter
    @Setter
    private String taskGroup;
    @Getter
    @Setter
    private String taskName;
    @Getter
    @Setter
    private String taskDesc;
    @Getter
    private Long createTime;

    private TaskMonitorEntity(){}

    public TaskMonitorEntity(Long taskId, String taskGroup, String taskName, String taskDesc){
        this.taskId = taskId;
        this.taskGroup = taskGroup;
        this.taskName = taskName;
        this.taskDesc = taskDesc;
        this.createTime = System.currentTimeMillis();
    }

    public static TaskMonitorEntity build(NeoMap record) {
        if (!NeoMap.isEmpty(record)) {
            TaskMonitorEntity monitor = new TaskMonitorEntity()
                .setTaskId(record.getLong("id"))
                .setTaskGroup(record.getStr("task_group"))
                .setTaskName(record.getStr("task_name"))
                .setTaskDesc(record.getStr("task_desc"));
            monitor.createTime = System.currentTimeMillis();
            return monitor;
        }
        return new TaskMonitorEntity();
    }
}
