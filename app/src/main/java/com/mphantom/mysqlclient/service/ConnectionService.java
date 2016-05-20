package com.mphantom.mysqlclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.core.SqlConnection;
import com.mphantom.mysqlclient.model.Function;
import com.mphantom.mysqlclient.model.Table;
import com.mphantom.mysqlclient.model.TableProperty;
import com.mphantom.mysqlclient.model.Trigger;

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

    public boolean isConnectToSQL() {
        return sqlConnection != null;
    }

    public void setSqlConnection(SqlConnection sqlConnection) {
        if (this.sqlConnection != null) {
            sqlConnectionClose();
        }
        this.sqlConnection = sqlConnection;
    }

    public void sqlConnectionClose() {
        if (sqlConnection != null)
            sqlConnection.close();
        sqlConnection = null;
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

    public List<Map<String, Object>> queryWithCondition(String tablename, String condition) {
        if (sqlConnection != null)
            return sqlConnection.queryWithCondition(tablename, condition);
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

    public void alterTable(List<String> sqls) {
        if (sqlConnection != null)
            sqlConnection.alterTable(sqls);
    }

    public void insertInto(String sql) {
        if (sqlConnection != null)
            sqlConnection.insertInto(sql);
    }

    public void deleteFrom(String sql) {
        if (sqlConnection != null)
            sqlConnection.deleteFrom(sql);
    }

    public void update(String sql) {
        if (sqlConnection != null)
            sqlConnection.update(sql);
    }

    public List<Trigger> showTriggers() {
        if (sqlConnection != null)
            return sqlConnection.showTriggers();
        return null;
    }

    public List<Function> showFunctions() {
        if (sqlConnection != null)
            return sqlConnection.showFunctions();
        return null;
    }

    public void deleteTrigger(String trigger) {
        if (sqlConnection != null)
            sqlConnection.deleteTrigger(trigger);
    }

    public void deleteFunction(String name) {
        if (sqlConnection != null)
            sqlConnection.deleteFunction(name);
    }

    public void deleteView(String name) {
        if (sqlConnection != null)
            sqlConnection.deleteView(name);
    }

    public void createTrigger(Trigger trigger) {
        if (sqlConnection != null)
            sqlConnection.createTrigger(trigger);
    }

    public void createFunction(Function info) {
        if (sqlConnection != null)
            sqlConnection.createFunction(info);
    }

    public void createView(String name, String viewInfo) {
        if (sqlConnection != null)
            sqlConnection.createView(name, viewInfo);
    }
}
