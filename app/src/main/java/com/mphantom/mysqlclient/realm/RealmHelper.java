package com.mphantom.mysqlclient.realm;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by wushaorong on 16-5-10.
 */
public class RealmHelper {
    private final static String DATABASE_NAME = "temp.realm";
    private final static int DATABASE_VERSION = 1;
    private static RealmHelper instance;
    private Realm realm;
    private RealmMigration mRealmMigration = (dynamicRealm, oldversion, newversion) -> {
        RealmSchema schema = dynamicRealm.getSchema();
        //TODO 数据库版本升级时需要在这里进行声明
        //TODO data包中Realm类更改后需要进行增加
            if (oldversion == 1) {
                oldversion++;
            }
    };

    private RealmHelper() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(DATABASE_NAME)
                .schemaVersion(DATABASE_VERSION)
                .migration(mRealmMigration)
                .build();
        realm = Realm.getInstance(config);
    }

    public static RealmHelper getInstance() {
        if (instance == null) {
            instance = new RealmHelper();
        }
        return instance;
    }

    // 应用关闭时需要调用close方法
    public void close() {
        realm.close();
    }

    public Realm getRealm() {
        return realm;
    }
}
