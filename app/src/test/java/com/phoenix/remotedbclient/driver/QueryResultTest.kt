package com.phoenix.remotedbclient.driver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 职责：验证 QueryResult 的 SELECT / DML / 错误三种工厂与默认值，
 * 确保查询结果建模不会在重构中悄悄改变。
 * 位置：driver 包的 JVM 单元测试。
 *
 * [改动前] 位于 data/remote，测试 app 内部的 QueryResult
 * [改动后] 随 QueryResult 迁移到 driver-api，测试 driver 包
 * [原因] 模型已移到驱动 API，测试随之迁移
 */
class QueryResultTest {

    @Test
    fun `selectResult keeps columns and rows`() {
        val rows = listOf(listOf("1", "Alice"), listOf("2", null))
        val result = QueryResult.selectResult(listOf("id", "name"), rows)

        assertTrue(result.isSelectResult)
        assertEquals(listOf("id", "name"), result.columns)
        assertEquals(rows, result.rows)
        assertEquals(0, result.affectedRows)
        assertNull(result.errorMessage)
    }

    @Test
    fun `updateResult reports affected rows`() {
        val result = QueryResult.updateResult(3)

        assertFalse(result.isSelectResult)
        assertEquals(3, result.affectedRows)
        assertNull(result.columns)
        assertNull(result.rows)
        assertNull(result.errorMessage)
    }

    @Test
    fun `error carries message`() {
        val result = QueryResult.error("boom")

        assertEquals("boom", result.errorMessage)
        assertFalse(result.isSelectResult)
        assertEquals(0, result.affectedRows)
        assertNull(result.columns)
        assertNull(result.rows)
    }

    @Test
    fun `default result is empty`() {
        val result = QueryResult()

        assertNull(result.columns)
        assertNull(result.rows)
        assertEquals(0, result.affectedRows)
        assertFalse(result.isSelectResult)
        assertNull(result.errorMessage)
    }
}
