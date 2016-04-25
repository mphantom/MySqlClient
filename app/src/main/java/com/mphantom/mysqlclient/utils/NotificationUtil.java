package com.mphantom.mysqlclient.utils;

/**
 * Created by wushaorong on 11/7/15.
 */
public class NotificationUtil {

    private static class NotificationHolder {
        private static final NotificationUtil INSTANCE = new NotificationUtil();
    }

    NotificationUtil() {

    }

    private static NotificationUtil getInstall() {
        return NotificationHolder.INSTANCE;
    }
}
