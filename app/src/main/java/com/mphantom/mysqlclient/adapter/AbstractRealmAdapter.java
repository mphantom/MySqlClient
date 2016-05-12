package com.mphantom.mysqlclient.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by wushaorong on 16-5-11.
 */
public abstract class AbstractRealmAdapter<T extends RealmObject, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    protected LayoutInflater inflater;
    protected Context context;
    protected RealmResults<T> mResults;

    protected AbstractRealmAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        mResults = loadData();
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public T getItem(int position) {
        if (!mResults.isEmpty()) {
            return mResults.get(position);
        }
        return null;
    }


    @Override
    public final int getItemCount() {
        return getCount();
    }

    public final int getCount() {
        return mResults.size();
    }

    public abstract boolean hasHeader();

    public abstract boolean hasFooter();


    public void setData(RealmResults<T> results) {
        mResults = results;
        notifyDataSetChanged();
    }

    protected abstract RealmResults<T> loadData();


}
