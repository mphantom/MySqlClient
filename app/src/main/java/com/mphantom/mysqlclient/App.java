package com.mphantom.mysqlclient;

import android.app.Application;
import android.content.Intent;

import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.service.ConnectionService;

/**
 * Created by wushaorong on 16-5-10.
 */
public class App extends Application {
    private static App instance;
    public ConnectionService connectionService;
    public ConnectionInfo connectionInfo;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startService(new Intent(this, ConnectionService.class));
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}

