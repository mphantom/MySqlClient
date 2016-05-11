package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.Table;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-4.
 */
public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> implements View.OnClickListener {

    private final LayoutInflater mLayoutInflater;
    private OnItemClickListener mItemClickListener;
    private List<Table> lists;

    public TableAdapter(Context context, List<Table> lists) {
        mLayoutInflater = LayoutInflater.from(context);
        this.lists = lists;
    }

    @Override
    public TableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.adapter_table, parent, false);
        view.setOnClickListener(this);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TableViewHolder holder, int position) {
        Table table = lists.get(position);
        holder.tvName.setText(table.getName());
        holder.itemView.setTag(table);

    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public class TableViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_namevalue_tableAdapter)
        TextView tvName;

        public TableViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mItemClickListener != null)
            mItemClickListener.OnItemClick(v, v.getTag());
    }
}
