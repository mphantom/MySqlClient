package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mphantom.mysqlclient.R;
import com.mphantom.mysqlclient.model.SshUser;
import com.mphantom.mysqlclient.realm.ConnectionHelper;
import com.mphantom.mysqlclient.realm.SshUserHelper;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by wushaorong on 16-5-21.
 */
public class SshAdapter extends AbstractRealmAdapter<SshUser, SshAdapter.SshViewHolder> implements
        View.OnLongClickListener, ItemTouchHelperCallback.ItemTouchHelperAdapter, View.OnClickListener {
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private SshUser tag = new SshUser();

    public SshAdapter(Context context) {
        super(context);
    }

    @Override
    public SshViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.adapter_ssh, parent, false);
        view.setOnLongClickListener(this);
        view.setOnClickListener(this);
        return new SshViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SshViewHolder holder, int position) {
        SshUser user = getItem(position);
        if (user != null) {
            holder.tvHome.setText(user.getHost() + ":" + user.getPort());
            holder.tvName.setText(user.getName());
            tag.setUuid(user.getUuid());
            tag.setName(user.getName());
            tag.setUserName(user.getUserName());
            tag.setPassword(user.getPassword());
            tag.setHost(user.getHost());
            tag.setPort(user.getPort());
            holder.itemView.setTag(tag);
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
    protected RealmResults<SshUser> loadData() {
        return SshUserHelper.getInstance().findAllRealmResults();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        SshUser sshUser = getItem(position);
        ConnectionHelper.getInstance().delete(sshUser.getUuid());
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (mItemClickListener != null) {
            mItemClickListener.OnItemClick(v, v.getTag());
        }
    }


    public class SshViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_homevalue_sshAdapter)
        TextView tvHome;
        @Bind(R.id.tv_namevalue_sshAdapter)
        TextView tvName;

        public SshViewHolder(View itemView) {
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