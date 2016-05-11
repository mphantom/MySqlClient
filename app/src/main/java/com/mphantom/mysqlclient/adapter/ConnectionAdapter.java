package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.ConnectionInfo;
import com.mphantom.mysqlclient.realm.ConnectionHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by wushaorong on 16-5-3.
 */
public class ConnectionAdapter extends AbstractRealmAdapter<ConnectionInfo, ConnectionAdapter.ConnectionViewHolder> implements View.OnClickListener,
        View.OnLongClickListener, ItemTouchHelperCallback.ItemTouchHelperAdapter {
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;

    public ConnectionAdapter(Context context) {
        super(context);
    }

    @Override
    public ConnectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.adapter_connection, parent, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new ConnectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConnectionViewHolder holder, int position) {
        ConnectionInfo info = getItem(position);
        if (info != null) {
            holder.tvHome.setText(info.getHost() + ":" + info.getPort());
            holder.tvName.setText(info.getName());

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
        ConnectionInfo info = getItem(position);
        ConnectionHelper.getInstance().delete(info.getName());
        notifyDataSetChanged();
    }


    public class ConnectionViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_homevalue_dataAdapter)
        TextView tvHome;
        @Bind(R.id.tv_namevalue_dataAdapter)
        TextView tvName;

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
