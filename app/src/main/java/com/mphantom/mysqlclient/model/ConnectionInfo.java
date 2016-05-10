package com.mphantom.mysqlclient.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by wushaorong on 11/6/15.
 */
public class ConnectionInfo extends RealmObject {
    @PrimaryKey
    private String name;
    private String host;
    private int port;
    private String userName;
    private String password;
    private String database;

    public ConnectionInfo() {
    }

    public ConnectionInfo(String name, String host, int port, String userName, String password, String database) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.database = database;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }


}
