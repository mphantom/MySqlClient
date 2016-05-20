package com.mphantom.mysqlclient.widget.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.adapter.ConsoleAdapter;
import com.mphantom.mysqlclient.utils.Constant;

import org.springframework.jdbc.BadSqlGrammarException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.Bind;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ConsoleFragment extends BaseFragment implements View.OnClickListener {
    @Bind(R.id.recycler_ConsoleF)
    RecyclerView recyclerview;
    @Bind(R.id.edit_consoleF_input)
    EditText edit_input;
    @Bind(R.id.btn_consoleF_submit)
    Button btn_submit;
    @Bind(R.id.tv_ConsoleF)
    TextView tvTip;
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
        btn_submit.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (App.getInstance().connectionService.isConnectToSQL()) {
            tvTip.setVisibility(View.GONE);
            recyclerview.setVisibility(View.VISIBLE);
            btn_submit.setVisibility(View.VISIBLE);
            edit_input.setVisibility(View.VISIBLE);
        } else {
            tvTip.setVisibility(View.VISIBLE);
            recyclerview.setVisibility(View.GONE);
            btn_submit.setVisibility(View.GONE);
            edit_input.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        String sql = edit_input.getText().toString();
        if (TextUtils.isEmpty(sql)) {
            return;
        }
        lists.add(sql);
        edit_input.setText("");
        adapter.notifyDataSetChanged();
        Observable.create((Observable.OnSubscribe<String>) onSubscribe -> onSubscribe.onNext(sql))
                .subscribeOn(Schedulers.io())
                .map(s2 -> App.getInstance().connectionService.dosql(s2))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    int type = Integer.parseInt(result.get(Constant.QUERY_TYPE).toString());
                    if (type == 0) {
                        List<Map<String, Object>> list = (List<Map<String, Object>>) result.get(Constant.QUERY_RESULT);
                        String s = covertoString(list);
                        lists.add(s == null ? "no result" : s);
                        adapter.notifyDataSetChanged();
                    } else if (type == 1) {
                        int count = Integer.parseInt(result.get(Constant.QUERY_RESULT).toString());
                        float time = Float.parseFloat(result.get(Constant.QUERY_TIME).toString());
                        lists.add(String.format("%1$d 项被调整 (%2$.2f 秒)", count, time));
                        adapter.notifyDataSetChanged();
                    }
                }, throwable -> {
                    if (throwable instanceof BadSqlGrammarException) {
                        String error = ((BadSqlGrammarException) throwable).getSQLException().getMessage();
                        lists.add(error);
                        adapter.notifyDataSetChanged();
                    } else {
                        lists.add("无法发送到数据库");
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private String covertoString(List<Map<String, Object>> list) {
        if (list != null && !list.isEmpty()) {
            int count = list.get(0).size();
            StringBuilder sbsum = new StringBuilder();
            Map<String, Object> paramMap = list.get(0);
            Set<String> keys = paramMap.keySet();
            String[] params = keys.toArray(new String[count]);
            for (String param : params) {
                sbsum.append(" * ").append(param);
            }
            sbsum.append(" *\n");
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = list.get(i);
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < map.size(); j++) {
                    sb.append(" * ").append(map.get(params[j]));
                }
                sb.append(" *\n");
                sbsum.append(sb);
            }
            return sbsum.toString();
        } else {
            return null;
        }
    }
}
