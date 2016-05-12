package com.mphantom.mysqlclient.utils;

import java.util.Arrays;

/**
 * Created by wushaorong on 16-5-13.
 */
public class PropertyTypeHelper {
    public static final int TYPE_UNKONW = 0;
    public static final int TYPE_NUMBER = 1;
    public static final int TYPE_CHARS = 2;
    public static final int TYPE_DATE = 3;

    public static final String[] NUMBERS = new String[]{
            "tinyint", "smallint", "mediumint", "int", "bigint", "float", "double"};
    public static final String[] CHARS = new String[]{
            "char", "varchar", "tinytext", "text", "mediumtext", "longtext"};
    public static final String[] TIMES = new String[]{
            "date", "time", "datetime", "timestamp"};

    public static int checkPropertyType(String type) {
        if (Arrays.asList(NUMBERS).contains(type.toLowerCase())) {
            return TYPE_NUMBER;
        }
        if (Arrays.asList(CHARS).contains(type.toLowerCase())) {
            return TYPE_CHARS;
        }
        if (Arrays.asList(TIMES).contains(type.toLowerCase())) {
            return TYPE_DATE;
        }
        return TYPE_UNKONW;
    }
}
