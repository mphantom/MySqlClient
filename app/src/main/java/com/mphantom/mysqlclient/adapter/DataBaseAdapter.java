package com.mphantom.mysqlclient.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mphantom.mysqlclient.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-3.
 */
public class DataBaseAdapter extends RecyclerView.Adapter<DataBaseAdapter.BataBaseViewHolder> {

    @Override
    public BataBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(BataBaseViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class BataBaseViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_homevalue_dataAdapter)
        TextView tvHome;
        @Bind(R.id.tv_namevalue_dataAdapter)
        TextView tvName;

        public BataBaseViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
