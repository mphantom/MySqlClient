package com.mphantom.mysqlclient.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.TableProperty;
import com.mphantom.mysqlclient.utils.OnConfirm;
import com.mphantom.mysqlclient.utils.PropertyTypeHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-12.
 */
public class TablePropertyDialog extends Dialog implements View.OnClickListener {
    @BindView(R.id.edit_tablePropertyDialog_file)
    EditText edit_file;
    @BindView(R.id.edit_tablePropertyDialog_type)
    AutoCompleteTextView edit_type;
    @BindView(R.id.edit_tablePropertyDialog_default)
    EditText edit_default;
    @BindView(R.id.check_tablePropertyDialog_autoInc)
    CheckBox check_autoInc;
    @BindView(R.id.check_tablePropertyDialog_primaryKey)
    CheckBox check_primaryKey;
    @BindView(R.id.check_tablePropertyDialog_notNull)
    CheckBox check_notNull;
    @BindView(R.id.btn_tablePropertyDialog_confirm)
    Button btn_confirm;

    private OnConfirm onConfirm;
    private TableProperty oldPerty;

    public TablePropertyDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_table_property);
        ButterKnife.bind(this);
        edit_type.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, PropertyTypeHelper.TYPE));
        edit_type.setThreshold(1);
        btn_confirm.setOnClickListener(this);
        check_primaryKey.setOnClickListener(v1 -> {
            if (!check_primaryKey.isChecked()) {
                if (oldPerty == null || TextUtils.isEmpty(oldPerty.getKey()))
                    check_autoInc.setChecked(false);
            }
        });
        check_autoInc.setOnClickListener(v -> {
            if (check_autoInc.isChecked() && !check_primaryKey.isChecked()) {
                if (oldPerty == null || TextUtils.isEmpty(oldPerty.getKey())) {
                    check_autoInc.setChecked(false);
                }
            }
        });
        if (oldPerty != null) {
            edit_file.setText(oldPerty.getField());
            edit_type.setText(oldPerty.getType());
            edit_default.setText(oldPerty.get_default());
            check_autoInc.setChecked(oldPerty.isAutoIncrement());
            check_primaryKey.setChecked(oldPerty.isPrimary());
            check_notNull.setChecked(!oldPerty.isNullable());
        }
    }

    @Override
    public void onClick(View v) {
        TableProperty tableProperty = new TableProperty();
        tableProperty.setField(edit_file.getText().toString().trim());
        tableProperty.setType(edit_type.getText().toString().trim());
        tableProperty.set_default(edit_default.getText().toString().trim());
        tableProperty.setPrimary(check_primaryKey.isChecked());
        tableProperty.setAutoIncrement(check_autoInc.isChecked());
        tableProperty.setNullable(!check_notNull.isChecked());
        if (onConfirm != null)
            onConfirm.OnButtonConfirm(tableProperty);
        this.dismiss();
    }

    public void setOnConfirm(OnConfirm confirm) {
        this.onConfirm = confirm;
    }

    public void setTableProperty(TableProperty property) {
        this.oldPerty = property;
    }
}
