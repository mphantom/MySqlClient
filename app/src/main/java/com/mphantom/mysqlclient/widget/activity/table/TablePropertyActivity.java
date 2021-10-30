package com.mphantom.mysqlclient.widget.activity.table;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.ItemTouchHelperCallback;
import com.mphantom.mysqlclient.adapter.OnItemClickListener;
import com.mphantom.mysqlclient.adapter.OnItemDismissListener;
import com.mphantom.mysqlclient.adapter.TablePropertyAdapter;
import com.mphantom.mysqlclient.dialog.TablePropertyDialog;
import com.mphantom.mysqlclient.model.TableProperty;
import com.mphantom.mysqlclient.utils.OnConfirm;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class TablePropertyActivity extends AppCompatActivity
        implements View.OnClickListener, OnConfirm, OnItemDismissListener, OnItemClickListener {
    @BindView(R.id.btn_confirm_tablePropertyA)
    Button btn_confirm;
    @BindView(R.id.recycler_tablePropertyA)
    RecyclerView recyclerView;
    @BindView(R.id.edit_tablename_tablePropertyA)
    EditText edit_tableName;
    @BindView(R.id.float_tablePropertyA)
    FloatingActionButton float_btn;

    private String tableName;
    private boolean newTable;
    private TablePropertyAdapter adapter;
    private List<TableProperty> tablePropertyList;
    private List<String> alertSqls;
    private ItemTouchHelper mItemTouchHelper;
    private boolean newProperty;
    private TableProperty oldTableProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_property);
        ButterKnife.bind(this);
        btn_confirm.setOnClickListener(this);
        float_btn.setOnClickListener(this);
        Intent intent = getIntent();
        newTable = intent.getBooleanExtra("newTable", true);
        tableName = intent.getStringExtra("tableName");
        setTitle(newTable ? "新建表结构" : "修改表结构");
        recyclerView.setLayoutManager(new LinearLayoutManager(TablePropertyActivity.this));
        tablePropertyList = new ArrayList<>();
        adapter = new TablePropertyAdapter(TablePropertyActivity.this, tablePropertyList);
        adapter.setOnItemDismissListener(this);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        alertSqls = new ArrayList<>();
        if (!newTable) {
            Observable.create((Observable.OnSubscribe<Integer>) subscriber -> subscriber.onNext(1))
                    .subscribeOn(Schedulers.io())
                    .doOnNext(integer -> {
                        tablePropertyList.addAll(App.getInstance().connectionService.schema(tableName));
                        App.getInstance().tablePropertyList = tablePropertyList;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong1 -> {
                        adapter.notifyDataSetChanged();
//                        adapter = new TablePropertyAdapter(TablePropertyActivity.this, tablePropertyList);
//                        recyclerView.setAdapter(adapter);
                    });
            edit_tableName.setText(tableName);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.float_tablePropertyA:
                TablePropertyDialog dialog = new TablePropertyDialog(this);
                newProperty = true;
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
                                String newTableName = edit_tableName.getText().toString();
                                if (!newTableName.equals(tableName))
                                    alertSqls.add(new StringBuffer().append("ALTER TABLE ").append(tableName).append(" RENAME TO ").append(newTableName).toString());
                                App.getInstance().connectionService.alterTable(alertSqls);
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
        if (newProperty) {
            tablePropertyList.add(tableProperty);
            alertSqls.add(tableProperty.addTable(tableName));
        } else {
            alertSqls.addAll(tableProperty.alertTable(tableName, oldTableProperty));
            Log.i("testforthealertTable", "on");
            for (String s : alertSqls)
                Log.i("testforthealertTable", s);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnItemDismiss(Object object) {
        alertSqls.add("ALTER TABLE " + tableName + " DROP " + ((TableProperty) object).getField());
    }

    @Override
    public void OnItemClick(View view, Object object) {
        TablePropertyDialog dialog = new TablePropertyDialog(this);
        newProperty = false;
        dialog.setOnConfirm(this);
        oldTableProperty = (TableProperty) object;
        dialog.setTableProperty(oldTableProperty);
        dialog.show();
    }
}
