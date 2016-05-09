package com.mphantom.mysqlclient;

import android.app.Application;

/**
 * Created by wushaorong on 16-5-10.
 */
public class App extends Application {
    private static App instance;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
