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

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TableColumnActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.btn_submit_tableColumnA)
    Button btn_submit;
    @Bind(R.id.layout_contant_tableColumnA)
    LinearLayout layout_contant;
    private List<TableProperty> list;
    private List<EditText> listEdit;
    private String tableName;
    private boolean newColume;
    private String name;
    private String value;

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
        listEdit = new ArrayList<>();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public void onClick(View v) {
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
        Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> {
                    String sql = new StringBuffer().append("INSERT  INTO ")
                            .append(tableName).append("(")
                            .append(name).append(")")
                            .append(" VALUES ").append("(")
                            .append(value).append(")").toString();
                    Log.i("testforinsert", sql);
                    App.getInstance().connectionService.insertInto(sql);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> finish());
    }
}
