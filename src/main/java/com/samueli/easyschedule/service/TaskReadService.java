package com.samueli.easyschedule.service;

import java.util.List;

import com.samueli.easyschedule.Task;

/**
 * 任务读取接口
 * 
 * @author samueli 2014年6月2日
 */
public interface TaskReadService {

    List<Task> read();
}
