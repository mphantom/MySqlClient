package com.mphantom.mysqlclient.model;

/**
 * Created by wushaorong on 16-5-9.
 */
public class Table {
    private String name;

    private String content;

    public Table() {

    }

    public Table(Table table) {
        this.name = table.name;
        this.content = table.content;
    }

    public Table(String name, String content) {
        this.name = name;
        this.content = content;
    }

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
