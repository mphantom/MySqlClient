package com.mphantom.mysqlclient.widget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.ConnectionAdapter;
import com.mphantom.mysqlclient.adapter.ItemTouchHelperCallback;
import com.mphantom.mysqlclient.adapter.OnItemClickListener;
import com.mphantom.mysqlclient.adapter.OnItemLongClickListener;
import com.mphantom.mysqlclient.core.SqlConnection;
import com.mphantom.mysqlclient.dialog.ConnectionDialog;
import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.utils.Constant;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-2.
 */
public class HomeFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener {
    @Bind(R.id.recycler_homeF)
    RecyclerView recyclerView;
    @Bind(R.id.float_homeF)
    FloatingActionButton floatButton;
    private ConnectionAdapter adapter;
    private ItemTouchHelper mItemTouchHelper;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    protected boolean isButterKnife() {
        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new ConnectionAdapter(context);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        floatButton.setOnClickListener(v -> {
            ConnectionDialog dialog = new ConnectionDialog(context);
            dialog.show();
        });

    }

    @Override
    public void OnItemClick(View view, Object object) {

    }

    @Override
    public void OnItemLongClick(View view, Object object) {

    }
}
