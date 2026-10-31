package com.phoenix.remotedbclient.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Local Room database holding connection configuration entities.
 */
@Database(
    entities = [ConnectionConfigEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** DAO for connection config operations. */
    abstract fun connectionConfigDao(): ConnectionConfigDao
}
