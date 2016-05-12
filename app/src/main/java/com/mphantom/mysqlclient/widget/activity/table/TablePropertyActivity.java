package com.mphantom.mysqlclient.widget.activity.table;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.TablePropertyAdapter;
import com.mphantom.mysqlclient.dialog.TablePropertyDialog;
import com.mphantom.mysqlclient.model.TableProperty;
import com.mphantom.mysqlclient.utils.OnConfirm;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class TablePropertyActivity extends AppCompatActivity implements View.OnClickListener, OnConfirm {
    @Bind(R.id.btn_confirm_tablePropertyA)
    Button btn_confirm;
    @Bind(R.id.recycler_tablePropertyA)
    RecyclerView recyclerView;
    @Bind(R.id.edit_tablename_tablePropertyA)
    EditText edit_tableName;
    @Bind(R.id.float_tablePropertyA)
    FloatingActionButton float_btn;

    private String tableName;
    private boolean newTable;
    private TablePropertyAdapter adapter;
    private List<TableProperty> tablePropertyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_property);
        ButterKnife.bind(this);
        btn_confirm.setOnClickListener(this);
        float_btn.setOnClickListener(this);
        Intent intent = getIntent();
        tableName = intent.getStringExtra("tableName");
        newTable = intent.getBooleanExtra("newTable", true);
        setTitle(newTable ? "新建表结构" : "修改表结构");
        recyclerView.setLayoutManager(new LinearLayoutManager(TablePropertyActivity.this));
        if (!newTable) {
            Observable.create((Observable.OnSubscribe<Integer>) subscriber -> subscriber.onNext(1))
                    .subscribeOn(Schedulers.io())
                    .doOnNext(integer -> {
                        tablePropertyList = App.getInstance().connectionService.schema(tableName);
                        App.getInstance().tablePropertyList = tablePropertyList;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong1 -> {
                        adapter = new TablePropertyAdapter(TablePropertyActivity.this, tablePropertyList);
                        recyclerView.setAdapter(adapter);
                    });
            edit_tableName.setText(tableName);
        } else {
            tablePropertyList = new ArrayList<>();
            adapter = new TablePropertyAdapter(TablePropertyActivity.this, tablePropertyList);
            recyclerView.setAdapter(adapter);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.float_tablePropertyA:
                TablePropertyDialog dialog = new TablePropertyDialog(this);
                dialog.setOnConfirm(this);
                dialog.show();
                break;
            case R.id.btn_confirm_tablePropertyA:
                if (TextUtils.isEmpty(edit_tableName.getText()) || adapter.getItemCount() <= 1)
                    return;
                Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                        .subscribeOn(Schedulers.io())
                        .doOnNext(integer -> {
                            if (newTable) {
                                App.getInstance().connectionService.createTable(edit_tableName.getText().toString(), tablePropertyList);
                            } else {

                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong1 -> {
                            finish();
                        });
                break;
            default:
                break;
        }
    }

    @Override
    public void OnButtonConfirm(Object object) {
        TableProperty tableProperty = (TableProperty) object;
        if (newTable) {
            tablePropertyList.add(tableProperty);
        } else {

        }
        adapter.notifyDataSetChanged();
    }
}
