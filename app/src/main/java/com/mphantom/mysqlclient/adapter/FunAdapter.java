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
import com.mphantom.mysqlclient.model.Function;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-12.
 */
public class FunAdapter extends RecyclerView.Adapter<FunAdapter.FunViewHolder>
        implements View.OnClickListener, View.OnLongClickListener, ItemTouchHelperCallback.ItemTouchHelperAdapter {

    private final LayoutInflater mLayoutInflater;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private List<Function> lists;
    private Context context;

    public FunAdapter(Context context, List<Function> lists) {
        this.context = context;
        mLayoutInflater = LayoutInflater.from(context);
        this.lists = lists;
    }

    @Override
    public FunViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.adapter_fun, parent, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new FunViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FunViewHolder holder, int position) {
        Function function = lists.get(position);
        holder.tvName.setText(function.getName());
        holder.itemView.setTag(function);

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
                .doOnNext(integer -> App.getInstance().connectionService.deleteFunction(lists.get(integer).getName()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer1 -> {
                    lists.remove(position);
                    notifyDataSetChanged();
                }, throwable -> Toast.makeText(context, R.string.error_sql, Toast.LENGTH_SHORT).show());

    }


    public class FunViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_namevalue_funAdapter)
        TextView tvName;

        public FunViewHolder(View itemView) {
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