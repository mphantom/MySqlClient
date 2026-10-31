package com.phoenix.remotedbclient.ui.browser

// [改动前] 错误提示与增删改结果以英文文案硬编码在 ViewModel 中，数量文案写作 "row(s)"
// [改动后] 提示改为 UiText 资源引用，数量文案改用 plurals；UiState 的错误改为 @StringRes
// [原因] 文案统一由 strings.xml 提供，避免英文单复数写法与界面文案分散
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.remotedbclient.R
import com.phoenix.remotedbclient.data.remote.RemoteDatabaseManager
import com.phoenix.remotedbclient.data.remote.SessionManager
import com.phoenix.remotedbclient.data.repository.ConnectionRepository
import com.phoenix.remotedbclient.driver.ColumnInfo
import com.phoenix.remotedbclient.driver.TableInfo
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

data class BrowserUiState(
    val tables: List<TableInfo> = emptyList(),
    val selectedTable: String? = null,
    val columns: List<ColumnInfo> = emptyList(),
    val dataColumns: List<String> = emptyList(),
    val dataRows: List<List<String?>> = emptyList(),
    val currentPage: Int = 0,
    val totalRows: Int = 0,
    // [改动前] 数据表没有排序状态，翻页依赖数据库默认返回顺序
    // [改动后] 记录当前排序列与方向，默认使用主键列
    // [原因] 排序既是用户功能，也保证分页稳定
    val sortColumn: String? = null,
    val sortAscending: Boolean = true,
    val isLoadingTables: Boolean = false,
    val isLoadingColumns: Boolean = false,
    val isLoadingData: Boolean = false,
    val isExecutingMutation: Boolean = false,
    val connectionName: String = "",
    val databaseName: String = "",
    // [改动前] errorMessage: String? 由 ViewModel 直接拼英文错误文案
    // [改动后] errorMessageRes: Int? 保存 strings.xml 资源 ID，界面需要展示时再解析
    // [原因] 避免 ViewModel 硬编码文案，保持与 Compose 使用同一文案来源
    @StringRes val errorMessageRes: Int? = null,
    val configId: Long = 0
) {
    val pageSize: Int = 50
    val totalPages: Int
        get() = if (totalRows <= 0) 0 else ((totalRows - 1) / pageSize) + 1
}

sealed class BrowserEvent {
    data class ShowSnackbar(val message: UiText) : BrowserEvent()
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
                _events.emit(BrowserEvent.ShowSnackbar(UiText.Res(R.string.session_not_found)))
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
            _uiState.value = _uiState.value.copy(isLoadingTables = true, errorMessageRes = null)
            val tables = withContext(Dispatchers.IO) { manager.getTables() }
            if (tables != null) {
                _uiState.value = _uiState.value.copy(tables = tables, isLoadingTables = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingTables = false,
                    errorMessageRes = R.string.error_load_tables
                )
                _events.emit(BrowserEvent.ShowSnackbar(UiText.Res(R.string.error_load_tables)))
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
                errorMessageRes = null
            )
            val cols = withContext(Dispatchers.IO) { manager.getColumns(tableName) }
            val total = withContext(Dispatchers.IO) { manager.countRows(tableName) }

            if (cols != null) {
                val dataCols = cols.map { it.name }
                // 默认按主键排序；没有主键时退回第一列，尽量避免无 ORDER BY 的分页
                val defaultSortColumn = cols.firstOrNull { it.isPrimaryKey }?.name
                    ?: cols.firstOrNull()?.name
                _uiState.value = _uiState.value.copy(
                    columns = cols,
                    dataColumns = dataCols,
                    isLoadingColumns = false,
                    totalRows = total,
                    currentPage = 0,
                    sortColumn = defaultSortColumn,
                    sortAscending = true
                )
                loadDataInternal(tableName, 0)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoadingColumns = false,
                    isLoadingData = false,
                    errorMessageRes = R.string.error_load_columns
                )
                _events.emit(BrowserEvent.ShowSnackbar(UiText.Res(R.string.error_load_columns)))
            }
        }
    }

    fun loadData(tableName: String, page: Int) {
        viewModelScope.launch { loadDataInternal(tableName, page) }
    }

    /**
     * 点击列头时切换排序：同一列在升/降序间切换，换列时默认升序，并回到第一页。
     */
    fun sortBy(column: String) {
        val state = _uiState.value
        val tableName = state.selectedTable ?: return
        val ascending = if (state.sortColumn == column) !state.sortAscending else true
        _uiState.value = state.copy(
            sortColumn = column,
            sortAscending = ascending,
            currentPage = 0
        )
        loadData(tableName, 0)
    }

    private suspend fun loadDataInternal(tableName: String, page: Int) {
        val manager = remoteDbManager ?: return
        _uiState.value = _uiState.value.copy(isLoadingData = true)
        val state = _uiState.value
        val result = withContext(Dispatchers.IO) {
            manager.selectRows(
                tableName,
                state.dataColumns,
                page * state.pageSize,
                state.pageSize,
                state.sortColumn,
                state.sortAscending
            )
        }
        if (result.errorMessage != null) {
            // [改动前] QueryResult 与 ViewModel 同模块时可直接智能转换，上一轮因此删掉了 ?: "" 兜底
            // [改动后] 模型移到 driver-api 后，跨模块公开属性无法智能转换，用 orEmpty() 显式兜底
            // [原因] 分支内已判定非空，orEmpty() 不改变实际文案，同时保证 List<String> 可传给 List<Any>
            val detail = result.errorMessage.orEmpty()
            _events.emit(
                BrowserEvent.ShowSnackbar(
                    UiText.Res(R.string.error_load_data, listOf(detail))
                )
            )
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
                if (aff < 0) {
                    UiText.Res(R.string.error_update_cell)
                } else {
                    UiText.Plural(R.plurals.cell_updated, aff, listOf(aff))
                }
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
                if (aff < 0) {
                    UiText.Res(R.string.error_delete_row)
                } else {
                    UiText.Plural(R.plurals.row_deleted, aff, listOf(aff))
                }
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
                if (aff < 0) {
                    UiText.Res(R.string.error_insert_row)
                } else {
                    UiText.Plural(R.plurals.row_inserted, aff, listOf(aff))
                }
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
