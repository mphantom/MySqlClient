package com.mphantom.mysqlclient.widget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.ConnectionAdapter;
import com.mphantom.mysqlclient.adapter.ItemTouchHelperCallback;
import com.mphantom.mysqlclient.adapter.OnItemClickListener;
import com.mphantom.mysqlclient.adapter.OnItemLongClickListener;
import com.mphantom.mysqlclient.core.SqlConnection;
import com.mphantom.mysqlclient.dialog.ConnectionDialog;
import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.utils.OnConfirm;
import com.mphantom.mysqlclient.widget.activity.MainActivity;

import butterknife.BindView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-2.
 */
public class HomeFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener, OnConfirm {
    @BindView(R.id.recycler_homeF)
    RecyclerView recyclerView;
    @BindView(R.id.float_homeF)
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
            dialog.setOnConfirm(this);
            dialog.show();
        });

    }

    @Override
    public void OnItemClick(View view, Object object) {
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> subscriber.onNext(1))
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> {
                    App.getInstance().connectionService.setSqlConnection(new SqlConnection((ConnectionInfo) object));
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(context, R.string.connection_success, Toast.LENGTH_SHORT).show();
                    ((MainActivity) context).setHostInfo(((ConnectionInfo) object).getName(), ((ConnectionInfo) object).getHost());
                }, throwable -> {
                    Toast.makeText(context, R.string.connection_fail, Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void OnItemLongClick(View view, Object object) {
        ConnectionDialog dialog = new ConnectionDialog(context);
        dialog.setConnectionInfo((ConnectionInfo) object);
        dialog.setOnConfirm(this);
        dialog.show();
    }

    @Override
    public void OnButtonConfirm(Object object) {
        adapter.notifyDataSetChanged();
    }
}
