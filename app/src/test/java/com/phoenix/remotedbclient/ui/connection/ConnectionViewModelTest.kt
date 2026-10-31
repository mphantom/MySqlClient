package com.phoenix.remotedbclient.ui.connection

import com.phoenix.remotedbclient.R
import com.phoenix.remotedbclient.data.db.ConnectionConfigEntity
import com.phoenix.remotedbclient.data.remote.RemoteDatabaseManager
import com.phoenix.remotedbclient.data.repository.ConnectionRepository
import com.phoenix.remotedbclient.testutil.FakeConnectionConfigDao
import com.phoenix.remotedbclient.testutil.FakeDatabaseSession
import com.phoenix.remotedbclient.testutil.FakePasswordStore
import com.phoenix.remotedbclient.testutil.FakeSessionConnector
import com.phoenix.remotedbclient.testutil.MainDispatcherRule
import com.phoenix.remotedbclient.testutil.testDriverRegistry
import com.phoenix.remotedbclient.ui.common.UiText
import com.phoenix.remotedbclient.util.PasswordStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.sql.SQLException

/**
 * 职责：验证主链路的关键时序——连接成功后会话已建立、密码已保存、发出导航事件；
 * 连接失败时会话被移除且不导航。
 * 位置：ui/connection 的 JVM 集成测试（使用假会话，不依赖真实数据库）。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val config = ConnectionConfigEntity(
        id = 7L,
        name = "Test connection",
        dbType = "MySQL",
        host = "127.0.0.1",
        port = 3306,
        username = "root"
    )

    private fun createViewModel(
        sessionConnector: FakeSessionConnector,
        passwordStore: PasswordStore
    ): ConnectionViewModel {
        return ConnectionViewModel(
            repository = ConnectionRepository(FakeConnectionConfigDao()),
            securePreferences = passwordStore,
            remoteDatabaseManager = RemoteDatabaseManager(testDriverRegistry()),
            sessionManager = sessionConnector
        )
    }

    @Test
    fun `successful connect establishes session before navigation`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val session = FakeDatabaseSession()
            val sessionConnector = FakeSessionConnector(session)
            val passwordStore = FakePasswordStore()
            val viewModel = createViewModel(sessionConnector, passwordStore)
            val events = mutableListOf<ConnectionEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.events.collect { events += it }
            }

            viewModel.connectToDatabase(config, "secret")
            advanceUntilIdle()

            assertTrue(session.connected)
            assertEquals(7L, sessionConnector.requestedConfigId)
            assertEquals("secret", passwordStore.getPassword(7L))
            val navigate = events.filterIsInstance<ConnectionEvent.NavigateToConnection>().single()
            assertEquals(7L, navigate.configId)
            assertEquals("secret", navigate.password)
            assertTrue(events.none { it is ConnectionEvent.ShowSnackbar })
        }

    @Test
    fun `failed connect removes session and does not navigate`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val session = FakeDatabaseSession().apply {
                failure = SQLException("boom")
            }
            val sessionConnector = FakeSessionConnector(session)
            val passwordStore = FakePasswordStore()
            val viewModel = createViewModel(sessionConnector, passwordStore)
            val events = mutableListOf<ConnectionEvent>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.events.collect { events += it }
            }

            viewModel.connectToDatabase(config, "secret")
            advanceUntilIdle()

            assertFalse(session.connected)
            assertTrue(sessionConnector.removed)
            assertTrue(passwordStore.getPassword(7L) == null)
            assertTrue(events.none { it is ConnectionEvent.NavigateToConnection })
            val snackbar = events.filterIsInstance<ConnectionEvent.ShowSnackbar>().single()
            assertEquals(
                UiText.Res(R.string.connection_failed, listOf("boom")),
                snackbar.message
            )
        }
}
