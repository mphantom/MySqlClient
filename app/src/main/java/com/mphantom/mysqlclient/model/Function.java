package com.mphantom.mysqlclient.model;

/**
 * Created by wushaorong on 16-5-21.
 */
public class Function {
    private String db;
    private String name;
    private String type;
    private String definer;
    private String modified;
    private String created;
    private String security_type;
    private String comment;
    private String character_set_client;
    private String collation_connection;
    private String database_collation;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getDatabase_collation() {
        return database_collation;
    }

    public void setDatabase_collation(String database_collation) {
        this.database_collation = database_collation;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getDefiner() {
        return definer;
    }

    public void setDefiner(String definer) {
        this.definer = definer;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSecurity_type() {
        return security_type;
    }

    public void setSecurity_type(String security_type) {
        this.security_type = security_type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
