package com.mphantom.mysqlclient.core;

import android.content.Context;

import com.mphantom.mysqlclient.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wushaorong on 16-5-9.
 */
public class SqlHelper {
    private static Context context;

    public static void init(Context context) {
        SqlHelper.context = context;
    }

    public static List<Map<String, Object>> getHelpList() {
        String[] keys = {"OP", "USAGE"};
        String[] ops = context.getResources().getStringArray(R.array.ops);
        String[] usages = context.getResources().getStringArray(R.array.usages);
        List<Map<String, Object>> helpList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < ops.length; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(keys[0], ops[i]);
            map.put(keys[1], usages[i]);
            helpList.add(map);
        }
        return helpList;
    }


}
