package com.mphantom.mysqlclient.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wushaorong on 11/6/15.
 */
public class ConnectionInfo implements Parcelable {
    private String name;
    private String host;
    private int port;
    private String userName;
    private String password;
    private String database;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.host);
        dest.writeInt(this.port);
        dest.writeString(this.userName);
        dest.writeString(this.password);
        dest.writeString(this.database);
    }

    public ConnectionInfo() {
    }

    protected ConnectionInfo(Parcel in) {
        this.name = in.readString();
        this.host = in.readString();
        this.port = in.readInt();
        this.userName = in.readString();
        this.password = in.readString();
        this.database = in.readString();
    }

    public static final Parcelable.Creator<ConnectionInfo> CREATOR = new Parcelable.Creator<ConnectionInfo>() {
        public ConnectionInfo createFromParcel(Parcel source) {
            return new ConnectionInfo(source);
        }

        public ConnectionInfo[] newArray(int size) {
            return new ConnectionInfo[size];
        }
    };

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

    public ConnectionInfo(String name, String host, int port, String userName, String password, String database) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.database = database;
    }
}
