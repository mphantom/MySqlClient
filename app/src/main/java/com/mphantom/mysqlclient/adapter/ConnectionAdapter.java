package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mphantom.mysqlclient.App;
import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.realm.ConnectionHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wushaorong on 16-5-3.
 */
public class ConnectionAdapter extends AbstractRealmAdapter<ConnectionInfo, ConnectionAdapter.ConnectionViewHolder> implements
        View.OnLongClickListener, ItemTouchHelperCallback.ItemTouchHelperAdapter {
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private int index = -1;
    private ConnectionInfo tag = new ConnectionInfo();

    public ConnectionAdapter(Context context) {
        super(context);
    }

    @Override
    public ConnectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.adapter_connection, parent, false);
        view.setOnLongClickListener(this);
        return new ConnectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConnectionViewHolder holder, int position) {
        ConnectionInfo info = getItem(position);
        if (info != null) {
//            if (position != index) {
//                holder.cardView.setCardBackgroundColor(R.color.md_green_200);
//            }
            holder.tvHome.setText(info.getHost() + ":" + info.getPort());
            holder.tvName.setText(info.getName());
            tag.setUuid(info.getUuid());
            tag.setName(info.getName());
            tag.setUserName(info.getUserName());
            tag.setPassword(info.getPassword());
            tag.setDatabase(info.getDatabase());
            tag.setHost(info.getHost());
            tag.setPort(info.getPort());
            holder.itemView.setTag(tag);
            holder.itemView.setOnClickListener(v -> {
                holder.cardView.setCardBackgroundColor(R.color.md_deep_orange_a200);
                index = position;
                if (mItemClickListener != null)
                    mItemClickListener.OnItemClick(v, v.getTag());
            });

        }
    }

    @Override
    public boolean hasHeader() {
        return false;
    }

    @Override
    public boolean hasFooter() {
        return false;
    }

    @Override
    protected RealmResults<ConnectionInfo> loadData() {
        return ConnectionHelper.getInstance().findAllRealmResults();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        if (position == index) {
            index = -1;
            Observable.create((Observable.OnSubscribe<Integer>) onSubscribe -> onSubscribe.onNext(1))
                    .observeOn(Schedulers.io())
                    .subscribeOn(Schedulers.io())
                    .subscribe(integer -> App.getInstance().connectionService.sqlConnectionClose()
                    );
        }
        ConnectionInfo info = getItem(position);
        ConnectionHelper.getInstance().delete(info.getUuid());
        notifyDataSetChanged();
    }


    public class ConnectionViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_homevalue_dataAdapter)
        TextView tvHome;
        @BindView(R.id.tv_namevalue_dataAdapter)
        TextView tvName;
        @BindView(R.id.card_layout_dataAdapter)
        CardView cardView;

        public ConnectionViewHolder(View itemView) {
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
    public boolean onLongClick(View v) {
        if (mItemLongClickListener != null)
            mItemLongClickListener.OnItemLongClick(v, v.getTag());
        return false;
    }

}
