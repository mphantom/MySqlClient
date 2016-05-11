package com.mphantom.mysqlclient.widget.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.TableAdapter;
import com.mphantom.mysqlclient.model.Table;
import com.mphantom.mysqlclient.widget.activity.TableActivity;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-4.
 */
public class TableFragment extends BaseFragment {
    @Bind(R.id.recycler_tableF)
    RecyclerView recyclerView;
    @Bind(R.id.float_tableF)
    FloatingActionButton floatButton;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_table;
    }

    @Override
    protected boolean isButterKnife() {
        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        floatButton.setOnClickListener(v -> {
//            startActivity(new Intent(context, TableActivity.class));
        });
        Observable.timer(1, TimeUnit.SECONDS)
                .map(aLong1 -> {
                    return App.getInstance().connectionService.showTables();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tables -> {
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                    TableAdapter adapter = new TableAdapter(context, tables);
                    adapter.setOnItemClickListener((view1, object) -> {
                        Intent intent = new Intent(context, TableActivity.class);
                        intent.putExtra("tableName", ((Table) object).getName());
                        context.startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);
                });
    }
}
