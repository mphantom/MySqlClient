package com.mphantom.mysqlclient.model;

/**
 * Created by wushaorong on 16-5-12.
 */
public class Trigger {
    private String trigger;
    private String event;
    private String table;
    private String statement;
    private String timing;
    private String created;
    private String sql_mode;
    private String definer;
    private String character_set_client;

    public String getCharacter_set_client() {
        return character_set_client;
    }

    public void setCharacter_set_client(String character_set_client) {
        this.character_set_client = character_set_client;
    }

    public String getCollation_connection() {
        return collation_connection;
    }

    public void setCollation_connection(String collation_connection) {
        this.collation_connection = collation_connection;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getDatabaseCollation() {
        return databaseCollation;
    }

    public void setDatabaseCollation(String databaseCollation) {
        this.databaseCollation = databaseCollation;
    }

    public String getDefiner() {
        return definer;
    }

    public void setDefiner(String definer) {
        this.definer = definer;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSql_mode() {
        return sql_mode;
    }

    public void setSql_mode(String sql_mode) {
        this.sql_mode = sql_mode;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    private String collation_connection;
    private String databaseCollation;
}
