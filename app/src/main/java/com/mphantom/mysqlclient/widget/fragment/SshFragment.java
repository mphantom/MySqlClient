package com.mphantom.mysqlclient.widget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.jcabi.ssh.SSHByPassword;
import com.jcabi.ssh.Shell;
import com.mphantom.mysqlclient.R;

import java.io.IOException;
import java.net.UnknownHostException;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SshFragment extends BaseFragment {
//        implements ConnectionMonitor {
//    private static final int conditions = ChannelCondition.STDOUT_DATA
//            | ChannelCondition.STDERR_DATA
//            | ChannelCondition.CLOSED
//            | ChannelCondition.EOF;
//    Session session;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ssh;
    }

    @Override
    protected boolean isButterKnife() {
        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
//                .subscribeOn(Schedulers.io())
//                .map(integer -> {
//                    Connection connection = new Connection("45.78.20.91", 29222);
//                    connection.addConnectionMonitor(this);
//                    ConnectionInfo connectionInfo;
//                    try {
//                        connectionInfo = connection.connect(new HostKeyVerifier());
//                        Log.i("SSHFORTHIS", "connect");
//                        connection.authenticateWithPassword("root", "yruHpW4v7t4N");
//                        session = connection.openSession();
////                        session.requestPTY("xterm-256color", columns, rows, width, height, null);
//                        session.startShell();
//                        App.getInstance().stdin = session.getStdin();
//                        App.getInstance().stdout = session.getStdout();
//                        App.getInstance().stderr = session.getStderr();
//                        App.getInstance().stdin.write("ls -al \r".getBytes());
//                        Log.i("SSHFORTHIS", "create");
//                    } catch (IOException e) {
//                        Log.i("SSHFORTHIS", "exception");
//                        e.printStackTrace();
//                    }
//                    return " ";
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(s -> {
//                    Log.i("SSHFORTHIS", s);
//                }, Throwable::printStackTrace);
//        new Thread(new Runnable() {
//            byte[] buffer = new byte[2048];
//            int num;
//
//            @Override
//            public void run() {
//                while (true) {
//                    if (App.getInstance().stdout != null && (session.waitForCondition(conditions, 0) & ChannelCondition.STDOUT_DATA) != 0) {
//                        try {
//                            while ((num = App.getInstance().stdout.read(buffer)) != -1) {
//                                Log.i("SSHFORTHIS", Arrays.toString(buffer));
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            }
//        }).start();
        Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                .subscribeOn(Schedulers.io())
                .map(integer1 -> {
                    Shell shell;
                    try {
                        shell = new SSHByPassword("45.78.20.91", 29222, "root", "yruHpW4v7t4N");
                        String stdout = new Shell.Plain(shell).exec("ls -al");
                        return stdout;
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return " ";

                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> Log.i("testfortheshh", s));
    }

//    @Override
//    public void connectionLost(Throwable reason) {
//        Log.i("SSHFORTHIS", "connectionLost" + reason.getMessage());
//    }
//
//    public class HostKeyVerifier extends ExtendedServerHostKeyVerifier {
//        @Override
//        public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey) throws IOException {
//            KnownHosts hosts = App.getInstance().knownHosts;
//            Boolean result;
//
//            String matchName = String.format(Locale.US, "%s:%d", hostname, port);
//
//            String fingerprint = KnownHosts.createHexFingerprint(serverHostKeyAlgorithm, serverHostKey);
//
//            String algorithmName;
//            if ("ssh-rsa".equals(serverHostKeyAlgorithm))
//                algorithmName = "RSA";
//            else if ("ssh-dss".equals(serverHostKeyAlgorithm))
//                algorithmName = "DSA";
//            else if (serverHostKeyAlgorithm.startsWith("ecdsa-"))
//                algorithmName = "EC";
//            else
//                algorithmName = serverHostKeyAlgorithm;
//
//            switch (hosts.verifyHostkey(matchName, serverHostKeyAlgorithm, serverHostKey)) {
//                case KnownHosts.HOSTKEY_IS_OK:
//                    return true;
//                case KnownHosts.HOSTKEY_IS_NEW:
//                    result = true;
//                    if (result == null) return false;
//                    if (result.booleanValue()) {
//                        App.getInstance().knownHosts.addHostkey(new String[]{String.format("%s:%d", hostname, port)}, serverHostKeyAlgorithm, serverHostKey);
//                    }
//                    return result.booleanValue();
//
//                case KnownHosts.HOSTKEY_HAS_CHANGED:
//                    // Users have no way to delete keys, so we'll prompt them for now.
//                    result = true;
//                    if (result != null && result.booleanValue()) {
//                        // save this key in known database
//                        App.getInstance().knownHosts.addHostkey(new String[]{String.format("%s:%d", hostname, port)}, serverHostKeyAlgorithm, serverHostKey);
//                        return true;
//                    } else {
//                        return false;
//                    }
//                default:
//                    return false;
//            }
//        }
//
//        @Override
//        public List<String> getKnownKeyAlgorithmsForHost(String host, int port) {
//            List<String> list = new ArrayList<>();
//            list.add("ssh-rsa");
//            return list;
//        }
//
//        @Override
//        public void removeServerHostKey(String host, int port, String algorithm, byte[] hostKey) {
//            App.getInstance().knownHosts = null;
//        }
//
//        @Override
//        public void addServerHostKey(String host, int port, String algorithm, byte[] hostKey) {
//            try {
//                App.getInstance().knownHosts.addHostkey(new String[]{String.format("%s:%d", host, port)}, "ssh-rsa", hostKey);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
}
