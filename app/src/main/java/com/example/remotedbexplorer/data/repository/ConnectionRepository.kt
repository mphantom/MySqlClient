package com.example.remotedbexplorer.data.repository

import com.example.remotedbexplorer.data.db.ConnectionConfigDao
import com.example.remotedbexplorer.data.db.ConnectionConfigEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository mediating between the UI layer and local data sources
 * (Room database for configs, EncryptedSharedPreferences for passwords).
 */
@Singleton
class ConnectionRepository @Inject constructor(
    private val dao: ConnectionConfigDao
) {
    /** Flow of all saved connection configs, ordered by name. */
    fun observeAllConfigs(): Flow<List<ConnectionConfigEntity>> = dao.observeAll()

    /** Get a single config by ID (one-shot). */
    suspend fun getConfigById(id: Long): ConnectionConfigEntity? = dao.getById(id)

    /** Insert a new config and return its generated ID. */
    suspend fun insertConfig(config: ConnectionConfigEntity): Long = dao.insert(config)

    /** Update an existing config. */
    suspend fun updateConfig(config: ConnectionConfigEntity) = dao.update(config)

    /** Delete a config. */
    suspend fun deleteConfig(config: ConnectionConfigEntity) = dao.delete(config)
}
