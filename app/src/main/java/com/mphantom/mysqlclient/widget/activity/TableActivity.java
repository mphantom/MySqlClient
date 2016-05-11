package com.mphantom.mysqlclient.widget.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.DataAdapter;
import com.mphantom.mysqlclient.model.TableProperty;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.schedulers.Schedulers;

public class TableActivity extends AppCompatActivity {
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.recycler_tableA)
    RecyclerView recyclerView;

    private List<TableProperty> listProperty;
    private List<Map<String, Object>> listdata;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        String tableName = intent.getStringExtra("tableName");
        toolbar.setTitle(tableName);
        Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(aLong -> {
                    listProperty = App.getInstance().connectionService.schema(tableName);
                    listdata = App.getInstance().connectionService.queryAll(tableName, 1, 10);
                })
                .subscribe(aLong1 -> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(TableActivity.this));
                    DataAdapter adapter = new DataAdapter(TableActivity.this, listProperty, listdata);
//                    adapter.setOnItemClickListener((view1, object) -> {
//                        Intent intent = new Intent(TableActivity.this, TableActivity.class);
//                        intent.putExtra("tableName", ((Table) object).getName());
//                        TableActivity.this.startActivity(intent);
//                    });
                });
    }

}
