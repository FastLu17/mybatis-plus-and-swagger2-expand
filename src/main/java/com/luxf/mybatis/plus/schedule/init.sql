CREATE TABLE `t_scheduling_task`(
  `id`          bigint(20) NOT NULL,
  `class_name`  varchar(255) NOT NULL COMMENT '类名(Spring Bean)',
  `method_name` varchar(128) NOT NULL COMMENT '方法名',
  `cron`        varchar(20)  NOT NULL COMMENT 'cron表达式',
  `opened`      tinyint(1) NOT NULL COMMENT '任务是否开启',
  `task_name`   varchar(32) DEFAULT NULL COMMENT '任务名',
  `create_time` datetime     NOT NULL,
  `update_time` datetime    DEFAULT NULL,
  `created_By`  varchar(32) DEFAULT NULL,
  `update_By`   varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
