package com.samueli.easyschedule;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.samueli.easyschedule.distributor.TaskDistributor;
import com.samueli.easyschedule.executor.ExecutorBuilder;
import com.samueli.easyschedule.executor.TaskExecutor;
import com.samueli.easyschedule.service.NodeRWService;
import com.samueli.easyschedule.service.TaskReadService;
import com.samueli.easyschedule.util.CacheHolder;
import com.samueli.easyschedule.util.IpUtils;

/**
 * 类TaskScheduler.java的实现描述：自调度执行器
 * 
 * @author samueli 2014年3月4日 上午10:01:56
 */
public class TaskScheduler {

    private static final int         NODE_CHECK_INTERVAL = 2 * 1000;
    private static final int         TASK_CHECK_INTERVAL = 2 * 1000;
    private static final Logger      log                 = Logger.getLogger(TaskScheduler.class);

    // 检测线程停止标志位
    private volatile boolean         stopped             = false;

    private volatile List<Node>      nodeList            = null;
    private volatile List<Task>      taskList            = null;

    private final Lock               nodeLock            = new ReentrantLock();
    private final Lock               taskLock            = new ReentrantLock();

    private TaskDistributor          distributor         = null;
    private TaskReadService          taskService         = null;
    private NodeRWService            nodeService         = null;
    private ExecutorBuilder          executorBuilder     = null;
    private Map<Task, ScheduledTask> scheduledTasks      = Maps.newConcurrentMap();
    private final Lock               scheduledLock       = new ReentrantLock();
    // 调度线程池
    private ScheduledExecutorService executors           = new ScheduledThreadPoolExecutor(20,
                                                                                           new ScheduledThreadFactory());

    public void setDistributor(TaskDistributor distributor) {
        this.distributor = distributor;
    }

    public void setTaskService(TaskReadService taskService) {
        this.taskService = taskService;
    }

    public void setNodeService(NodeRWService nodeService) {
        this.nodeService = nodeService;
    }

    public void setExecutorBuilder(ExecutorBuilder executorBuilder) {
        this.executorBuilder = executorBuilder;
    }

    /**
     * 已经调度的任务缓存
     */
    class ScheduledTask {

        private ScheduledFuture<?> future;
        private int                taskPeriodInSeconds;

        public ScheduledTask(ScheduledFuture<?> future, int taskPeriodInSeconds){
            super();
            this.future = future;
            this.taskPeriodInSeconds = taskPeriodInSeconds;
        }

        public ScheduledFuture<?> getFuture() {
            return future;
        }

        public int getTaskPeriodInSeconds() {
            return taskPeriodInSeconds;
        }
    }

    /**
     * 线程工厂
     * 
     * @author samueli 2014年5月14日
     */
    class ScheduledThreadFactory implements ThreadFactory {

        private final ThreadGroup   group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private static final String namePrefix   = "TaskScheduler-Thread-";

        ScheduledThreadFactory(){
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }

    /**
     * 挂起
     */
    public void pending() {
        this.pending = true;
    }

    /**
     * 恢复任务
     */
    public void resume() {
        this.pending = false;
    }

    private volatile boolean pending = false; // 任务挂起状态。

    /**
     * 类TaskScheduler.java的实现描述：调度执行的实际业务逻辑
     * 
     * @author samueli 2014年5月14日 下午9:11:56
     */
    class ScheduleThread implements Runnable {

        private final Task task;

        public ScheduleThread(Task task){
            this.task = task;
        }

        @Override
        public void run() {

            // 如果任务是挂起状态。则任务不往下执行。(方便做测试使用。) //默认任务挂起状态(false）
            if (pending) {
                return;
            }

            // 执行本地调度
            log.warn("ScheduleThread execute for task:" + task + " on node:" + IpUtils.localIp());

            try {
                // 执行实际的任务
                TaskExecutor builder = executorBuilder.build(task);
                if (builder == null) {
                    log.error("unknown task executor: " + task.getExecutor());
                } else {
                    builder.execute(task);
                }
            } catch (Exception e) {
                // log schedule exception
                log.error("schedule " + task + " on localhost[" + IpUtils.localIp() + "] exception", e);
            }
        }
    }

    /**
     * 初始化
     */
    public void init() {
        // 启动机器变化检测线程，同时周期性的把自己注册到机器列表中
        new LiveNodeCheckThread(this).start();
        // 启动任务变化检测线程
        new TaskCheckThread(this).start();

        try {
            nodeLock.lock();
            nodeList = nodeService.read();
        } finally {
            nodeLock.unlock();
        }
        try {
            taskLock.lock();
            taskList = taskService.read();
        } finally {
            taskLock.unlock();
        }
    }

    /**
     * 强制在某台机器上重新调度task任务
     * 
     * @param task
     * @param nodeIp
     */
    public void schedule(final Task task, final String nodeIp) {
        if (StringUtils.equals(nodeIp, IpUtils.localIp())) {
            schedule(task, true);
        }
    }

    /**
     * 根据task指定调度间隔在nodes上调度任务
     * 
     * @param task
     * @param nodes
     * @param force 忽略调度间隔是否变化，强制重新调度
     */
    private void schedule(final Task task, boolean force) {
        if (task == null) {
            return;
        }

        try {
            // 加锁避免多线程进入下面代码，导致同一task下的多次put成功，但之前的future没有cancel
            scheduledLock.lock();
            ScheduledTask existScheduler = scheduledTasks.get(task);
            if (existScheduler == null) {
                ScheduleThread cmd = new ScheduleThread(task);
                ScheduledFuture<?> future = executors.scheduleWithFixedDelay(cmd, 1, task.getPeriodInSeconds(),
                                                                             TimeUnit.SECONDS);
                scheduledTasks.put(task, new ScheduledTask(future, task.getPeriodInSeconds()));
            } else {
                // 如果调度间隔发生变化，需要取消原来任务，重新调度
                if (force || existScheduler.getTaskPeriodInSeconds() != task.getPeriodInSeconds()) {
                    existScheduler.getFuture().cancel(false);
                    ScheduleThread scheduler = new ScheduleThread(task);
                    ScheduledFuture<?> future = executors.scheduleWithFixedDelay(scheduler, 1,
                                                                                 task.getPeriodInSeconds(),
                                                                                 TimeUnit.SECONDS);
                    scheduledTasks.put(task, new ScheduledTask(future, task.getPeriodInSeconds()));
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("task schedule period not change, task:" + task);
                    }
                }
            }
        } finally {
            scheduledLock.unlock();
        }
    }

    private void schedule() {
        List<Task> tasks = null;
        try {
            taskLock.lock();
            tasks = this.taskList;
        } finally {
            taskLock.unlock();
        }

        List<Node> nodes = null;
        try {
            nodeLock.lock();
            nodes = this.nodeList;
        } finally {
            nodeLock.unlock();
        }

        if (tasks == null || tasks.size() == 0 || nodes == null || nodes.size() == 0) {
            log.error("no task or not nodes can schedule, waiting for next loop");
            return;
        }

        Map<String, List<Task>> distribMapper = distributor.distribute(tasks, nodes);
        if (distribMapper == null || distribMapper.size() == 0) {
            log.error("distribute task error");
            return;
        }

        CacheHolder.set(CacheHolder.Key.DISTRIBUTE_RESULT, distribMapper);

        log.warn("task distribute result:" + distribMapper);

        // 获取本机的所有分配调度任务
        List<Task> distTasks = distribMapper.get(IpUtils.localIp());
        if (distTasks == null || distTasks.size() == 0) {
            return;
        }

        for (Task task : distTasks) {
            try {
                schedule(task, false);
            } catch (Exception e) {
                log.error("schedule task exception, taskId=" + task.getId(), e);
            }
        }
    }

    public void destroy() {
        // 停止node和task变化监听
        stopped = true;

        // 停止调度线程池
        executors.shutdown();
    }

    private void notifyNodeChange(List<Node> nodeList) {
        try {
            nodeLock.lock();
            this.nodeList = nodeList;
        } finally {
            nodeLock.unlock();
        }

        schedule();
    }

    private void notifyTaskChange(List<Task> taskList) {
        try {
            taskLock.lock();
            this.taskList = taskList;
        } finally {
            taskLock.unlock();
        }

        schedule();
    }

    class LiveNodeCheckThread extends Thread {

        TaskScheduler scheduler = null;

        public LiveNodeCheckThread(TaskScheduler scheduler){
            super("LiveNodeCheckThread");
            this.scheduler = scheduler;
        }

        public void run() {
            while (!scheduler.stopped) {
                if (log.isInfoEnabled()) {
                    log.info("LiveNodeCheckThread check now....");
                }

                try {
                    registerSelf();
                    List<Node> nodeList = nodeService.read();
                    scheduler.notifyNodeChange(nodeList);

                } catch (Throwable throwable) {
                    log.error("LiveNodeCheckThreadERROR------- run ", throwable);
                }
                try {
                    Thread.sleep(NODE_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    log.error("LiveNodeCheckThread sleep exception", e);
                }
            }
        }

        private void registerSelf() {
            String localIp = IpUtils.localIp();

            if (localIp != null) {
                Node node = new Node(localIp);
                nodeService.write(node);
            }
        }
    }

    class TaskCheckThread extends Thread {

        TaskScheduler scheduler = null;

        public TaskCheckThread(TaskScheduler scheduler){
            super("TaskCheckThread");
            this.scheduler = scheduler;
        }

        public void run() {
            while (!scheduler.stopped) {
                if (log.isInfoEnabled()) {
                    log.info("TaskCheckThread check now....");
                }
                try {
                    List<Task> taskList = taskService.read();
                    scheduler.notifyTaskChange(taskList);
                } catch (Throwable throwable) {
                    log.error("TaskCheckThreadERROR------- run ", throwable);
                }
                try {
                    Thread.sleep(TASK_CHECK_INTERVAL);
                } catch (InterruptedException e) {
                    log.error("TaskCheckThread sleep exception", e);
                }
            }
        }
    }

}
