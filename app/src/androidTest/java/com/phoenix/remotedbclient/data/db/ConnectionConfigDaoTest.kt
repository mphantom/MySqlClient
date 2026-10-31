package com.phoenix.remotedbclient.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 职责：在内存 Room 数据库上验证连接配置的增删改查与按名称排序，
 * 不依赖真实设备存储或网络。
 * 位置：data/db 的 instrumented 测试。
 */
@RunWith(AndroidJUnit4::class)
class ConnectionConfigDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ConnectionConfigDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.connectionConfigDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndReadById() = runBlocking {
        val id = dao.insert(config("Production"))

        val loaded = dao.getById(id)

        assertTrue(id > 0)
        assertEquals("Production", loaded?.name)
        assertEquals("MySQL", loaded?.dbType)
        assertEquals(3306, loaded?.port)
    }

    @Test
    fun observeAllSortedByName() = runBlocking {
        dao.insert(config("Zeta"))
        dao.insert(config("Alpha"))
        dao.insert(config("Mike"))

        val names = dao.observeAll().first().map { it.name }

        assertEquals(listOf("Alpha", "Mike", "Zeta"), names)
    }

    @Test
    fun updateChangesFields() = runBlocking {
        val id = dao.insert(config("Old name"))
        val existing = requireNotNull(dao.getById(id))

        dao.update(existing.copy(name = "New name", dbType = "PostgreSQL", port = 5432))

        val updated = requireNotNull(dao.getById(id))
        assertEquals("New name", updated.name)
        assertEquals("PostgreSQL", updated.dbType)
        assertEquals(5432, updated.port)
    }

    @Test
    fun deleteRemovesRow() = runBlocking {
        val id = dao.insert(config("Temporary"))
        val entity = requireNotNull(dao.getById(id))

        dao.delete(entity)

        assertNull(dao.getById(id))
        assertTrue(dao.observeAll().first().isEmpty())
    }

    private fun config(name: String) = ConnectionConfigEntity(
        name = name,
        dbType = "MySQL",
        host = "127.0.0.1",
        port = 3306,
        username = "root"
    )
}
