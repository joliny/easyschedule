package com.samueli.easyschedule.executor;

import com.samueli.easyschedule.Task;

/**
 * 任务执行接口，实现这个接口完成具体的任务执行逻辑
 * 
 * @author samueli 2014年6月2日
 */
public interface TaskExecutor {

    public void execute(Task task);

}
