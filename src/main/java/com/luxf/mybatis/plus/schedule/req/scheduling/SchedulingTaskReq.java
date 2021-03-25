package com.luxf.mybatis.plus.schedule.req.scheduling;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author luxf
 * @since 2021-03-25
 */
@Data
@ApiModel(value="SchedulingTaskReq")
public class SchedulingTaskReq implements Serializable {

    @ApiModelProperty(value = "cron表达式")
    @NotBlank
    private String cron;

    @ApiModelProperty(value = "任务是否开启")
    @NotNull
    private Boolean opened;

    @ApiModelProperty(value = "任务名,默认''")
    private String taskName;
}
