package com.mphantom.mysqlclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.core.SqlConnection;
import com.mphantom.mysqlclient.model.Table;
import com.mphantom.mysqlclient.model.TableProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<Table> showTables() {
        if (sqlConnection != null)
            return sqlConnection.showTables();
        return null;
    }

    public void useDb(String dbname) {
        if (sqlConnection != null)
            sqlConnection.useDb(dbname);
    }

    public List<TableProperty> schema(String tablename) {
        if (sqlConnection != null)
            return sqlConnection.schema(tablename);
        return null;
    }

    public List<Map<String, Object>> queryAll(String tablename, int page, int pagesize) {
        if (sqlConnection != null)
            return sqlConnection.queryAll(tablename, page, pagesize);
        return null;
    }

    public int queryItemCount(String tablename) {
        if (sqlConnection != null)
            return sqlConnection.queryItemCount(tablename);
        return -1;
    }

    public HashMap<String, Object> dosql(String sql) {
        if (sqlConnection != null)
            return sqlConnection.dosql(sql);
        return null;
    }

    public void deleteTable(String tableName) {
        if (sqlConnection != null)
            sqlConnection.deleteTable(tableName);
    }

    public void createTable(String tablename, List<TableProperty> list) {
        if (sqlConnection != null)
            sqlConnection.createTable(tablename, list);
    }
}
