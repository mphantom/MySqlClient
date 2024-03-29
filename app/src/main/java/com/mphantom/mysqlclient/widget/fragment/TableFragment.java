package com.mphantom.mysqlclient.widget.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.ItemTouchHelperCallback;
import com.mphantom.mysqlclient.adapter.TableAdapter;
import com.mphantom.mysqlclient.model.Table;
import com.mphantom.mysqlclient.widget.activity.table.TableActivity;
import com.mphantom.mysqlclient.widget.activity.table.TablePropertyActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-4.
 */
public class TableFragment extends BaseFragment {
    @BindView(R.id.recycler_tableF)
    RecyclerView recyclerView;
    @BindView(R.id.float_tableF)
    FloatingActionButton floatButton;
    @BindView(R.id.tv_tableF)
    TextView tvTip;

    private ItemTouchHelper mItemTouchHelper;

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
            Intent intent = new Intent(context, TablePropertyActivity.class);
            intent.putExtra("newTable", true);
            startActivity(intent);
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (App.getInstance().connectionService.isConnectToSQL()) {
            tvTip.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            floatButton.setVisibility(View.VISIBLE);
            Observable.create((Observable.OnSubscribe<Integer>) subscriber -> subscriber.onNext(1))
                    .subscribeOn(Schedulers.io())
                    .map(aLong1 -> App.getInstance().connectionService.showTables())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(tables -> {
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        TableAdapter adapter = new TableAdapter(context, getTable(tables));
                        adapter.setOnItemClickListener((view1, object) -> {
                            Intent intent = new Intent(context, TableActivity.class);
                            intent.putExtra("tableName", ((Table) object).getName());
                            context.startActivity(intent);
                        });
                        adapter.setOnItemLongClickListener((view2, object1) -> {
                            Intent intent = new Intent(context, TablePropertyActivity.class);
                            intent.putExtra("tableName", ((Table) object1).getName());
                            intent.putExtra("newTable", false);
                            context.startActivity(intent);
                        });
                        recyclerView.setAdapter(adapter);
                        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
                        mItemTouchHelper.attachToRecyclerView(recyclerView);
                    });
        } else {
            recyclerView.setVisibility(View.GONE);
            floatButton.setVisibility(View.GONE);
            tvTip.setVisibility(View.VISIBLE);
        }

    }

    public List<Table> getTable(List<Table> tableList) {
        List<Table> tablesList = new ArrayList<>();
        for (int i = 0; i < tableList.size(); i++) {
            if (tableList.get(i).getContent().contains("TABLE")) {
                tablesList.add(tableList.get(i));
            }
        }
        return tablesList;
    }
}
