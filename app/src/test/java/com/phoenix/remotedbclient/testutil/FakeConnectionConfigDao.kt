package com.phoenix.remotedbclient.testutil

import com.phoenix.remotedbclient.data.db.ConnectionConfigDao
import com.phoenix.remotedbclient.data.db.ConnectionConfigEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * 职责：ConnectionConfigDao 的轻量测试替身，让不依赖 Room 的 ViewModel 单元测试
 * 可以构造 ConnectionRepository。
 * 位置：test 源；仅返回空数据并空实现写操作。
 */
class FakeConnectionConfigDao : ConnectionConfigDao {

    override fun observeAll(): Flow<List<ConnectionConfigEntity>> = flowOf(emptyList())

    override suspend fun getById(id: Long): ConnectionConfigEntity? = null

    override suspend fun insert(config: ConnectionConfigEntity): Long = 0L

    override suspend fun update(config: ConnectionConfigEntity) = Unit

    override suspend fun delete(config: ConnectionConfigEntity) = Unit
}
