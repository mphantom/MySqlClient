# JDBC drivers
-keep class com.mysql.cj.** { *; }
-keep class org.postgresql.** { *; }
-dontwarn com.mysql.cj.**
-dontwarn org.postgresql.**
-dontwarn javax.xml.**
-dontwarn org.w3c.dom.**

# Keep Room entities
# [改动前] 保留规则指向 com.example.remotedbexplorer.data.db
# [改动后] 指向 com.phoenix.remotedbclient.data.db
# [原因] 包名迁移后旧规则不再匹配，release 构建会误混淆/裁剪 Room 实体
-keep class com.phoenix.remotedbclient.data.db.** { *; }
