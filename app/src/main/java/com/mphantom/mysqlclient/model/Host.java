package com.mphantom.mysqlclient.model;

/**
 * Created by wushaorong on 16-5-14.
 */
public class Host {
    private String name;
    private int port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
