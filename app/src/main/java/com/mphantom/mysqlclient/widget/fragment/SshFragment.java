package com.mphantom.mysqlclient.widget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.mphantom.mysqlclient.R;

public class SshFragment extends BaseFragment {


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ssh;
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
