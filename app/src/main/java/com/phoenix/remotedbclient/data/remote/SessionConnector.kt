package com.phoenix.remotedbclient.data.remote

/**
 * 职责：会话管理的最小契约，供 ConnectionViewModel 测试替换真实 SessionManager。
 * 位置：data/remote；SessionManager 是生产实现。
 */
interface SessionConnector {
    fun getOrCreate(configId: Long): DatabaseConnector
    fun remove(configId: Long)
}
