package com.mphantom.mysqlclient.realm;

import com.mphantom.mysqlclient.model.SshUser;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by wushaorong on 16-5-21.
 */
public class SshUserHelper {
    private Realm realm;
    private static SshUserHelper instance;

    public static SshUserHelper getInstance() {
        if (instance == null) {
            instance = new SshUserHelper();
        }
        return instance;
    }

    private SshUserHelper() {
        realm = RealmHelper.getInstance().getRealm();
    }

    public SshUser find(String uuid) {
        return realm.where(SshUser.class).equalTo("uuid", uuid).findFirst();
    }

    public List<SshUser> findAll() {
        RealmResults<SshUser> results = realm.where(SshUser.class).findAll();
        return results.subList(0, results.size());
    }


    public RealmResults<SshUser> findAllRealmResults() {
        return realm.where(SshUser.class).findAll();

    }

    public List<SshUser> find(int start, int end) {
        RealmResults<SshUser> results = realm.where(SshUser.class).findAll();
        return results.subList(start, end);
    }


    public void insert(SshUser connectionInfo) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(connectionInfo);
        realm.commitTransaction();
    }


    public void update(String uuid, SshUser sshUserinfo) {
        realm.beginTransaction();
        SshUser sshUser = find(uuid);
        sshUser.setName(sshUserinfo.getName());
        sshUser.setHost(sshUserinfo.getHost());
        sshUser.setPort(sshUserinfo.getPort());
        sshUser.setUserName(sshUserinfo.getUserName());
        sshUser.setPassword(sshUserinfo.getPassword());
        realm.commitTransaction();
    }

    public void update(SshUser connectionInfo) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(connectionInfo);
        realm.commitTransaction();

    }


    public void delete(String uuid) {
        realm.beginTransaction();
        RealmResults<SshUser> results = realm.where(SshUser.class).equalTo("uuid", uuid).findAll();
        results.clear();
        realm.commitTransaction();
    }

    public void deleteAll() {
        realm.beginTransaction();
        RealmResults<SshUser> results = realm.where(SshUser.class).findAll();
        results.clear();
        realm.commitTransaction();
    }
}
