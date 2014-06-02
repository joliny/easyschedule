package com.samueli.easyschedule.distributor;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.samueli.easyschedule.Node;
import com.samueli.easyschedule.Task;

public class HashDistributorTest {

    private TaskDistributor hashDistributor = new HashDistributor();

    @Test
    public void testDistribute() {
        List<Task> tasks = Lists.newArrayList();
        for (int i = 1; i <= 5; i++) {
            Task task1 = new Task();
            Long id = Long.valueOf(i);
            task1.setId(id);
            tasks.add(task1);
        }

        List<Node> nodes = Lists.newArrayList();
        for (int i = 1; i <= 10; i++) {
            String ip = "192.168.0." + i;
            nodes.add(new Node(ip));
        }

        Map<String, List<Task>> result = hashDistributor.distribute(tasks, nodes);
        for (Entry<String, List<Task>> entry : result.entrySet()) {
            System.out.print("\n" + entry.getKey() + " -> ");
            for (Task task : entry.getValue()) {
                System.out.print("[" + task.getId() + "]");
            }
        }
    }

}
