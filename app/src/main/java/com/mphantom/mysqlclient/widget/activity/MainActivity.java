package com.mphantom.mysqlclient.widget.activity;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toolbar;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.utils.Constant;
import com.mphantom.mysqlclient.widget.fragment.ConsoleFragment;
import com.mphantom.mysqlclient.widget.fragment.FunctionFragment;
import com.mphantom.mysqlclient.widget.fragment.HomeFragment;
import com.mphantom.mysqlclient.widget.fragment.SshFragment;
import com.mphantom.mysqlclient.widget.fragment.TableFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {
    @Bind(R.id.navigation_mainA)
    NavigationView navigationView;
    @Bind(R.id.layout_drawer_mainA)
    DrawerLayout drawerLayout;
    @Bind(R.id.toolbar_mainA)
    Toolbar toolbar;
    View headView;
    TextView tv_hostInfo;
    TextView tv_nick;

    private int currentIndex;
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setActionBar(toolbar);
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    item.setChecked(true);
                    title = item.getTitle();
                    toolbar.setTitle(title);
                    currentIndex = item.getItemId();
                    changeFragment(item.getItemId(), new HomeFragment(), "HomeFragment");
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.navigation_table:
                    item.setChecked(true);
                    title = item.getTitle();
                    toolbar.setTitle(title);
                    currentIndex = item.getItemId();
                    changeFragment(item.getItemId(), new TableFragment(), "TableFragment");
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.navigation_consule:
                    item.setChecked(true);
                    title = item.getTitle();
                    toolbar.setTitle(title);
                    currentIndex = item.getItemId();
                    changeFragment(item.getItemId(), new ConsoleFragment(), "ConsoleFragment");
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.navigation_ssh:
                    item.setChecked(true);
                    title = item.getTitle();
                    toolbar.setTitle(title);
                    currentIndex = item.getItemId();
                    changeFragment(item.getItemId(), new SshFragment(), "SshFragment");
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.navigation_functions:
                    item.setChecked(true);
                    title = item.getTitle();
                    toolbar.setTitle(title);
                    currentIndex = item.getItemId();
                    changeFragment(item.getItemId(), new FunctionFragment(), "FunctionFragment");
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.navigation_setting:
                default:
                    return false;
            }
        });
        currentIndex = R.id.navigation_home;
        changeFragment(R.id.navigation_home, new HomeFragment(), "HomeFragment");
        title = getString(R.string.home);
        headView = navigationView.getHeaderView(0);
        tv_hostInfo = (TextView) headView.findViewById(R.id.tv_hostinfo_mainA);
        tv_nick = (TextView) headView.findViewById(R.id.tv_nick_mainA);
        tv_hostInfo.setText(Constant.DEFAULT_HOST);
        tv_nick.setText("BOB");
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle(title);
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
