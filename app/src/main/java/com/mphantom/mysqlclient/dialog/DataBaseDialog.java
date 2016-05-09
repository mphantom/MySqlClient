package com.mphantom.mysqlclient.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.ConnectionInfo;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-3.
 */
public class DataBaseDialog extends DialogFragment {
    @Bind(R.id.edit_databaseDialgo_nick)
    EditText edit_nick;
    @Bind(R.id.edit_databaseDialgo_host)
     EditText edit_host;
    @Bind(R.id.edit_databaseDialgo_port)
     EditText edit_port;
    @Bind(R.id.edit_databaseDialgo_user)
     EditText edit_user;
    @Bind(R.id.edit_databaseDialgo_database)
     EditText edit_database;
    @Bind(R.id.edit_databaseDialgo_password)
     EditText edit_password;
    @Bind(R.id.btn_databaseDialog_confirm)
     Button btn_confirm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_database, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_confirm.setOnClickListener(v -> {
            ConnectionInfo info = new ConnectionInfo();
            info.setName(edit_nick.getText().toString().trim());
            info.setHost(edit_host.getText().toString().trim());
            info.setPort(Integer.parseInt(edit_port.getText().toString().trim()));
            info.setUserName(edit_user.getText().toString());
            info.setDatabase(edit_database.getText().toString());
            info.setPassword(edit_password.getText().toString());
        });
    }
}
