CREATE TABLE `t_task` (
  `id` bigint(32) NOT NULL COMMENT '主键',
  `task_group` char(64) NOT NULL COMMENT '数据来源组，外键关联lk_config_group',
  `task_name` varchar(64) NOT NULL DEFAULT '' COMMENT '任务name',
  `task_desc` varchar(120) NOT NULL DEFAULT '' COMMENT '任务描述',
  `cron` varchar(120) NOT NULL COMMENT 'cron表达式',
  `status` enum('Y','N') DEFAULT 'Y' COMMENT '状态:Y=启用;N=禁用',
  `task_type` enum('GROOVY','URL') NOT NULL COMMENT '任务类型:GROOVY=groovy脚本;URL=url链接post方式',
  `data` text NOT NULL COMMENT 'groovy脚本或者url表达式，根据task_type确定',
  `param` text COMMENT 'url时候的post参数',
  `run_status` enum('RUNNING','DONE') DEFAULT 'DONE' COMMENT '执行状态:RUNNING=执行中;DONE=完成',
  `create_user_name` varchar(24) DEFAULT NULL COMMENT '创建人名字',
  `update_user_name` varchar(24) DEFAULT NULL COMMENT '修改人名字',
  `create_time` datetime(3) NOT NULL COMMENT '创建时间',
  `update_time` datetime(3) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_code_index` (`task_group`,`task_name`),
  KEY `task_name_index` (`task_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

CREATE TABLE `t_config_group` (
  `id` bigint(32) NOT NULL COMMENT '主键',
  `group_code` varchar(64) DEFAULT NULL COMMENT '分组编码',
  `group_name` varchar(120) DEFAULT NULL COMMENT '分组名称',
  `create_time` datetime(3) NOT NULL,
  `update_time` datetime(3) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `group_index` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分组配置';