package com.phoenix.remotedbclient.data.remote

import com.phoenix.remotedbclient.driver.DriverRegistry
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Holds active [RemoteDatabaseManager] instances keyed by connection config ID.
 * Survives configuration changes; managers are created once per connection session.
 *
 * [改动前] 直接 new RemoteDatabaseManager()，新连接无法拿到方言注册表
 * [改动后] 注入 DriverRegistry，并用它为每个会话创建 RemoteDatabaseManager
 * [原因] 让每个连接都能按 dbType 解析到对应数据库方言
 */
@Singleton
class SessionManager @Inject constructor(
    private val driverRegistry: DriverRegistry
) : SessionConnector {

    private val sessions = ConcurrentHashMap<Long, RemoteDatabaseManager>()
    private val passwords = ConcurrentHashMap<Long, String>()

    /**
     * Returns the existing manager for [configId], or creates and stores a new one.
     */
    override fun getOrCreate(configId: Long): RemoteDatabaseManager {
        return sessions.getOrPut(configId) { RemoteDatabaseManager(driverRegistry) }
    }

    /**
     * Returns the manager for [configId], or null if no session exists.
     */
    fun get(configId: Long): RemoteDatabaseManager? = sessions[configId]

    /**
     * Closes the session for [configId] (disconnects + releases JDBC resources)
     * and removes it from the session map.
     */
    override fun remove(configId: Long) {
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
