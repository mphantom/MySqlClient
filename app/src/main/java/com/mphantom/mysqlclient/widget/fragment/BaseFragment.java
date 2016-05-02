package com.mphantom.mysqlclient.widget.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-4-30.
 */
public abstract class BaseFragment extends Fragment {
    protected View rootView;
    protected Context context;

    @CallSuper
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(getLayoutId(), container, false);
        if (isButterKnife())
            ButterKnife.bind(this, rootView);
        return rootView;
    }

    @LayoutRes
    protected abstract int getLayoutId();

    protected abstract boolean isButterKnife();

    @CallSuper
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected View viewById(@IdRes int resId) {
        return rootView.findViewById(resId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
