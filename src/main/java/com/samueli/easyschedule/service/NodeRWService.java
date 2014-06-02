package com.samueli.easyschedule.service;

import java.util.List;

import com.samueli.easyschedule.Node;

/**
 * 读写机器的接口
 * 
 * @author samueli 2014年6月2日
 */
public interface NodeRWService {

    List<Node> read();

    boolean write(Node node);
}
