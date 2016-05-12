package com.mphantom.mysqlclient.widget.activity.table;

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
import com.mphantom.mysqlclient.model.TableProperty;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class TablePropertyActivity extends AppCompatActivity implements View.OnClickListener {
    @Bind(R.id.btn_confirm_tablePropertyA)
    Button btn_confirm;
    @Bind(R.id.recycler_tablePropertyA)
    RecyclerView recyclerView;
    @Bind(R.id.edit_tablename_tablePropertyA)
    EditText edit_tableName;
    @Bind(R.id.float_tablePropertyA)
    FloatingActionButton float_btn;

    private String tableName;
    private List<TableProperty> tablePropertyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_property);
        ButterKnife.bind(this);
        setTitle("新建/修改表结构");

        tableName = getIntent().getStringExtra("tableName");
        btn_confirm.setOnClickListener(this);
        float_btn.setOnClickListener(this);
//        recyclerView.setLayoutManager(new LinearLayoutManager(TablePropertyActivity.this));
//        TablePropertyAdapter adapter = new TablePropertyAdapter(TablePropertyActivity.this, null);
//        recyclerView.setAdapter(adapter);
        if (!TextUtils.isEmpty(tableName)) {
            Observable.timer(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .doOnNext(aLong -> {
                        tablePropertyList = App.getInstance().connectionService.schema(tableName);
                        App.getInstance().tablePropertyList = tablePropertyList;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong1 -> {

                        recyclerView.setLayoutManager(new LinearLayoutManager(TablePropertyActivity.this));
                        TablePropertyAdapter adapter = new TablePropertyAdapter(TablePropertyActivity.this, tablePropertyList);
                        recyclerView.setAdapter(adapter);
                    });
            edit_tableName.setText(tableName);
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.float_tablePropertyA:
                break;
            case R.id.btn_confirm_tablePropertyA:
                break;
            default:
                break;
        }
    }
}
