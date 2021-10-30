package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.Table;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.TableViewHolder>
        implements View.OnClickListener, View.OnLongClickListener, ItemTouchHelperCallback.ItemTouchHelperAdapter {

    private final LayoutInflater mLayoutInflater;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private List<Table> lists;
    private Context context;

    public ViewAdapter(Context context, List<Table> lists) {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.lists = lists;
    }

    @Override
    public TableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.adapter_table, parent, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
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

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> subscriber.onNext(position))
                .subscribeOn(Schedulers.io())
                .doOnNext(integer -> App.getInstance().connectionService.deleteView(lists.get(integer).getName()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> {
                    lists.remove(position);
                    notifyDataSetChanged();
                }, throwable -> Toast.makeText(context, R.string.error_sql, Toast.LENGTH_SHORT).show());

    }


    public class TableViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_namevalue_tableAdapter)
        TextView tvName;

        public TableViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mItemLongClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mItemClickListener != null)
            mItemClickListener.OnItemClick(v, v.getTag());
    }

    @Override
    public boolean onLongClick(View v) {
        if (mItemLongClickListener != null)
            mItemLongClickListener.OnItemLongClick(v, v.getTag());
        return false;
    }
}
