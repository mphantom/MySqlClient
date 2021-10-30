package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mphantom.mysqlclient.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-12.
 */
public class ConsoleAdapter extends RecyclerView.Adapter<ConsoleAdapter.ConsoleViewHolder> {
    private final LayoutInflater mLayoutInflater;
    private List<String> lists;

    public ConsoleAdapter(Context context, List<String> lists) {
        mLayoutInflater = LayoutInflater.from(context);
        this.lists = lists;
    }

    @Override
    public ConsoleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.adapter_console, parent, false);
        return new ConsoleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConsoleViewHolder holder, int position) {
        holder.textView.setText(lists.get(position));
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public class ConsoleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_messageInfo_ConsoleAdapter)
        TextView textView;

        public ConsoleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

