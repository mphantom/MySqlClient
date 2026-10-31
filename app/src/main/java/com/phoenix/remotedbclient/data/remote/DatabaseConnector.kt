package com.phoenix.remotedbclient.data.remote

import com.phoenix.remotedbclient.data.db.ConnectionConfigEntity

/**
 * 职责：连接会话的最小契约，供 ConnectionViewModel 在不依赖真实 JDBC 的情况下测试。
 * 位置：data/remote；RemoteDatabaseManager 是生产实现。
 */
interface DatabaseConnector {
    val isConnected: Boolean
    suspend fun connect(config: ConnectionConfigEntity, password: String)
}
