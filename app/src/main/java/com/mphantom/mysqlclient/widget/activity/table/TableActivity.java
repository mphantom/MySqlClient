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

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.DataAdapter;
import com.mphantom.mysqlclient.adapter.ItemTouchHelperCallback;
import com.mphantom.mysqlclient.model.TableProperty;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TableActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.toolbar_tableA)
    Toolbar toolbar;
    @Bind(R.id.recycler_tableA)
    RecyclerView recyclerView;
    @Bind(R.id.fab_tableA)
    FloatingActionButton floatButton;

    private List<TableProperty> listProperty;
    private List<Map<String, Object>> listdata;
    private String tableName;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        tableName = intent.getStringExtra("tableName");
        floatButton.setOnClickListener(this);
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
                    DataAdapter adapter = new DataAdapter(TableActivity.this, listProperty, listdata);
                    recyclerView.setAdapter(adapter);
                    adapter.setOnItemClickListener((view1, object) -> {
                        Intent inten = new Intent(TableActivity.this, TableColumnActivity.class);
                        intent.putExtra("tableName", tableName);
                        intent.putExtra("newColume", false);
                        startActivity(inten);
                    });
                    mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
                    mItemTouchHelper.attachToRecyclerView(recyclerView);
                });

    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle("表：" + tableName);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, TableColumnActivity.class);
        intent.putExtra("tableName", tableName);
        intent.putExtra("newColume", true);
        startActivity(intent);
    }
}
