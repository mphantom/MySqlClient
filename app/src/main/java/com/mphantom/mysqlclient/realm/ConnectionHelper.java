package com.mphantom.mysqlclient.realm;

import com.mphantom.mysqlclient.model.ConnectionInfo;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by wushaorong on 16-5-10.
 */
public class ConnectionHelper {
    private Realm realm;
    private static ConnectionHelper instance;

    public static ConnectionHelper getInstance() {
        if (instance == null) {
            instance = new ConnectionHelper();
        }
        return instance;
    }

    private ConnectionHelper() {
        realm = RealmHelper.getInstance().getRealm();
    }

    public ConnectionInfo find(String name) {
        return realm.where(ConnectionInfo.class).equalTo("name", name).findFirst();
    }

    public List<ConnectionInfo> findAll() {
        RealmResults<ConnectionInfo> results = realm.where(ConnectionInfo.class).findAll();
        return results.subList(0, results.size());
    }


    public RealmResults<ConnectionInfo> findAllRealmResults() {
        return realm.where(ConnectionInfo.class).findAll();

    }

    public List<ConnectionInfo> find(int start, int end) {
        RealmResults<ConnectionInfo> results = realm.where(ConnectionInfo.class).findAll();
        return results.subList(start, end);
    }


    public void insert(ConnectionInfo connectionInfo) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(connectionInfo);
        realm.commitTransaction();
    }


    public void update(String name, ConnectionInfo connectionInfo) {
        realm.beginTransaction();
        ConnectionInfo connection = find(name);
        connection.setName(connectionInfo.getName());
        connection.setHost(connectionInfo.getHost());
        connection.setPort(connectionInfo.getPort());
        connection.setUserName(connectionInfo.getUserName());
        connection.setDatabase(connectionInfo.getDatabase());
        connection.setPassword(connectionInfo.getPassword());
        realm.commitTransaction();
    }

    public void update(ConnectionInfo connectionInfo) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(connectionInfo);
        realm.commitTransaction();

    }


    public void delete(String name) {
        realm.beginTransaction();
        RealmResults<ConnectionInfo> results = realm.where(ConnectionInfo.class).equalTo("name", name).findAll();
        results.clear();
        realm.commitTransaction();
    }

    public void deleteAll() {
        realm.beginTransaction();
        RealmResults<ConnectionInfo> results = realm.where(ConnectionInfo.class).findAll();
        results.clear();
        realm.commitTransaction();
    }
}
