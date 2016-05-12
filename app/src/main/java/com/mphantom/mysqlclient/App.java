package com.mphantom.mysqlclient;

import android.app.Application;
import android.content.Intent;

import com.mphantom.mysqlclient.core.SqlConnection;
import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.model.TableProperty;
import com.mphantom.mysqlclient.service.ConnectionService;
import com.mphantom.mysqlclient.utils.Constant;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-10.
 */
public class App extends Application {
    private static App instance;
    public ConnectionService connectionService;
    public ConnectionInfo connectionInfo;
    public List<TableProperty> tablePropertyList;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        startService(new Intent(this, ConnectionService.class));
        Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(aLong -> {
                    connectionService.setSqlConnection(new SqlConnection(
                            new ConnectionInfo("test", Constant.DEFAULT_HOST,
                                    Constant.DEFAULT_PORT, Constant.DEFAULT_USER,
                                    Constant.DEFAULT_PASSWORD, Constant.DEFAULT_DATABASE)));
                }, Throwable::printStackTrace);
    }

    public void setConnectionInfo(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
    }
}

