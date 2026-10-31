package com.example.remotedbexplorer.ui.query

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remotedbexplorer.data.remote.QueryResult
import com.example.remotedbexplorer.data.remote.RemoteDatabaseManager
import com.example.remotedbexplorer.data.remote.SessionManager
import com.example.remotedbexplorer.data.repository.ConnectionRepository
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
    data class ShowSnackbar(val message: String) : QueryEvent()
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
                _events.emit(QueryEvent.ShowSnackbar("Session not found. Please reconnect."))
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
            viewModelScope.launch { _events.emit(QueryEvent.ShowSnackbar("Please enter a SQL statement")) }
            return
        }
        viewModelScope.launch {
            val manager = remoteDbManager ?: return@launch
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = withContext(Dispatchers.IO) { manager.executeRawQuery(sql) }
            if (result.errorMessage != null) {
                _events.emit(QueryEvent.ShowSnackbar("Query error: ${result.errorMessage}"))
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
