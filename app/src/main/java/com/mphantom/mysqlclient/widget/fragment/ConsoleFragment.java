package com.mphantom.mysqlclient.widget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.ConsoleAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class ConsoleFragment extends BaseFragment {
    @Bind(R.id.recycler_ConsoleF)
    RecyclerView recyclerview;
    @Bind(R.id.edit_consoleF_input)
    EditText edit_input;
    @Bind(R.id.btn_consoleF_submit)
    Button btn_submit;
    private ConsoleAdapter adapter;
    private List<String> lists;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_console;
    }

    @Override
    protected boolean isButterKnife() {
        return true;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lists = new ArrayList<>();
        adapter = new ConsoleAdapter(context, lists);
        recyclerview.setLayoutManager(new LinearLayoutManager(context));
        recyclerview.setAdapter(adapter);
    }
}
