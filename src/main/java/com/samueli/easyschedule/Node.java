package com.samueli.easyschedule;

/**
 * 调度可用机器
 * 
 * @author samueli
 */
public class Node {

    private String ip;

    public Node(String ip){
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Node other = (Node) obj;
        if (ip == null) {
            if (other.ip != null) return false;
        } else if (!ip.equals(other.ip)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "Node [ip=" + ip + "]";
    }

}
