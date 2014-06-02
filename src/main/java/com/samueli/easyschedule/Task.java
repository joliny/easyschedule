package com.samueli.easyschedule;

/**
 * 一个调度任务，根据id获取任务详情
 * 
 * @author samueli
 */
public class Task {

    private Long   id;
    private String name;
    private String executor;       // task执行器标识
    private int    weight;         // 权重
    private int    periodInSeconds; // 调度频率，单位秒
    private String bizCode;        // 业务code，根据业务code可以找到具体的业务任务

    public String getBizCode() {
        return bizCode;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getPeriodInSeconds() {
        return periodInSeconds;
    }

    public void setPeriodInSeconds(int periodInSeconds) {
        this.periodInSeconds = periodInSeconds;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Task other = (Task) obj;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Task [id=" + id + "]";
    }

}
