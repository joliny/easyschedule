package com.samueli.easyschedule;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.samueli.easyschedule.distributor.HashDistributor;
import com.samueli.easyschedule.executor.ExecutorBuilder;
import com.samueli.easyschedule.executor.TaskExecutor;
import com.samueli.easyschedule.service.NodeRWService;
import com.samueli.easyschedule.service.TaskReadService;

public class TaskSchedulerTest {

    private TaskScheduler scheduler = new TaskScheduler();

    @Before
    public void init() {
        scheduler.setDistributor(new HashDistributor());
        ExecutorBuilder executorBuilder = new ExecutorBuilder();
        Map<String, TaskExecutor> executors = Maps.newHashMap();
        executors.put("TestExecutor", new TaskExecutor() {

            @Override
            public void execute(Task task) {
                System.out.println("task[" + task.getId() + "] executor with bizCode:" + task.getBizCode());
            }
        });
        executorBuilder.setExecutors(executors);
        scheduler.setExecutorBuilder(executorBuilder);
        scheduler.setNodeService(new NodeRWService() {

            List<Node> ipList = Lists.newArrayList();

            @Override
            public boolean write(Node node) {
                return ipList.add(node);
            }

            @Override
            public List<Node> read() {
                return ipList;
            }
        });
        scheduler.setTaskService(new TaskReadService() {

            @Override
            public List<Task> read() {
                List<Task> taskList = Lists.newArrayList();
                Task task = new Task();
                task.setId(1L);
                task.setBizCode("1");
                task.setName("testTask");
                task.setExecutor("TestExecutor");
                task.setPeriodInSeconds(5);
                task.setWeight(10);

                taskList.add(task);
                return taskList;
            }
        });
    }

    @Test
    public void testSchedule() {
        scheduler.init();

        try {
            Thread.sleep(60 * 60 * 1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
