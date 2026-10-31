package com.example.remotedbexplorer.ui.browser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remotedbexplorer.data.remote.ColumnInfo
import com.example.remotedbexplorer.data.remote.RemoteDatabaseManager
import com.example.remotedbexplorer.data.remote.SessionManager
import com.example.remotedbexplorer.data.remote.TableInfo
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

data class BrowserUiState(
    val tables: List<TableInfo> = emptyList(),
    val selectedTable: String? = null,
    val columns: List<ColumnInfo> = emptyList(),
    val dataColumns: List<String> = emptyList(),
    val dataRows: List<List<String?>> = emptyList(),
    val currentPage: Int = 0,
    val totalRows: Int = 0,
    val isLoadingTables: Boolean = false,
    val isLoadingColumns: Boolean = false,
    val isLoadingData: Boolean = false,
    val isExecutingMutation: Boolean = false,
    val connectionName: String = "",
    val databaseName: String = "",
    val errorMessage: String? = null,
    val configId: Long = 0
) {
    val pageSize: Int = 50
    val totalPages: Int
        get() = if (totalRows <= 0) 0 else ((totalRows - 1) / pageSize) + 1
}

sealed class BrowserEvent {
    data class ShowSnackbar(val message: String) : BrowserEvent()
    data object Disconnected : BrowserEvent()
}

/**
 * ViewModel for browsing database structure and data.
 * Call [load] with a configId to initialize or switch to a different connection.
 */
@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val repository: ConnectionRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<BrowserEvent>()
    val events = _events.asSharedFlow()

    private var remoteDbManager: RemoteDatabaseManager? = null
    private var currentConfigId: Long = 0

    /**
     * Loads/switches to the given connection config.
     * Safe to call multiple times with the same or different IDs.
     */
    fun load(configId: Long) {
        if (configId == currentConfigId && remoteDbManager?.isConnected == true) return
        currentConfigId = configId
        viewModelScope.launch {
            val manager = sessionManager.get(configId)
            if (manager == null || !manager.isConnected) {
                _events.emit(BrowserEvent.ShowSnackbar("Session not found. Please reconnect."))
                _events.emit(BrowserEvent.Disconnected)
                return@launch
            }
            remoteDbManager = manager
            val config = manager.connectedConfig
            _uiState.value = BrowserUiState(
                configId = configId,
                connectionName = config?.name ?: "",
                databaseName = config?.defaultDatabase ?: ""
            )
            loadTables()
        }
    }

    fun loadTables() {
        viewModelScope.launch {
            val manager = remoteDbManager ?: return@launch
            _uiState.value = _uiState.value.copy(isLoadingTables = true, errorMessage = null)
            val tables = withContext(Dispatchers.IO) { manager.getTables() }
            if (tables != null) {
                _uiState.value = _uiState.value.copy(tables = tables, isLoadingTables = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingTables = false,
                    errorMessage = "Failed to load tables"
                )
                _events.emit(BrowserEvent.ShowSnackbar("Failed to load tables"))
            }
        }
    }

    fun selectTable(tableName: String) {
        viewModelScope.launch {
            val manager = remoteDbManager ?: return@launch
            _uiState.value = _uiState.value.copy(
                selectedTable = tableName,
                isLoadingColumns = true,
                isLoadingData = true,
                errorMessage = null
            )
            val cols = withContext(Dispatchers.IO) { manager.getColumns(tableName) }
            val total = withContext(Dispatchers.IO) { manager.countRows(tableName) }

            if (cols != null) {
                val dataCols = cols.map { it.name }
                _uiState.value = _uiState.value.copy(
                    columns = cols,
                    dataColumns = dataCols,
                    isLoadingColumns = false,
                    totalRows = total,
                    currentPage = 0
                )
                loadDataInternal(tableName, 0)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingColumns = false,
                    isLoadingData = false,
                    errorMessage = "Failed to load column info"
                )
                _events.emit(BrowserEvent.ShowSnackbar("Failed to load column information"))
            }
        }
    }

    fun loadData(tableName: String, page: Int) {
        viewModelScope.launch { loadDataInternal(tableName, page) }
    }

    private suspend fun loadDataInternal(tableName: String, page: Int) {
        val manager = remoteDbManager ?: return
        _uiState.value = _uiState.value.copy(isLoadingData = true)
        val state = _uiState.value
        val result = withContext(Dispatchers.IO) {
            manager.selectRows(tableName, state.dataColumns, page * state.pageSize, state.pageSize)
        }
        if (result.errorMessage != null) {
            _events.emit(BrowserEvent.ShowSnackbar("Failed to load data: ${result.errorMessage}"))
            _uiState.value = _uiState.value.copy(isLoadingData = false)
        } else {
            _uiState.value = _uiState.value.copy(
                dataRows = result.rows ?: emptyList(),
                dataColumns = result.columns ?: state.dataColumns,
                isLoadingData = false,
                currentPage = page
            )
        }
    }

    fun nextPage() {
        val s = _uiState.value; val t = s.selectedTable ?: return
        if (s.currentPage < s.totalPages - 1) loadData(t, s.currentPage + 1)
    }

    fun previousPage() {
        val s = _uiState.value; val t = s.selectedTable ?: return
        if (s.currentPage > 0) loadData(t, s.currentPage - 1)
    }

    fun updateCell(tableName: String, columnName: String, newValue: String?,
                   primaryKeyCols: List<String>, primaryKeyVals: List<String?>) {
        viewModelScope.launch {
            val m = remoteDbManager ?: return@launch
            _uiState.value = _uiState.value.copy(isExecutingMutation = true)
            val aff = withContext(Dispatchers.IO) {
                m.updateCell(tableName, columnName, newValue, primaryKeyCols, primaryKeyVals)
            }
            _events.emit(BrowserEvent.ShowSnackbar(
                if (aff < 0) "Failed to update cell" else "Cell updated ($aff row(s))"
            ))
            if (aff >= 0) loadData(tableName, _uiState.value.currentPage)
            _uiState.value = _uiState.value.copy(isExecutingMutation = false)
        }
    }

    fun deleteRow(tableName: String, primaryKeyCols: List<String>, primaryKeyVals: List<String?>) {
        viewModelScope.launch {
            val m = remoteDbManager ?: return@launch
            _uiState.value = _uiState.value.copy(isExecutingMutation = true)
            val aff = withContext(Dispatchers.IO) { m.deleteRow(tableName, primaryKeyCols, primaryKeyVals) }
            _events.emit(BrowserEvent.ShowSnackbar(
                if (aff < 0) "Failed to delete row" else "Row deleted ($aff row(s))"
            ))
            if (aff >= 0) {
                val nt = withContext(Dispatchers.IO) { m.countRows(tableName) }
                _uiState.value = _uiState.value.copy(totalRows = nt)
                loadData(tableName, _uiState.value.currentPage)
            }
            _uiState.value = _uiState.value.copy(isExecutingMutation = false)
        }
    }

    fun insertRow(tableName: String, columnValues: Map<String, String?>) {
        viewModelScope.launch {
            val m = remoteDbManager ?: return@launch
            _uiState.value = _uiState.value.copy(isExecutingMutation = true)
            val aff = withContext(Dispatchers.IO) { m.insertRow(tableName, columnValues) }
            _events.emit(BrowserEvent.ShowSnackbar(
                if (aff < 0) "Failed to insert row" else "Row inserted ($aff row(s))"
            ))
            if (aff >= 0) {
                val nt = withContext(Dispatchers.IO) { m.countRows(tableName) }
                _uiState.value = _uiState.value.copy(totalRows = nt)
                val lp = if (nt <= 0) 0 else (nt - 1) / _uiState.value.pageSize
                loadData(tableName, lp)
            }
            _uiState.value = _uiState.value.copy(isExecutingMutation = false)
        }
    }

    fun disconnect() {
        sessionManager.remove(currentConfigId)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
