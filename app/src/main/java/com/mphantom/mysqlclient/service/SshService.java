package com.mphantom.mysqlclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.jcabi.ssh.SSHByPassword;
import com.jcabi.ssh.Shell;
import com.mphantom.mysqlclient.App;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SshService extends Service {
    public List<String> list;
    private Shell shell;
    private Shell.Plain plain;

    public SshService() {
        list = new ArrayList<>();
    }

    @Override
    public void onCreate() {
        App.getInstance().sshService = this;
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

    public boolean isConnected() {
        return shell != null && plain != null;
    }

    public void setConnectedInfo(String host, int port, String user, String password) {
        try {
            shell = new SSHByPassword(host, port, user, password);
            plain = new Shell.Plain(shell);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void exect(String cmd) {
        try {
            if (isConnected()) {
                String s = plain.exec(cmd);
                list.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
