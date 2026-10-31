package com.phoenix.remotedbclient.ui.browser

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
 * 职责：验证 BrowserViewModel 在会话不存在时先提示、再发出 Disconnected 事件，
 * 保证工作区能回到连接列表而不是停留在空页面。
 * 位置：ui/browser 的 JVM 单元测试。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BrowserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(): BrowserViewModel {
        val repository = ConnectionRepository(FakeConnectionConfigDao())
        return BrowserViewModel(repository, SessionManager(testDriverRegistry()))
    }

    @Test
    fun `missing session emits disconnect and message`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        val events = mutableListOf<BrowserEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events += it }
        }

        viewModel.load(42L)
        advanceUntilIdle()

        assertTrue(events.any { it is BrowserEvent.Disconnected })
        val snackbar = events.filterIsInstance<BrowserEvent.ShowSnackbar>().single()
        assertEquals(UiText.Res(R.string.session_not_found), snackbar.message)
    }

    @Test
    fun `loading twice re-attempts when session is missing`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        val events = mutableListOf<BrowserEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.collect { events += it }
        }

        viewModel.load(42L)
        advanceUntilIdle()
        val eventCountAfterFirstLoad = events.size

        viewModel.load(42L)
        advanceUntilIdle()

        // 第一次加载失败后 remoteDbManager 仍为 null，因此第二次会再次尝试并再次提示；
        // 这里只断言重复调用不会崩溃且仍然给出断开事件。
        assertEquals(eventCountAfterFirstLoad + 2, events.size)
        assertEquals(2, events.count { it is BrowserEvent.ShowSnackbar })
        assertEquals(2, events.count { it is BrowserEvent.Disconnected })
    }
}
