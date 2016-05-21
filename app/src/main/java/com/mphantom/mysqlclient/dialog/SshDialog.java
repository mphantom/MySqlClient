package com.mphantom.mysqlclient.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.SshUser;
import com.mphantom.mysqlclient.realm.SshUserHelper;
import com.mphantom.mysqlclient.utils.OnConfirm;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-12.
 */
public class SshDialog extends Dialog implements View.OnClickListener {
    @Bind(R.id.edit_sshDialog_nick)
    EditText edit_nick;
    @Bind(R.id.edit_sshDialog_host)
    EditText edit_host;
    @Bind(R.id.edit_sshDialog_port)
    EditText edit_port;
    @Bind(R.id.edit_sshDialog_name)
    EditText edit_name;
    @Bind(R.id.edit_sshDialog_password)
    EditText edit_password;
    @Bind(R.id.btn_sshDialog_confirm)
    Button btn_confirm;
    private OnConfirm onConfirm;

    public SshDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ssh);
        ButterKnife.bind(this);
        btn_confirm.setOnClickListener(this);
    }

    public void setOnConfirm(OnConfirm onConfirm) {
        this.onConfirm = onConfirm;
    }

    @Override
    public void onClick(View v) {
        SshUser info = new SshUser();
        info.setUuid(UUID.randomUUID().toString());
        info.setName(edit_nick.getText().toString());
        info.setPort(Integer.parseInt(edit_port.getText().toString().trim()));
        info.setUserName(edit_name.getText().toString().trim());
        info.setPassword(edit_password.getText().toString().trim());
        SshUserHelper.getInstance().insert(info);
        this.dismiss();
        if (onConfirm != null) {
            onConfirm.OnButtonConfirm(new Object());
        }

    }
}