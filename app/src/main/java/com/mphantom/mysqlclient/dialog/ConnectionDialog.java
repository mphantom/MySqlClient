package com.mphantom.mysqlclient.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.realm.ConnectionHelper;
import com.mphantom.mysqlclient.utils.Constant;
import com.mphantom.mysqlclient.utils.OnConfirm;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-11.
 */
public class ConnectionDialog extends Dialog implements View.OnClickListener {
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

    private ConnectionInfo connectionInfo;
    private OnConfirm onConfirm;

    public ConnectionDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_database);
        ButterKnife.bind(this);
        btn_confirm.setOnClickListener(this);
        if (connectionInfo == null) {
            edit_host.setText(Constant.DEFAULT_HOST);
            edit_port.setText(Constant.DEFAULT_PORT + "");
            edit_user.setText(Constant.DEFAULT_USER);
            edit_password.setText(Constant.DEFAULT_PASSWORD);
            edit_database.setText(Constant.DEFAULT_DATABASE);
        } else {
            edit_nick.setText(connectionInfo.getName());
            edit_host.setText(connectionInfo.getHost());
            edit_port.setText(connectionInfo.getPort() + "");
            edit_user.setText(connectionInfo.getUserName());
            edit_password.setText(connectionInfo.getPassword());
            edit_database.setText(connectionInfo.getDatabase());
        }
    }

    @Override
    public void onClick(View v) {
        ConnectionInfo info = new ConnectionInfo();
        info.setUuid(UUID.randomUUID().toString());
        info.setName(edit_nick.getText().toString().trim());
        info.setHost(edit_host.getText().toString().trim());
        info.setPort(Integer.parseInt(edit_port.getText().toString().trim()));
        info.setUserName(edit_user.getText().toString());
        info.setDatabase(edit_database.getText().toString());
        info.setPassword(edit_password.getText().toString());
        ConnectionHelper.getInstance().insert(info);
        this.dismiss();
        if (onConfirm != null) {
            onConfirm.OnButtonConfirm(new Object());
        }
    }

    public void setConnectionInfo(ConnectionInfo info) {
        this.connectionInfo = info;
    }

    public void setOnConfirm(OnConfirm confirm) {
        this.onConfirm = confirm;
    }
}
