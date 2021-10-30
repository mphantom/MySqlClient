package com.mphantom.mysqlclient.widget.activity.table;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.TableProperty;
import com.mphantom.mysqlclient.utils.PropertyTypeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TableColumnActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.btn_submit_tableColumnA)
    Button btn_submit;
    @BindView(R.id.layout_contant_tableColumnA)
    LinearLayout layout_contant;
    private List<TableProperty> list;
    private List<EditText> listEdit;
    private String tableName;
    private boolean newColume;
    private String name;
    private String value;
    private String newValue;
    private String olddefine;
    private Map<String, Object> oldData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_column);
        ButterKnife.bind(this);
        btn_submit.setOnClickListener(this);
        Intent intent = getIntent();
        tableName = intent.getStringExtra("tableName");
        newColume = intent.getBooleanExtra("newColume", true);
        setTitle(newColume ? "新建行数据" : "修改行数据");
        if (!newColume) {
            oldData = (Map<String, Object>) intent.getSerializableExtra("data");
        }
        listEdit = new ArrayList<>();
        list = App.getInstance().tablePropertyList;
        for (int i = 0; i < list.size(); i++) {
            TextInputLayout layout = new TextInputLayout(this);
            EditText editText = new EditText(this);
            editText.setHint(list.get(i).getField());
            if (PropertyTypeHelper.checkPropertyType(list.get(i).getType()) == PropertyTypeHelper.TYPE_NUMBER) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
            }
            listEdit.add(editText);
            layout.addView(editText);
            layout_contant.addView(layout);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!newColume) {
            for (int i = 0; i < listEdit.size(); i++) {
                Object object = oldData.get(list.get(i).getField());
                if (object != null)
                    listEdit.get(i).setText(object.toString());
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (newColume) {
            StringBuilder sb_name = new StringBuilder();
            StringBuilder sb_value = new StringBuilder();
            for (int i = 0; i < listEdit.size(); i++) {
                if (!TextUtils.isEmpty(listEdit.get(i).getText())) {
                    sb_name.append(list.get(i).getField()).append(",");
                    sb_value.append("\"").append(listEdit.get(i).getText()).append("\",");
                }
            }
            sb_name.deleteCharAt(sb_name.length() - 1);
            sb_value.deleteCharAt(sb_value.length() - 1);
            name = sb_name.toString();
            value = sb_value.toString();
        } else {
            StringBuilder sb_newValue = new StringBuilder();
            StringBuilder sb_oldDefine = new StringBuilder();
            for (int i = 0; i < listEdit.size(); i++) {
                String field = list.get(i).getField();
                Object object = oldData.get(field);
                if (object != null)
                    sb_oldDefine.append(field).append("=\"").append(object.toString()).append("\"").append(" AND ");
                sb_newValue.append(field).append("=\"").append(listEdit.get(i).getText()).append("\"").append(",");
            }
            newValue = sb_newValue.substring(0, sb_newValue.length() - 1);
            olddefine = sb_oldDefine.substring(0, sb_oldDefine.length() - 4);
        }
        Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> {
                    if (newColume) {
                        String sql = new StringBuffer().append("INSERT  INTO ")
                                .append(tableName).append("(")
                                .append(name).append(")")
                                .append(" VALUES ").append("(")
                                .append(value).append(")").toString();
                        Log.i("testforinsert", sql);
                        App.getInstance().connectionService.insertInto(sql);
                    } else {
                        String sql = new StringBuffer().append("UPDATE ")
                                .append(tableName).append(" SET ")
                                .append(newValue)
                                .append(" WHERE ")
                                .append(olddefine)
                                .toString();
                        Log.i("testforUpdate", sql);
                        App.getInstance().connectionService.update(sql);
                    }

                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> finish());
    }
}
