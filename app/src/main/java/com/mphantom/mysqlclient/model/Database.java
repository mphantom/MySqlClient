package com.mphantom.mysqlclient.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wushaorong on 11/7/15.
 */
public class Database implements Parcelable {
    private String name;
    private String content;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.content);
    }

    public Database() {
    }

    public Database(String name, String content) {
        this.name = name;
        this.content = content;
    }

    protected Database(Parcel in) {
        this.name = in.readString();
        this.content = in.readString();
    }

    public static final Parcelable.Creator<Database> CREATOR = new Parcelable.Creator<Database>() {
        public Database createFromParcel(Parcel source) {
            return new Database(source);
        }

        public Database[] newArray(int size) {
            return new Database[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
