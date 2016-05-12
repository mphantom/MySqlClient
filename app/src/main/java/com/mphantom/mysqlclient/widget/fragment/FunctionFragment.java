package com.mphantom.mysqlclient.widget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.mphantom.mysqlclient.R;

import butterknife.Bind;

/**
 * Created by wushaorong on 16-5-11.
 */
public class FunctionFragment extends BaseFragment {
    @Bind(R.id.recycler_FunctionF)
    RecyclerView recyclerView;
    @Bind(R.id.float_FunctionF)
    FloatingActionButton float_btn;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_function;
    }

    @Override
    protected boolean isButterKnife() {
        return true;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
