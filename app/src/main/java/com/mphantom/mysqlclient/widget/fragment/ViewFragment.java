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
import android.widget.Toast;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.ItemTouchHelperCallback;
import com.mphantom.mysqlclient.adapter.TableAdapter;
import com.mphantom.mysqlclient.dialog.ViewDialog;
import com.mphantom.mysqlclient.model.Table;
import com.mphantom.mysqlclient.widget.activity.table.TableActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class ViewFragment extends BaseFragment implements View.OnClickListener {
    @Bind(R.id.recycler_viewF)
    RecyclerView recyclerView;
    @Bind(R.id.float_viewF)
    FloatingActionButton floatButton;
    @Bind(R.id.tv_viewF)
    TextView tvTip;

    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_view;
    }

    @Override
    protected boolean isButterKnife() {
        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        floatButton.setOnClickListener(this);

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
                        TableAdapter adapter = new TableAdapter(context, getView(tables));
                        adapter.setOnItemClickListener((view1, object) -> {
                            Intent intent = new Intent(context, TableActivity.class);
                            intent.putExtra("tableName", ((Table) object).getName());
                            context.startActivity(intent);

                        });
                        adapter.setOnItemLongClickListener((view2, object1) -> {
                        });
                        recyclerView.setAdapter(adapter);
                        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
                        mItemTouchHelper.attachToRecyclerView(recyclerView);
                    }, throwable -> {
                        Toast.makeText(context, R.string.connection_fail, Toast.LENGTH_SHORT).show();
                    });
        } else {
            recyclerView.setVisibility(View.GONE);
            floatButton.setVisibility(View.GONE);
            tvTip.setVisibility(View.VISIBLE);
        }

    }

    public List<Table> getView(List<Table> tableList) {
        List<Table> tablesList = new ArrayList<>();
        for (int i = 0; i < tableList.size(); i++) {
            if (tableList.get(i).getContent().contains("VIEW")) {
                tablesList.add(tableList.get(i));
            }
        }
        return tablesList;
    }

    @Override
    public void onClick(View v) {
        ViewDialog dialog = new ViewDialog(context);
        dialog.show();
    }
}