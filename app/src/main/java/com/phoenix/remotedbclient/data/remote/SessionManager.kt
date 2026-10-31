package com.phoenix.remotedbclient.data.remote

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds active [RemoteDatabaseManager] instances keyed by connection config ID.
 * Survives configuration changes; managers are created once per connection session.
 */
@Singleton
class SessionManager @Inject constructor() {

    private val sessions = ConcurrentHashMap<Long, RemoteDatabaseManager>()
    private val passwords = ConcurrentHashMap<Long, String>()

    /**
     * Returns the existing manager for [configId], or creates and stores a new one.
     */
    fun getOrCreate(configId: Long): RemoteDatabaseManager {
        return sessions.getOrPut(configId) { RemoteDatabaseManager() }
    }

    /**
     * Returns the manager for [configId], or null if no session exists.
     */
    fun get(configId: Long): RemoteDatabaseManager? = sessions[configId]

    /**
     * Closes the session for [configId] (disconnects + releases JDBC resources)
     * and removes it from the session map.
     */
    fun remove(configId: Long) {
        sessions.remove(configId)?.close()
    }

    /**
     * Returns all active session config IDs.
     */
    fun activeConfigIds(): Set<Long> = sessions.keys.toSet()

    /**
     * Whether a session exists for [configId].
     */
    fun hasSession(configId: Long): Boolean = sessions.containsKey(configId)

    /**
     * Stores a password for the given config ID in memory.
     */
    fun storePassword(configId: Long, password: String) {
        passwords[configId] = password
    }

    /**
     * Retrieves the stored password for [configId], or null.
     */
    fun getPassword(configId: Long): String? = passwords[configId]

    /**
     * Closes and removes all sessions.
     */
    fun clearAll() {
        sessions.values.forEach { it.close() }
        sessions.clear()
        passwords.clear()
    }
}
