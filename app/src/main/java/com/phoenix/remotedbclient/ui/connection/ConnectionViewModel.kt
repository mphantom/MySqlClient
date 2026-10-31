package com.phoenix.remotedbclient.ui.connection

// [改动前] 连接结果与异常提示以英文文案硬编码在 ViewModel 中，界面无法本地化
// [改动后] 事件改为携带 UiText（strings.xml 资源 + 参数），由 Screen 在 Snackbar 中解析
// [原因] 文案集中到 strings.xml，避免 ViewModel 持有 Context 或散落英文字面量
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.remotedbclient.R
import com.phoenix.remotedbclient.data.db.ConnectionConfigEntity
import com.phoenix.remotedbclient.data.remote.RemoteDatabaseManager
import com.phoenix.remotedbclient.data.remote.SessionConnector
import com.phoenix.remotedbclient.data.repository.ConnectionRepository
import com.phoenix.remotedbclient.ui.common.UiText
import com.phoenix.remotedbclient.util.PasswordStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the connection list screen.
 */
data class ConnectionListUiState(
    val configs: List<ConnectionConfigEntity> = emptyList(),
    val isTestingConnection: Boolean = false
)

/**
 * One-shot UI events emitted by [ConnectionViewModel].
 */
sealed class ConnectionEvent {
    data class ShowSnackbar(val message: UiText) : ConnectionEvent()
    data class NavigateToConnection(val configId: Long, val password: String) : ConnectionEvent()
}

/**
 * ViewModel for the connection list screen.
 * Manages saved connections, deletion, and password entry for connecting.
 */
@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val repository: ConnectionRepository,
    private val securePreferences: PasswordStore,
    private val remoteDatabaseManager: RemoteDatabaseManager,
    private val sessionManager: SessionConnector
) : ViewModel() {

    /** Observable list of saved configs. */
    val configs: StateFlow<List<ConnectionConfigEntity>> = repository.observeAllConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(ConnectionListUiState())
    val uiState: StateFlow<ConnectionListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ConnectionEvent>()
    val events = _events.asSharedFlow()

    /**
     * Returns the saved password for [configId], or null if not stored.
     */
    fun getSavedPassword(configId: Long): String? {
        return securePreferences.getPassword(configId)
    }

    /**
     * Attempts to connect to the database using [config] and [password].
     * On success the session manager holds an actually connected manager and
     * [ConnectionEvent.NavigateToConnection] is emitted.
     * On failure the session is removed and [ConnectionEvent.ShowSnackbar] is emitted.
     *
     * [改动前] 只调用 testConnection(临时建连后立即关闭)，会话管理器里仍是未连接实例，
     *          进入 Browse/Query 后会报 “Session not found” 并退回列表
     * [改动后] 直接通过 SessionManager.getOrCreate(...).connect(...) 建立并保留连接，
     *          成功后才导航；失败时移除半初始化会话
     * [原因] 修复主链路断点，保证工作区拿到的会话处于已连接状态
     */
    fun connectToDatabase(config: ConnectionConfigEntity, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true)
            try {
                sessionManager.getOrCreate(config.id).connect(config, password)
                // Save password for future use
                securePreferences.savePassword(config.id, password)
                _events.emit(ConnectionEvent.NavigateToConnection(config.id, password))
            } catch (e: Exception) {
                sessionManager.remove(config.id)
                val detail = e.message
                _events.emit(
                    ConnectionEvent.ShowSnackbar(
                        if (detail != null) {
                            UiText.Res(R.string.connection_failed, listOf(detail))
                        } else {
                            UiText.Res(R.string.error_unexpected)
                        }
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isTestingConnection = false)
            }
        }
    }

    /**
     * Deletes a connection config and its stored password.
     */
    fun deleteConnection(config: ConnectionConfigEntity) {
        viewModelScope.launch {
            repository.deleteConfig(config)
            securePreferences.deletePassword(config.id)
        }
    }

    /**
     * Tests a connection without navigating — just shows success/failure via snackbar.
     */
    fun testConnectionOnly(config: ConnectionConfigEntity, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true)
            try {
                val error = remoteDatabaseManager.testConnection(config, password)
                if (error == null) {
                    securePreferences.savePassword(config.id, password)
                    _events.emit(ConnectionEvent.ShowSnackbar(UiText.Res(R.string.connection_test_success)))
                } else {
                    _events.emit(
                        ConnectionEvent.ShowSnackbar(
                            UiText.Res(R.string.connection_test_failed, listOf(error))
                        )
                    )
                }
            } catch (e: Exception) {
                val detail = e.message
                _events.emit(
                    ConnectionEvent.ShowSnackbar(
                        if (detail != null) {
                            UiText.Res(R.string.connection_test_failed, listOf(detail))
                        } else {
                            UiText.Res(R.string.connection_test_failed_no_detail)
                        }
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isTestingConnection = false)
            }
        }
    }
}
