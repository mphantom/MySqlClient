package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mphantom.mysqlclient.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-3.
 */
public class DataBaseAdapter extends RecyclerView.Adapter<DataBaseAdapter.DataBaseViewHolder> implements View.OnClickListener {
    private final LayoutInflater mLayoutInflater;
    private OnItemClickListener mItemClickListener;

    public DataBaseAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public DataBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.adapter_database, parent, false);
        view.setOnClickListener(this);
        return new DataBaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataBaseViewHolder holder, int position) {
        holder.tvHome.setText("testhome");
        holder.tvName.setText("testname");
    }

    @Override
    public int getItemCount() {
        return 5;
    }


    public class DataBaseViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_homevalue_dataAdapter)
        TextView tvHome;
        @Bind(R.id.tv_namevalue_dataAdapter)
        TextView tvName;

        public DataBaseViewHolder(View itemView) {
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
