package com.phoenix.remotedbclient.ui.query

import com.phoenix.remotedbclient.R
import com.phoenix.remotedbclient.data.remote.SessionManager
import com.phoenix.remotedbclient.data.repository.ConnectionRepository
import com.phoenix.remotedbclient.testutil.FakeConnectionConfigDao
import com.phoenix.remotedbclient.testutil.MainDispatcherRule
import com.phoenix.remotedbclient.testutil.testDriverRegistry
import com.phoenix.remotedbclient.ui.common.UiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * 职责：验证 QueryViewModel 的空 SQL 校验与“会话不存在”分支，
 * 这两条路径不依赖真实 JDBC 连接。
 * 位置：ui/query 的 JVM 单元测试。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QueryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(): QueryViewModel {
        val repository = ConnectionRepository(FakeConnectionConfigDao())
        return QueryViewModel(repository, SessionManager(testDriverRegistry()))
    }

    @Test
    fun `empty SQL emits validation snackbar`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        val events = mutableListOf<QueryEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events += it }
        }

        viewModel.executeQuery()
        advanceUntilIdle()

        val snackbar = events.single() as QueryEvent.ShowSnackbar
        assertEquals(UiText.Res(R.string.error_query_empty), snackbar.message)
    }

    @Test
    fun `missing session emits disconnect and message`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        val events = mutableListOf<QueryEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events += it }
        }

        viewModel.load(42L)
        advanceUntilIdle()

        assertTrue(events.any { it is QueryEvent.Disconnected })
        val snackbar = events.filterIsInstance<QueryEvent.ShowSnackbar>().single()
        assertEquals(UiText.Res(R.string.session_not_found), snackbar.message)
    }
}
