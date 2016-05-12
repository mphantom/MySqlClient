package com.mphantom.mysqlclient.widget.activity.table;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.TablePropertyAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_property);
        ButterKnife.bind(this);
        setTitle("修改表结构");

        tableName = getIntent().getStringExtra("tableName");
        btn_confirm.setOnClickListener(this);
        float_btn.setOnClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(TablePropertyActivity.this));
        TablePropertyAdapter adapter = new TablePropertyAdapter(TablePropertyActivity.this, null);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {

    }
}
