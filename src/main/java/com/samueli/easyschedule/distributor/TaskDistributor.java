package com.samueli.easyschedule.distributor;

import java.util.List;
import java.util.Map;

import com.samueli.easyschedule.Node;
import com.samueli.easyschedule.Task;

/**
 * 调度算法
 * 
 * @author samueli
 */
public interface TaskDistributor {

    Map<String, List<Task>> distribute(List<Task> tasks, List<Node> nodes);
}
