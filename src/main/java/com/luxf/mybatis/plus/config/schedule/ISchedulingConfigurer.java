package com.luxf.mybatis.plus.config.schedule;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.luxf.mybatis.plus.schedule.entity.SchedulingTask;
import com.luxf.mybatis.plus.schedule.service.SchedulingTaskService;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author luxf
 * @date 2021-03-23 11:52
 **/
@Slf4j
@Component
@EnableScheduling
public class ISchedulingConfigurer implements SchedulingConfigurer, InitializingBean {

    private final Map<Long, SchedulingTask> taskMap = new HashMap<>(32);

    @Resource
    private SchedulingTaskService taskService;

    @Resource
    private ApplicationContext context;

    @Resource
    private Snowflake idWorker;

    /**
     * org.springframework.scheduling.config.ScheduledTaskRegistrar#getScheduledTasks() --> 返回值是 unmodifiableSet
     */
    private ScheduledTaskRegistrar taskRegistrar;

    /**
     * 初始值是ScheduledTaskRegistrar#getScheduledTasks()的结果、
     */
    private Set<ScheduledTask> scheduledTasks;

    @Override
    public void afterPropertiesSet() throws Exception {
        Class<?> mainClass = deduceMainApplicationClass();
        if (Objects.isNull(mainClass)) {
            return;
        }
        initTaskMap(mainClass);
    }

    /**
     * 在这个方法执行时, org.springframework.scheduling.config.ScheduledTaskRegistrar#scheduleTasks() 还没有执行,
     * 此时 org.springframework.scheduling.config.ScheduledTaskRegistrar#getScheduledTasks() is empty.
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        taskRegistrar = registrar;
        taskMap.forEach((key, task) -> initTaskInstance(task, taskInfo -> {
            // 此处可以使用分布式锁, 解决集群时, 相同的定时任务全都启动的问题.
            registrar.addTriggerTask(TaskRunnable.of(taskInfo), TaskTrigger.of(taskInfo));
            log.info("addTriggerTask: [{}]", getKey(taskInfo.getMethodName(), taskInfo.getClassName()));
        }));
    }

    /**
     * 修改定时任务, 下一次执行任务时, 修改才会生效.
     */
    public void modifyTask(SchedulingTask info) {
        SchedulingTask taskInfo = taskMap.get(info.getId());
        if (taskInfo == null) {
            return;
        }
        taskInfo.setOpened(info.getOpened());
        taskInfo.setCron(info.getCron());
    }

    /**
     * 取消任务, 取消后, if the trigger won't fire anymore、
     */
    public void cancelTask(Long taskId) {
        for (ScheduledTask scheduledTask : getTasks()) {
            Runnable run = scheduledTask.getTask().getRunnable();
            if (run instanceof TaskRunnable) {
                TaskRunnable runnable = (TaskRunnable) run;
                SchedulingTask taskInfo = runnable.taskInfo;
                if (taskId.equals(taskInfo.getId())) {
                    scheduledTask.cancel();
                    getTasks().remove(scheduledTask);
                    taskMap.remove(taskId);
                    return;
                }
            }
        }
    }

    /**
     * 恢复定时任务, 实际是新建一个定时任务
     */
    public void resumeTask(SchedulingTask task) {
        task.setOpened(Boolean.TRUE);
        if (taskMap.containsKey(task.getId())) {
            modifyTask(task);
            return;
        }

        initTaskInstance(task, taskInfo -> {
            TriggerTask triggerTask = new TriggerTask(TaskRunnable.of(taskInfo), TaskTrigger.of(taskInfo));
            // 执行ScheduledTaskRegistrar.scheduleTriggerTask(TriggerTask)后, 就会创建新的定时任务、
            ScheduledTask scheduledTask = taskRegistrar.scheduleTriggerTask(triggerTask);
            if (scheduledTask != null) {
                taskMap.put(taskInfo.getId(), taskInfo);
                getTasks().add(scheduledTask);
                log.info("resumeTask: [{}]", getKey(task.getMethodName(), task.getClassName()));
            }
        });
    }

    private void initTaskInstance(SchedulingTask task, Consumer<SchedulingTask> consumer) {
        try {
            // 获取Spring Context Bean.
            Object bean = context.getBean(Class.forName(task.getClassName()));
            task.setInstance(bean);

            consumer.accept(task);
        } catch (BeansException e) {
            log.error("Spring Context No Such Bean [{}]", task.getClassName());
        } catch (ClassNotFoundException e) {
            log.error("ClassNotFound [{}]", task.getClassName());
        }
    }

    private Set<ScheduledTask> getTasks() {
        if (scheduledTasks == null) {
            synchronized (taskMap) {
                scheduledTasks = new LinkedHashSet<>(taskRegistrar.getScheduledTasks());
            }
        }
        return scheduledTasks;
    }

    private void initTaskMap(Class<?> mainClass) {
        try {
            String packageName = mainClass.getPackage().getName();
            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(packageName))
                    .filterInputsBy(str -> str != null && str.endsWith(".class"))
                    .addScanners(new MethodAnnotationsScanner()));
            Set<Method> methods = reflections.getMethodsAnnotatedWith(DynamicSchedule.class);
            List<SchedulingTask> taskList = taskService.list();
            if (CollectionUtils.isEmpty(methods)) {
                if (!taskList.isEmpty()) {
                    // 删除不存在的任务、
                    taskService.removeByIds(taskList.stream().map(SchedulingTask::getId).collect(Collectors.toList()));
                    taskList.clear();
                }
                return;
            }
            Map<String, SchedulingTask> taskKeyMap = taskList.stream()
                    .collect(Collectors.toMap(task -> getKey(task.getMethodName(), task.getClassName()), t -> t));
            List<SchedulingTask> saveTaskList = new ArrayList<>();
            for (Method method : methods) {
                DynamicSchedule schedule = method.getAnnotation(DynamicSchedule.class);
                Class<?> declaringClass = method.getDeclaringClass();
                String methodName = method.getName();
                String className = declaringClass.getName();
                SchedulingTask task = taskKeyMap.get(getKey(methodName, className));
                if (task == null) {
                    task = new SchedulingTask();
                    task.setId(idWorker.nextId());
                    task.setCron(schedule.value());
                    task.setOpened(schedule.opened());
                    task.setTaskName(schedule.taskName());
                    task.setClassName(className);
                    task.setMethodName(methodName);
                    task.setCreateTime(LocalDateTime.now());
                    saveTaskList.add(task);
                }
                this.taskMap.put(task.getId(), task);
            }
            taskService.saveBatch(saveTaskList);
        } catch (Exception e) {
            log.error("initTaskMap error. detail is: ", e);
        }
    }

    private String getKey(String methodName, String className) {
        return className + StringPool.DOT + methodName;
    }

    private Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        } catch (ClassNotFoundException ex) {
            log.error("deduceMainApplicationClass error.");
        }
        return null;
    }

    private static class TaskRunnable implements Runnable {
        private final SchedulingTask taskInfo;

        private TaskRunnable(SchedulingTask taskInfo) {
            this.taskInfo = taskInfo;
        }

        static TaskRunnable of(SchedulingTask taskInfo) {
            return new TaskRunnable(taskInfo);
        }

        @Override
        public void run() {
            // opened == false 只是假死状态、
            // 但是执行org.springframework.scheduling.config.ScheduledTask.cancel() --> if the trigger won't fire anymore.
            if (taskInfo.getOpened()) {
                Object instance = taskInfo.getInstance();
                ReflectUtil.invoke(instance, taskInfo.getMethodName());
            }
        }
    }


    private static class TaskTrigger implements Trigger {
        private final SchedulingTask task;

        private TaskTrigger(SchedulingTask task) {
            this.task = task;
        }

        static TaskTrigger of(SchedulingTask task) {
            return new TaskTrigger(task);
        }

        @Override
        public Date nextExecutionTime(TriggerContext triggerContext) {
            try {
                CronTrigger trigger = new CronTrigger(task.getCron());
                return trigger.nextExecutionTime(triggerContext);
            } catch (Exception e) {
                log.error("create trigger error: ", e);
                return null;
            }
        }
    }
}
