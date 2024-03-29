package com.mphantom.mysqlclient.widget.activity.table;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.DataAdapter;
import com.mphantom.mysqlclient.adapter.ItemTouchHelperCallback;
import com.mphantom.mysqlclient.model.TableProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TableActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.toolbar_tableA)
    Toolbar toolbar;
    @BindView(R.id.recycler_tableA)
    RecyclerView recyclerView;
    @BindView(R.id.fab_tableA)
    FloatingActionButton floatButton;
    @BindView(R.id.edit_TableA_confition)
    EditText editText;
    @BindView(R.id.btn_tableA)
    Button btnSubmit;


    private List<TableProperty> listProperty;
    private List<Map<String, Object>> listdata;
    private String tableName;
    private ItemTouchHelper mItemTouchHelper;
    DataAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Intent fromIntent = getIntent();
        tableName = fromIntent.getStringExtra("tableName");
        floatButton.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);


    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle("表：" + tableName);
        Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                .subscribeOn(Schedulers.io())
                .doOnNext(aLong -> {
                    listProperty = App.getInstance().connectionService.schema(tableName);
                    App.getInstance().tablePropertyList = listProperty;
                    listdata = App.getInstance().connectionService.queryAll(tableName, 1, 10);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong1 -> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(TableActivity.this));
                    adapter = new DataAdapter(TableActivity.this, listProperty, listdata);
                    recyclerView.setAdapter(adapter);
                    adapter.setTableName(tableName);
                    adapter.setOnItemClickListener((view1, object) -> {
                        Intent intent = new Intent(TableActivity.this, TableColumnActivity.class);
                        intent.putExtra("tableName", tableName);
                        intent.putExtra("newColume", false);
                        intent.putExtra("data", (HashMap<String, Object>) object);
                        startActivity(intent);
                    });
                    mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
                    mItemTouchHelper.attachToRecyclerView(recyclerView);
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recycler_tableA:
                Intent intent = new Intent(this, TableColumnActivity.class);
                intent.putExtra("tableName", tableName);
                intent.putExtra("newColume", true);
                startActivity(intent);
                break;
            case R.id.btn_tableA:
                String conditon = editText.getText().toString().trim();
                Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                        .subscribeOn(Schedulers.io())
                        .map(integer -> App.getInstance().connectionService.queryWithCondition(tableName, conditon))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(list -> {
                            listdata.clear();
                            listdata.addAll(list);
                            adapter.notifyDataSetChanged();
                        });

                break;
        }

    }


}
