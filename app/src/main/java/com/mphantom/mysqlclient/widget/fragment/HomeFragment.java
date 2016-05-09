package com.mphantom.mysqlclient.widget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.DataBaseAdapter;
import com.mphantom.mysqlclient.core.SqlConnection;
import com.mphantom.mysqlclient.dialog.DataBaseDialog;
import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.utils.Constant;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-2.
 */
public class HomeFragment extends BaseFragment {
    @Bind(R.id.recycler_homeF)
    RecyclerView recyclerView;
    @Bind(R.id.float_homeF)
    FloatingActionButton floatButton;


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
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new DataBaseAdapter(context));
        floatButton.setOnClickListener(v -> {
            DataBaseDialog dialog = new DataBaseDialog();
            dialog.show(getFragmentManager(), "databaseDialg");
        });
        Observable.timer(1, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(aLong -> {

                    SqlConnection sql = new SqlConnection(new ConnectionInfo("test", Constant.DEFAULT_HOST, Constant.DEFAULT_PORT, Constant.DEFAULT_USER, Constant.DEFAULT_PASSWORD, Constant.DEFAULT_DATABASE));
                    Log.i("testforme", sql.showTables().toString());
                    Log.i("testforme", sql.queryItemCount("test") + "");
                });
    }
}
