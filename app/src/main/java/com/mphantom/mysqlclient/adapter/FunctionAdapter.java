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
import com.mphantom.mysqlclient.model.Trigger;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FunctionViewHolder>
        implements View.OnClickListener, View.OnLongClickListener, ItemTouchHelperCallback.ItemTouchHelperAdapter {

    private final LayoutInflater mLayoutInflater;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private List<Trigger> lists;
    private Context context;

    public FunctionAdapter(Context context, List<Trigger> lists) {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.lists = lists;
    }

    @Override
    public FunctionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.adapter_function, parent, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new FunctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FunctionViewHolder holder, int position) {
        Trigger trigger = lists.get(position);
        holder.tvName.setText(trigger.getTrigger());
        holder.tvEvent.setText(trigger.getEvent());
        holder.tvTable.setText(trigger.getTable());
        holder.tvTime.setText(trigger.getTiming());
        holder.itemView.setTag(trigger);

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
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> subscriber.onNext(position)).subscribeOn(Schedulers.io())
                .doOnNext(integer -> App.getInstance().connectionService.deleteTrigger(lists.get(integer).getTrigger()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> {
                    lists.remove(position);
                    notifyDataSetChanged();
                }, throwable -> Toast.makeText(context, R.string.error_sql, Toast.LENGTH_SHORT).show());

    }


    public class FunctionViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_namevalue_functionAdapter)
        TextView tvName;
        @Bind(R.id.tv_eventvalue_functionAdapter)
        TextView tvEvent;
        @Bind(R.id.tv_tablevalue_functionAdapter)
        TextView tvTable;
        @Bind(R.id.tv_timevalue_functionAdapter)
        TextView tvTime;

        public FunctionViewHolder(View itemView) {
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