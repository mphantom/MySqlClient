package com.phoenix.remotedbclient.driver

import com.phoenix.remotedbclient.driver.mysql.MySqlDialect
import com.phoenix.remotedbclient.driver.postgresql.PostgreSqlDialect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * 职责：验证 DriverRegistry 的查找、排序与重复 id 校验，确保新增驱动时
 * 注册表行为稳定。
 * 位置：driver 包的 JVM 单元测试。
 */
class DriverRegistryTest {

    private val registry = DriverRegistry(listOf(MySqlDialect(), PostgreSqlDialect()))

    @Test
    fun `get returns dialect by id`() {
        assertEquals("MySQL", registry.get("MySQL").id)
        assertEquals("PostgreSQL", registry.get("PostgreSQL").id)
    }

    @Test
    fun `find returns null for unknown id`() {
        assertNull(registry.find("Oracle"))
    }

    @Test
    fun `get throws for unknown id`() {
        try {
            registry.get("Oracle")
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message.orEmpty().contains("Oracle"))
        }
    }

    @Test
    fun `all is sorted by display name`() {
        assertEquals(
            listOf("MySQL", "PostgreSQL"),
            registry.all().map { it.displayName }
        )
    }

    @Test
    fun `duplicate dialect ids are rejected`() {
        try {
            DriverRegistry(listOf(MySqlDialect(), MySqlDialect()))
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message.orEmpty().contains("Duplicate"))
        }
    }
}
