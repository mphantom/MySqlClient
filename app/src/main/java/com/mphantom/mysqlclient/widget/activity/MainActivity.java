package com.mphantom.mysqlclient.widget.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.widget.Toolbar;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.widget.fragment.HomeFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    @Bind(R.id.navigation_mainA)
    NavigationView navigationView;
    @Bind(R.id.layout_drawer_mainA)
    DrawerLayout drawerLayout;
    @Bind(R.id.toolbar_mainA)
    Toolbar toolbar;
    private int currentIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d("MainActivity", "changeFragment");
                    item.setChecked(true);
                    toolbar.setTitle(item.getTitle());
                    currentIndex = item.getItemId();
                    changeFragment(item.getItemId(), new HomeFragment(), "HomeFragment");
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.navigation_table:
                case R.id.navigation_view:
                case R.id.navigation_functions:
                case R.id.navigation_setting:
                default:
                    return false;
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getReplaceLayoutId() {
        return R.id.layout_contant_mainA;
    }


}
