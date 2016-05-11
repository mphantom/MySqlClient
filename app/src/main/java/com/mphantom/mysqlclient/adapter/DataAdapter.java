package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.TableProperty;

import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-12.
 */
public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> implements View.OnClickListener {

    private final LayoutInflater mLayoutInflater;
    private OnItemClickListener mItemClickListener;
    private List<TableProperty> listProperty;
    private List<Map<String, Object>> lists;

    public DataAdapter(Context context, List<TableProperty> listProperty, List<Map<String, Object>> lists) {
        mLayoutInflater = LayoutInflater.from(context);
        this.listProperty = listProperty;
        this.lists = lists;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.adapter_data, parent, false);
        view.setOnClickListener(this);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        Map<String, Object> map = lists.get(position);
//        holder.tvParam1.setText();
//        holder.itemView.setTag(table);

    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public class DataViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_param1_dataAdapter)
        TextView tvParam1;
        @Bind(R.id.tv_param2_dataAdapter)
        TextView tvParam2;
        @Bind(R.id.tv_param3_dataAdapter)
        TextView tvParam3;

        public DataViewHolder(View itemView) {
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
