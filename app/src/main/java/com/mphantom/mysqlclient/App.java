package com.mphantom.mysqlclient;

import android.app.Application;
import android.content.Intent;

import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.model.TableProperty;
import com.mphantom.mysqlclient.service.ConnectionService;
import com.mphantom.mysqlclient.service.SshService;

import java.util.List;

/**
 * Created by wushaorong on 16-5-10.
 */
public class App extends Application {
    private static App instance;
    public ConnectionService connectionService;
    public ConnectionInfo connectionInfo;
    public List<TableProperty> tablePropertyList;
    public SshService sshService;
//    public KnownHosts knownHosts;
//    public OutputStream stdin;
//    public InputStream stdout;
//    public InputStream stderr;


    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startService(new Intent(this, ConnectionService.class));
        startService(new Intent(this, SshService.class));
//        Observable.timer(2, TimeUnit.SECONDS)
//                .observeOn(Schedulers.io())
//                .subscribeOn(Schedulers.io())
//                .subscribe(aLong -> {
//                    connectionService.setSqlConnection(new SqlConnection(
//                            new ConnectionInfo(UUID.randomUUID().toString(), "test", Constant.DEFAULT_HOST,
//                                    Constant.DEFAULT_PORT, Constant.DEFAULT_USER,
//                                    Constant.DEFAULT_PASSWORD, Constant.DEFAULT_DATABASE)));
//                }, Throwable::printStackTrace);
//        knownHosts = new KnownHosts();
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}

