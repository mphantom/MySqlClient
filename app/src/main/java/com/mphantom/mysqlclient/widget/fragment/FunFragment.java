package com.mphantom.mysqlclient.widget.fragment;

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
import com.mphantom.mysqlclient.adapter.FunctionAdapter;
import com.mphantom.mysqlclient.adapter.ItemTouchHelperCallback;
import com.mphantom.mysqlclient.dialog.FunctionDialog;
import com.mphantom.mysqlclient.model.Trigger;

import butterknife.Bind;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class FunFragment extends BaseFragment implements View.OnClickListener {
    @Bind(R.id.recycler_FunF)
    RecyclerView recyclerView;
    @Bind(R.id.float_FunF)
    FloatingActionButton float_btn;
    @Bind(R.id.tv_FunF)
    TextView tvTip;

    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_fun;
    }

    @Override
    protected boolean isButterKnife() {
        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        float_btn.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (App.getInstance().connectionService.isConnectToSQL()) {
            tvTip.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            float_btn.setVisibility(View.VISIBLE);
            Observable.create((Observable.OnSubscribe<Integer>) subscriber -> subscriber.onNext(1))
                    .subscribeOn(Schedulers.io())
                    .map(aLong1 -> App.getInstance().connectionService.showTriggers())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(triggers -> {
                        recyclerView.setLayoutManager(new LinearLayoutManager(context));
                        FunctionAdapter adapter = new FunctionAdapter(context, triggers);
                        adapter.setOnItemLongClickListener((view2, object1) -> {
                            FunctionDialog dialog = new FunctionDialog(context);
                            dialog.setTrigger((Trigger) object1);
                            dialog.show();
                        });
                        recyclerView.setAdapter(adapter);
                        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
                        mItemTouchHelper.attachToRecyclerView(recyclerView);
                    });
        } else {
            tvTip.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            float_btn.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        FunctionDialog dialog = new FunctionDialog(context);
        dialog.show();
    }
}