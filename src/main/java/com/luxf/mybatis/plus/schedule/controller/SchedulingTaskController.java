package com.luxf.mybatis.plus.schedule.controller;

import com.luxf.mybatis.plus.base.Result;
import com.luxf.mybatis.plus.config.schedule.DynamicSchedule;
import com.luxf.mybatis.plus.config.schedule.ISchedulingConfigurer;
import com.luxf.mybatis.plus.schedule.entity.SchedulingTask;
import com.luxf.mybatis.plus.schedule.req.scheduling.SchedulingTaskReq;
import com.luxf.mybatis.plus.schedule.service.SchedulingTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author luxf
 * @since 2021-03-25
 */
@RestController
@RequestMapping("/scheduling-task")
@Api(value = "定时任务", tags = "定时任务管理")
@Validated
public class SchedulingTaskController {

    @Resource
    private SchedulingTaskService schedulingTaskService;

    @Resource
    private ISchedulingConfigurer configurer;

    /**
     * 根据ID修改 SchedulingTask
     *
     * @param id id
     * @return Result
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "根据ID修改 定时任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", required = true, paramType = "path"),
    })
    public Result<?> updateById(@PathVariable Long id, @Valid @RequestBody SchedulingTaskReq req) {
        SchedulingTask task = new SchedulingTask();
        BeanUtils.copyProperties(req, task);
        task.setId(id);
        configurer.modifyTask(task);
        boolean updated = schedulingTaskService.updateById(task);
        return updated ? Result.success() : Result.failed();
    }

    /**
     * 根据ID取消 SchedulingTask
     *
     * @param id id
     * @return Result
     */
    @PutMapping("/action/cancel/{id}")
    @ApiOperation(value = "根据ID取消 定时任务")
    @ApiImplicitParam(name = "id", required = true, paramType = "path")
    public Result<?> cancelById(@PathVariable Long id) {
        configurer.cancelTask(id);
        return Result.success();
    }

    /**
     * 根据ID恢复 SchedulingTask
     *
     * @param id id
     * @return Result
     */
    @PutMapping("/action/resume/{id}")
    @ApiOperation(value = "根据ID恢复 定时任务")
    @ApiImplicitParam(name = "id", required = true, paramType = "path")
    public Result<?> resumeById(@PathVariable Long id) {
        SchedulingTask taskById = schedulingTaskService.getById(id);
        if (taskById == null) {
            return Result.failed();
        }
        // 先取消、再重启Task.
        configurer.cancelTask(id);
        configurer.resumeTask(taskById);
        return Result.success();
    }

    /**
     * 根据ID查询 SchedulingTask
     *
     * @param id id
     * @return Result
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "根据ID查询 定时任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", required = true, paramType = "path"),
    })
    public Result<SchedulingTask> getById(@PathVariable Long id) {
        return Result.success(schedulingTaskService.getById(id));
    }

    /**
     * 根据ID查询 SchedulingTask
     *
     * @return Result
     */
    @GetMapping("/all-list")
    @ApiOperation(value = "查询所有的 定时任务")
    public Result<List<SchedulingTask>> getAllTask() {
        return Result.success(schedulingTaskService.list());
    }

    @DynamicSchedule(value = "0/10 * * * * ?", taskName = "测试定时任务")
    public void testTesk() {
        System.out.println("LocalTime.now() = " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}

