package com.mphantom.mysqlclient.widget.activity;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.List;

/**
 * Created by wushaorong on 16-4-30.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected FragmentManager fragmentManager;
    protected SparseArray<Fragment> fragmentArrays;

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        fragmentManager = getSupportFragmentManager();
        fragmentArrays = new SparseArray<>();
    }

    @LayoutRes
    protected abstract int getLayoutId();

    @IdRes
    protected abstract int getReplaceLayoutId();

    protected void replaceFragment(Fragment fragment, String isBackStack) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (TextUtils.isEmpty(isBackStack)) {
            fragmentTransaction.replace(getReplaceLayoutId(), fragment);
        } else {
            fragmentTransaction.replace(getReplaceLayoutId(), fragment, isBackStack);
            fragmentTransaction.addToBackStack(isBackStack);
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    protected void hideFragments() {
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null && fragments.size() > 0) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            for (Fragment fragment : fragments) {
                fragmentTransaction.hide(fragment);
            }
            fragmentTransaction.commit();
        }
    }

    protected void addFragment(@IdRes int containerViewId, Fragment fragment, @IdRes int key, @Nullable String tag) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(containerViewId, fragment, tag).commit();
        fragmentArrays.put(key, fragment);

    }

    protected void addFragment(@IdRes int containerViewId, Fragment fragment, @IdRes int key) {
        this.addFragment(containerViewId, fragment, key, null);
    }

    protected void showFragment(@IdRes int key) {
        if (fragmentArrays.get(key) == null) {
            return;
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentArrays.get(key);
        fragmentTransaction.show(fragment).commit();
        fragment.onResume();
    }

    protected boolean isFragmentExit(@IdRes int key) {
        return fragmentArrays.get(key) != null;
    }

    protected void changeFragment(@IdRes int id, Fragment fragment, @Nullable String tag) {
        hideFragments();
        if (isFragmentExit(id)) {
            showFragment(id);
        } else {
            addFragment(getReplaceLayoutId(), fragment, id, tag);
        }
    }
}
