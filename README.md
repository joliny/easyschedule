easyschedule
============

### 介绍

一个简单的调度程序，主要特性包括：

1.  可用调度机器的心跳检测和故障转移；
2.  把不同任务分布式调度到不同机器；
2.  支持动态调整任务的调度频率；

### Quick Start

参考测试代码[这里](https://github.com/samueli/easyschedule/blob/master/src/test/java/com/samueli/easyschedule/TaskSchedulerTest.java)

### 版本记录

#### 1.0.0 (2014-06-01)
- 完成easyschedule核心功能
- 实现任务调度和任务执行在同机执行的自调度
- 实现简单的hash调度算法
- 借助drone.io在线构建

### 设计

TODO

### 构建状态 
[![Build Status](https://drone.io/github.com/samueli/easyschedule/status.png)](https://drone.io/github.com/samueli/easyschedule/latest)
