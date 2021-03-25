package com.luxf.mybatis.plus.config.schedule;

import java.lang.annotation.*;

/**
 * @author 小66
 * @create 2021-03-25 19:25
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicSchedule {

    /**
     * cron表达式
     */
    String value();

    /**
     * 是否开启、
     */
    boolean opened() default true;

    /**
     * 任务名称
     */
    String taskName() default "";
}

