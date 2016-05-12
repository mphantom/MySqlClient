package com.mphantom.mysqlclient.utils;

/**
 * Created by wushaorong on 16-5-9.
 */
public class Constant {
    public static final String DEFAULT_HOST="rm-bp1k96rdj6v2mz8pq.mysql.rds.aliyuncs.com";
    public static final String DEFAULT_USER="wsr";
    public static final String DEFAULT_PASSWORD="wsr1117";
    public static final String DEFAULT_DATABASE="testforme";
    public static final int DEFAULT_PORT=3306;


    public static final String CONNECTION_INFO_NAME = "connection_info_name";
    public static final String CONNECTION_INFO = "connection_info";

    public static final String DATABASE = "Database";
    public static final String TABLE = "table";
    public static final String TABLE_IN_DB = "Tables_in_";

    public static final String QUERY_TIME = "query_time";
    public static final String QUERY_TYPE = "query_type";
    public static final String QUERY_RESULT = "query_result";


    public static final int WHAT_DATA_RETURNED = 0;
    public static final int WHAT_DATA_EMPTY = 1;
    public static final int WHAT_FAILED_TO_CONNECT = 2;
    public static final int WHAT_UPDATE_RETURNED = 3;
    public static final int WHAT_ERROR_OCCURRED = 4;

}
