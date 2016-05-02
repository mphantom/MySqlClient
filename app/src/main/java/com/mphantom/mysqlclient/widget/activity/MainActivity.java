package com.mphantom.mysqlclient.widget.activity;

import android.os.Bundle;

import com.mphantom.mysqlclient.R;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getReplaceLayoutId() {
        return 0;
    }

}
