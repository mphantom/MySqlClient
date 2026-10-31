package com.phoenix.remotedbclient.testutil

import com.phoenix.remotedbclient.data.db.ConnectionConfigEntity
import com.phoenix.remotedbclient.data.remote.DatabaseConnector
import com.phoenix.remotedbclient.data.remote.SessionConnector

/**
 * 职责：SessionConnector / DatabaseConnector 的测试替身，用于验证主链路
 * “先连接、后导航、失败清理”的时序，不依赖真实 JDBC。
 * 位置：test 源的公共测试工具。
 */
class FakeDatabaseSession : DatabaseConnector {

    var connected: Boolean = false
        private set

    var failure: Exception? = null

    override val isConnected: Boolean
        get() = connected

    override suspend fun connect(config: ConnectionConfigEntity, password: String) {
        failure?.let { throw it }
        connected = true
    }

    fun reset() {
        connected = false
    }
}

class FakeSessionConnector(
    val session: FakeDatabaseSession = FakeDatabaseSession()
) : SessionConnector {

    var removed: Boolean = false
        private set

    var requestedConfigId: Long? = null
        private set

    override fun getOrCreate(configId: Long): DatabaseConnector {
        requestedConfigId = configId
        return session
    }

    override fun remove(configId: Long) {
        removed = true
        session.reset()
    }
}
