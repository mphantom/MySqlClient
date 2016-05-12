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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by wushaorong on 16-5-12.
 */
public class TablePropertyAdapter extends RecyclerView.Adapter<TablePropertyAdapter.TablePropertyViewHolder> {
    private List<TableProperty> tablePropertyList;
    private final LayoutInflater mLayoutInflater;

    public TablePropertyAdapter(Context context, List<TableProperty> list) {
        mLayoutInflater = LayoutInflater.from(context);
        this.tablePropertyList = list;
    }

    @Override
    public TablePropertyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.adapter_table_property, parent, false);
        return new TablePropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TablePropertyViewHolder holder, int position) {
        TableProperty tableProperty = tablePropertyList.get(position);
        holder.tv_field.setText(tableProperty.getName());
        holder.tv_type.setText(tableProperty.getType());
        holder.tv_null.setText(tableProperty.get_null());
        holder.tv_key.setText(tableProperty.getKey() == null ? "<null>" : tableProperty.getKey());
        holder.tv_default.setText(tableProperty.get_default() == null ? "<null>" : tableProperty.get_default());
        holder.tv_extra.setText(tableProperty.getExtra() == null ? "<null>" : tableProperty.getExtra());
        holder.itemView.setTag(tableProperty);
    }

    @Override
    public int getItemCount() {
        return tablePropertyList.size();
    }

    class TablePropertyViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_field_tableProperty)
        TextView tv_field;
        @Bind(R.id.tv_type_tableProperty)
        TextView tv_type;
        @Bind(R.id.tv_null_tableProperty)
        TextView tv_null;
        @Bind(R.id.tv_key_tableProperty)
        TextView tv_key;
        @Bind(R.id.tv_default_tableProperty)
        TextView tv_default;
        @Bind(R.id.tv_extra_tableProperty)
        TextView tv_extra;

        public TablePropertyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);
        }
    }
}
