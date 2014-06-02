package com.samueli.easyschedule.distributor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.samueli.easyschedule.Node;
import com.samueli.easyschedule.Task;

/**
 * 机器按照ip排序，任务按照id分配
 * 
 * @author samueli
 */
public class HashDistributor implements TaskDistributor {

    @Override
    public Map<String, List<Task>> distribute(List<Task> tasks, List<Node> nodes) {
        int nodeSize = nodes.size();
        int taskSize = tasks.size();
        if (tasks == null || taskSize == 0 || nodes == null || nodeSize == 0) {
            return null;
        }

        // 按照ip的hash排序
        Collections.sort(nodes, new Comparator<Node>() {

            @Override
            public int compare(Node o1, Node o2) {
                long ip1Hash = o1.getIp().hashCode();
                long ip2Hash = o2.getIp().hashCode();
                return (int) (ip1Hash - ip2Hash);
            }
        });

        Collections.sort(tasks, new Comparator<Task>() {

            @Override
            public int compare(Task o1, Task o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });

        // 保证key按照weight有序
        LinkedHashMap<String, List<Task>> map = new LinkedHashMap<String, List<Task>>();
        for (Node node : nodes) {
            map.put(node.getIp(), new ArrayList<Task>());
        }

        // 机器多于任务
        if (nodes.size() >= tasks.size()) {
            for (int i = 0; i < nodes.size(); i++) {
                map.get(nodes.get(i).getIp()).add(tasks.get(i % tasks.size()));
            }
        } else {
            // 任务多于机器
            for (int i = 0; i < tasks.size(); i++) {
                map.get(nodes.get(i % nodes.size()).getIp()).add(tasks.get(i));
            }
        }

        return map;
    }

}
