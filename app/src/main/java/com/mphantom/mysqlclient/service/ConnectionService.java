package com.mphantom.mysqlclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.core.SqlConnection;

public class ConnectionService extends Service {
    private SqlConnection sqlConnection;

    public ConnectionService() {
    }

    @Override
    public void onCreate() {
        App.getInstance().connectionService = this;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setSqlConnection(SqlConnection sqlConnection) {
        if (this.sqlConnection != null) {
            sqlConnectionClose();
        }
        this.sqlConnection = sqlConnection;
    }

    public void sqlConnectionClose() {
        sqlConnection.close();
    }
}
