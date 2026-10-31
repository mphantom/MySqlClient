package com.phoenix.remotedbclient.ui.query

// [改动前] 会话失效、空 SQL 与查询失败提示均为英文字面量
// [改动后] 改为 UiText 资源引用，数据库错误作为参数传入 strings.xml 占位符
// [原因] 提示文案统一由 strings.xml 提供，便于本地化
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.remotedbclient.R
import com.phoenix.remotedbclient.driver.QueryResult
import com.phoenix.remotedbclient.data.remote.RemoteDatabaseManager
import com.phoenix.remotedbclient.data.remote.SessionManager
import com.phoenix.remotedbclient.data.repository.ConnectionRepository
import com.phoenix.remotedbclient.ui.common.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class QueryUiState(
    val sqlText: String = "",
    val history: List<String> = emptyList(),
    val queryResult: QueryResult? = null,
    val isLoading: Boolean = false,
    val connectionName: String = "",
    val databaseName: String = ""
)

sealed class QueryEvent {
    data class ShowSnackbar(val message: UiText) : QueryEvent()
    data object Disconnected : QueryEvent()
}

/**
 * ViewModel for custom SQL queries.
 * Call [load] with a configId to initialize or switch connections.
 */
@HiltViewModel
class QueryViewModel @Inject constructor(
    private val repository: ConnectionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QueryUiState())
    val uiState: StateFlow<QueryUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QueryEvent>()
    val events = _events.asSharedFlow()

    private var remoteDbManager: RemoteDatabaseManager? = null
    private var currentConfigId: Long = 0
    private val maxHistorySize = 20

    /**
     * Loads/switches to the given connection.
     */
    fun load(configId: Long) {
        if (configId == currentConfigId && remoteDbManager?.isConnected == true) return
        currentConfigId = configId
        viewModelScope.launch {
            val manager = sessionManager.get(configId)
            if (manager == null || !manager.isConnected) {
                _events.emit(QueryEvent.ShowSnackbar(UiText.Res(R.string.session_not_found)))
                _events.emit(QueryEvent.Disconnected)
                return@launch
            }
            remoteDbManager = manager
            val config = manager.connectedConfig
            _uiState.value = _uiState.value.copy(
                connectionName = config?.name ?: "",
                databaseName = config?.defaultDatabase ?: "",
                queryResult = null
            )
        }
    }

    fun updateSqlText(text: String) {
        _uiState.value = _uiState.value.copy(sqlText = text)
    }

    fun selectHistory(sql: String) {
        _uiState.value = _uiState.value.copy(sqlText = sql)
    }

    fun executeQuery() {
        val sql = _uiState.value.sqlText.trim()
        if (sql.isEmpty()) {
            viewModelScope.launch {
                _events.emit(QueryEvent.ShowSnackbar(UiText.Res(R.string.error_query_empty)))
            }
            return
        }
        viewModelScope.launch {
            val manager = remoteDbManager ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = withContext(Dispatchers.IO) { manager.executeRawQuery(sql) }
            if (result.errorMessage != null) {
                // [改动前] QueryResult 与 ViewModel 同模块时可直接智能转换，上一轮因此删掉了 ?: "" 兜底
                // [改动后] 模型移到 driver-api 后，跨模块公开属性无法智能转换，用 orEmpty() 显式兜底
                // [原因] 分支内已判定非空，orEmpty() 不改变实际文案，同时保证 List<String> 可传给 List<Any>
                val detail = result.errorMessage.orEmpty()
                _events.emit(
                    QueryEvent.ShowSnackbar(
                        UiText.Res(R.string.error_query_failed, listOf(detail))
                    )
                )
            }
            val newHistory = buildList {
                if (_uiState.value.history.firstOrNull() != sql) add(sql)
                addAll(_uiState.value.history)
            }.take(maxHistorySize)
            _uiState.value = _uiState.value.copy(
                queryResult = result,
                history = newHistory,
                isLoading = false
            )
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(queryResult = null)
    }

    fun disconnect() {
        sessionManager.remove(currentConfigId)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
