package com.samueli.easyschedule.executor;

import java.util.Map;

import com.samueli.easyschedule.Task;

public class ExecutorBuilder {

    private Map<String, TaskExecutor> executors;

    public void setExecutors(Map<String, TaskExecutor> executors) {
        this.executors = executors;
    }

    public TaskExecutor build(Task task) {
        return executors.get(task.getExecutor());
    }

}
