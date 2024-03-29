package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.TableProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder>
        implements View.OnClickListener, ItemTouchHelperCallback.ItemTouchHelperAdapter {

    private final LayoutInflater mLayoutInflater;
    private OnItemClickListener mItemClickListener;
    private List<TableProperty> listProperty;
    private List<Map<String, Object>> lists;
    private String tableName;

    public DataAdapter(Context context, List<TableProperty> listProperty, List<Map<String, Object>> lists) {
        mLayoutInflater = LayoutInflater.from(context);
        this.listProperty = listProperty;
        this.lists = lists;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.adapter_data, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, int position) {
        if (position == 0) {
            for (int i = 0; i < listProperty.size(); i++) {
                if (i >= 3) {
                    holder.tvParam4.setText("....");
                    break;
                }
                holder.tv_list.get(i).setText(listProperty.get(i).getField());
            }
        } else {
            Map<String, Object> map = lists.get(position - 1);
            holder.itemView.setTag(map);
            holder.itemView.setOnClickListener(this);
            for (int i = 0; i < listProperty.size(); i++) {
                if (i >= 3) {
                    holder.tvParam4.setText("....");
                    break;
                }
                Object object = map.get(listProperty.get(i).getField());
                if (object != null)
                    holder.tv_list.get(i).setText(object.toString());
            }
        }

    }

    @Override
    public int getItemCount() {
        return lists.size() + 1;
    }


    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mItemClickListener != null)
            mItemClickListener.OnItemClick(v, v.getTag());
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        Map<String, Object> map = lists.get(position - 1);
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ").append(tableName).append(" ")
                .append(" WHERE ");
        for (int i = 0; i < listProperty.size(); i++) {
            String field = listProperty.get(i).getField();
            String value = map.get(field).toString();
            if (!TextUtils.isEmpty(value))
                sb.append(" ").append(field).append(" = ").append("\"").append(value).append("\"").append(" and");
        }
        String sql = sb.substring(0, sb.length() - 3);
        Log.d("testforthedelete", sql);
        Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> App.getInstance().connectionService.deleteFrom(sql))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> {
                    lists.remove(position - 1);
                    notifyDataSetChanged();
                });
    }

    public class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_param1_dataAdapter)
        TextView tvParam1;
        @BindView(R.id.tv_param2_dataAdapter)
        TextView tvParam2;
        @BindView(R.id.tv_param3_dataAdapter)
        TextView tvParam3;
        @BindView(R.id.tv_param4_dataAdapter)
        TextView tvParam4;
        List<TextView> tv_list;

        public DataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            tv_list = new ArrayList<>();
            tv_list.add(tvParam1);
            tv_list.add(tvParam2);
            tv_list.add(tvParam3);
            tv_list.add(tvParam4);
        }
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
