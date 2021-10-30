package com.mphantom.mysqlclient.widget.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.ItemTouchHelperCallback;
import com.mphantom.mysqlclient.adapter.OnItemClickListener;
import com.mphantom.mysqlclient.adapter.OnItemLongClickListener;
import com.mphantom.mysqlclient.adapter.SshAdapter;
import com.mphantom.mysqlclient.dialog.SshDialog;
import com.mphantom.mysqlclient.model.SshUser;
import com.mphantom.mysqlclient.utils.OnConfirm;
import com.mphantom.mysqlclient.widget.activity.SshActivity;

import butterknife.BindView;

public class SshFragment extends BaseFragment implements OnConfirm, OnItemClickListener, OnItemLongClickListener {
    @BindView(R.id.recycler_SSHF)
    RecyclerView recyclerview;
    @BindView(R.id.float_SSHF)
    FloatingActionButton float_btn;
    private SshAdapter adapter;
    private ItemTouchHelper mItemTouchHelper;

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
        adapter = new SshAdapter(context);
        recyclerview.setLayoutManager(new LinearLayoutManager(context));
        recyclerview.setAdapter(adapter);
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        mItemTouchHelper.attachToRecyclerView(recyclerview);
        float_btn.setOnClickListener(v -> {
            SshDialog dialog = new SshDialog(context);
            dialog.setOnConfirm(this);
            dialog.show();
        });

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void OnButtonConfirm(Object object) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void OnItemClick(View view, Object object) {
        Intent intent = new Intent(context, SshActivity.class);
        SshUser sshuser = (SshUser) object;
        intent.putExtra("host", sshuser.getHost());
        intent.putExtra("port", sshuser.getPort());
        intent.putExtra("username", sshuser.getUserName());
        intent.putExtra("password", sshuser.getPassword());
        startActivity(intent);
    }

    @Override
    public void OnItemLongClick(View view, Object object) {

    }
}
