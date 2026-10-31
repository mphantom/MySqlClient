package com.phoenix.remotedbclient.ui.browser

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 职责：验证 BrowserUiState.totalPages 的分页边界，避免表格页码计算出错。
 * 位置：ui/browser 的 JVM 单元测试。
 */
class BrowserUiStateTest {

    @Test
    fun `zero rows means zero pages`() {
        assertEquals(0, BrowserUiState(totalRows = 0).totalPages)
        assertEquals(0, BrowserUiState(totalRows = -5).totalPages)
    }

    @Test
    fun `partial page rounds up`() {
        assertEquals(1, BrowserUiState(totalRows = 1).totalPages)
        assertEquals(2, BrowserUiState(totalRows = 51).totalPages)
        assertEquals(2, BrowserUiState(totalRows = 99).totalPages)
    }

    @Test
    fun `exact page boundaries`() {
        assertEquals(1, BrowserUiState(totalRows = 50).totalPages)
        assertEquals(2, BrowserUiState(totalRows = 100).totalPages)
        assertEquals(3, BrowserUiState(totalRows = 150).totalPages)
    }

    @Test
    fun `page size is 50`() {
        assertEquals(50, BrowserUiState().pageSize)
    }
}
