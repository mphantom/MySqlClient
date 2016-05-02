package com.mphantom.mysqlclient.widget.fragment;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;

import com.mphantom.mysqlclient.R;

import butterknife.Bind;

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
}
