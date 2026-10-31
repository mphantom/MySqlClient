package com.phoenix.remotedbclient.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a saved remote database connection configuration.
 * Password is stored separately in EncryptedSharedPreferences for security.
 */
@Entity(tableName = "connection_configs")
data class ConnectionConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** Human-readable name for this connection, e.g. "Production MySQL". */
    val name: String,

    // [改动前] 注释与实现都只考虑 "MySQL" / "PostgreSQL" 两种类型
    // [改动后] dbType 保存任意 DatabaseDialect.id，端口默认值由对应方言提供
    // [原因] 方言注册表允许新增数据库，实体不再限制类型枚举
    /** 方言 id，对应 DatabaseDialect.id，例如 "MySQL" / "PostgreSQL"。 */
    val dbType: String,

    /** Remote host address (IP or hostname). */
    val host: String,

    /** TCP port number; the default comes from the selected dialect. */
    val port: Int,

    /** Database username for authentication. */
    val username: String,

    /** Optional default database/schema to connect to. */
    val defaultDatabase: String = "",

    /** Timestamp of when this config was created, in epoch millis. */
    val createdAt: Long = System.currentTimeMillis()
)
