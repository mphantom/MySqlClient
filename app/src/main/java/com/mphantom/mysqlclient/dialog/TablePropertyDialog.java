package com.mphantom.mysqlclient.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.mphantom.mysqlclient.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-12.
 */
public class TablePropertyDialog extends Dialog implements View.OnClickListener {
    @Bind(R.id.edit_tablePropertyDialog_file)
    EditText edit_file;
    @Bind(R.id.edit_tablePropertyDialog_type)
    EditText edit_type;
    @Bind(R.id.edit_tablePropertyDialog_default)
    EditText edit_default;
    @Bind(R.id.check_tablePropertyDialog_autoInc)
    CheckBox check_autoInc;
    @Bind(R.id.check_tablePropertyDialog_primaryKey)
    CheckBox check_primaryKey;
    @Bind(R.id.check_tablePropertyDialog_notNull)
    CheckBox check_notNull;
    @Bind(R.id.btn_tablePropertyDialog_confirm)
    Button btn_confirm;


    public TablePropertyDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_table_property);
        ButterKnife.bind(this);
        btn_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        this.dismiss();
    }
}
