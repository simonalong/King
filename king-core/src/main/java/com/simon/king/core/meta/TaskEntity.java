package com.simon.king.core.meta;

import java.sql.Timestamp;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 任务表
 * @author robot
 */
@Data
@Accessors(chain = true)
public class TaskEntity {


    /**
     * 主键
     */
    private Long id;

    /**
     * 数据来源组，外键关联lk_config_group
     */
    private String taskGroup;

    /**
     * 任务name
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * cron表达式
     */
    private String cron;

    /**
     * 状态:Y=启用;N=禁用
     */
    private String status;

    /**
     * 任务类型:GROOVY=groovy脚本;URL=url链接post方式
     */
    private String taskType;

    /**
     * groovy脚本或者url表达式，根据task_type确定
     */
    private String data;

    /**
     * url时候的post参数
     */
    private String param;

    /**
     * 执行状态:RUNNING=执行中;DONE=完成
     */
    private String runStatus;

    /**
     * 创建人名字
     */
    private String createUserName;

    /**
     * 修改人名字
     */
    private String updateUserName;

    /**
     * 创建时间
     */
    private Timestamp createTime;

    /**
     * 更新时间
     */
    private Timestamp updateTime;

}
