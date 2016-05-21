package com.mphantom.mysqlclient.widget.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.ConsoleAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-21.
 */
public class SshActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.recycler_SSHA)
    RecyclerView recyclerview;
    @Bind(R.id.edit_SSHA_input)
    EditText edit_input;
    @Bind(R.id.btn_SSHA_submit)
    Button btn_submit;
    private ConsoleAdapter adapter;
    private List<String> lists;
    private String username;
    private String password;
    private int port;
    private String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ssh);
        ButterKnife.bind(this);
        Intent fromIntent = getIntent();
        password = fromIntent.getStringExtra("password");
        username = fromIntent.getStringExtra("username");
        port = fromIntent.getIntExtra("port", 0);
        host = fromIntent.getStringExtra("host");
        btn_submit.setOnClickListener(this);
        lists = App.getInstance().sshService.list;
        adapter = new ConsoleAdapter(this, lists);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAdapter(adapter);
        Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> {
                    App.getInstance().sshService.setConnectedInfo(host, port, username, password);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> {
                    Toast.makeText(this, R.string.ssh_connection_success, Toast.LENGTH_SHORT).show();
                }, throwable -> {
                    Toast.makeText(this, R.string.ssh_connection_fauli, Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_SSHA_submit) {
            String s = edit_input.getText().toString();
            App.getInstance().sshService.list.add(s);
            edit_input.setText("");
            adapter.notifyDataSetChanged();
            Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                    .subscribeOn(Schedulers.io())
                    .doOnNext(integer1 -> App.getInstance().sshService.exect(s))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> adapter.notifyDataSetChanged());
        }

    }

}
