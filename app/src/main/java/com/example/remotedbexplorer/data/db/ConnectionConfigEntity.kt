package com.example.remotedbexplorer.data.db

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

    /** Database type: either "MySQL" or "PostgreSQL". */
    val dbType: String,

    /** Remote host address (IP or hostname). */
    val host: String,

    /** TCP port number. Defaults to 3306 for MySQL or 5432 for PostgreSQL. */
    val port: Int,

    /** Database username for authentication. */
    val username: String,

    /** Optional default database/schema to connect to. */
    val defaultDatabase: String = "",

    /** Timestamp of when this config was created, in epoch millis. */
    val createdAt: Long = System.currentTimeMillis()
)
