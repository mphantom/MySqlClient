# JDBC drivers
-keep class com.mysql.cj.** { *; }
-keep class org.postgresql.** { *; }
-dontwarn com.mysql.cj.**
-dontwarn org.postgresql.**
-dontwarn javax.xml.**
-dontwarn org.w3c.dom.**

# Keep Room entities
-keep class com.example.remotedbexplorer.data.db.** { *; }
