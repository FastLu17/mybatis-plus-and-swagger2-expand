package com.luxf.mybatis.plus.schedule.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.luxf.mybatis.plus.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author luxf
 * @since 2021-03-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_scheduling_task")
@ApiModel(value = "SchedulingTask")
public class SchedulingTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "类名(Spring Bean)")
    private String className;

    @ApiModelProperty(value = "方法名")
    private String methodName;

    @ApiModelProperty(value = "cron表达式")
    private String cron;

    @ApiModelProperty(value = "任务是否开启")
    private Boolean opened;

    @ApiModelProperty(value = "任务名")
    private String taskName;

    /**
     * Spring Bean、
     */
    @TableField(exist = false)
    private Object instance;

    public void setInstance(Object instance) {
        synchronized (this) {
            if (this.instance == null) {
                this.instance = instance;
            }
        }
    }

    public Object getInstance() {
        return this.instance;
    }
}
