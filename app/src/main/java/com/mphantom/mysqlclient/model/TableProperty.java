package com.mphantom.mysqlclient.model;

import android.text.TextUtils;

/**
 * Created by wushaorong on 16-5-9.
 */
public class TableProperty {
    private String field;
    private String type;
    private String _null;
    private String key;
    private String _default;
    private String extra;

    public TableProperty() {

    }

    public TableProperty(TableProperty prop) {
        this.field = prop.field;
        this.type = prop.type;
        this._null = prop._null;
        this.key = prop.key;
        this._default = prop._default;
        this.extra = prop.extra;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String get_null() {
        return _null;
    }

    public void set_null(String _null) {
        this._null = _null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String get_default() {
        return _default;
    }

    public void set_default(String _default) {
        this._default = _default;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getName() {
        return field;
    }

    public void setPrimary(boolean flag) {
        if (flag) {
            this.key = "PRI";
        } else {
            this.key = "";
        }
    }

    public void setNullable(boolean flag) {
        _null = flag ? "YES" : "NO";
    }

    public void setAutoIncrement(boolean flag) {
        if (flag) {
            extra = "auto_increment";
        } else {
            extra = "";
        }
    }

    public String ConvertSql() {
        StringBuilder sb = new StringBuilder();
        sb.append(field).append(" ");
        sb.append(type).append(" ");
        if (_default != null)
            sb.append("Default " + _default).append(" ");
        if (key != null && key.contains("PRI"))
            sb.append("PRIMARY KEY").append(" ");
        if (_null != null && _null.equals("NO"))
            sb.append("NOT NULL").append(" ");
        if (extra != null && extra.contains("auto_increment"))
            sb.append("AUTO_INCREMENT").append(" ");
        sb.append(",");
        return sb.toString();
    }

    public String getContent() {
        StringBuilder sb = new StringBuilder();
        sb.append(field);
        sb.append(type);
        if (!TextUtils.isEmpty(key)) {
            sb.append(", ").append(key);
        }
        if (_null.equals("YES")) {
            sb.append(", ").append("can be null");
        } else {
            sb.append(", ").append("not null");
        }
        if (!TextUtils.isEmpty(_default)) {
            sb.append(", ").append("default for ").append(_default);
        }
        if (!TextUtils.isEmpty(extra)) {
            sb.append(", ").append(extra);
        }
        return sb.toString();
    }
}
