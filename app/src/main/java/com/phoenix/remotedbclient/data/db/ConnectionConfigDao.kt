package com.phoenix.remotedbclient.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for connection configuration CRUD operations.
 */
@Dao
interface ConnectionConfigDao {

    /** Observe all saved connection configs, ordered by name. */
    @Query("SELECT * FROM connection_configs ORDER BY name ASC")
    fun observeAll(): Flow<List<ConnectionConfigEntity>>

    /** Get a single config by ID (suspend, one-shot). */
    @Query("SELECT * FROM connection_configs WHERE id = :id")
    suspend fun getById(id: Long): ConnectionConfigEntity?

    /** Insert a new config and return its generated ID. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ConnectionConfigEntity): Long

    /** Update an existing config. */
    @Update
    suspend fun update(config: ConnectionConfigEntity)

    /** Delete a config. */
    @Delete
    suspend fun delete(config: ConnectionConfigEntity)
}
