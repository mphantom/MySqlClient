package com.phoenix.remotedbclient.ui.browser

// [改动前] 浏览页的标签、顶部标题、对话框、表头、分页与空状态均为英文字面量
// [改动后] 全部改为 stringResource 读取 strings.xml，数量与列名等动态值通过占位符传入
// [原因] 文案集中到 strings.xml，支持本地化与无障碍描述
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.ViewColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoenix.remotedbclient.R
import com.phoenix.remotedbclient.data.remote.ColumnInfo
import com.phoenix.remotedbclient.data.remote.TableInfo
import kotlinx.coroutines.launch

/**
 * Screen for browsing database structure (tables, columns) and table data.
 * Provides tabbed interface for Tables/Columns/Data views.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DatabaseBrowserScreen(
    onDisconnected: () -> Unit,
    viewModel: BrowserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Event handling
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BrowserEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
                is BrowserEvent.Disconnected -> onDisconnected()
            }
        }
    }

    // Tab state: 0=Tables, 1=Columns, 2=Data
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tables),
        stringResource(R.string.columns),
        stringResource(R.string.data)
    )

    // Dialogs
    var showAddRowDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<String?>(null) }
    var editColName by remember { mutableStateOf("") }
    var editValue by remember { mutableStateOf("") }
    var editPkCols by remember { mutableStateOf<List<String>>(emptyList()) }
    var editPkVals by remember { mutableStateOf<List<String?>>(emptyList()) }
    var showDeleteConfirm by remember { mutableStateOf<List<String>?>(null) }
    var deletePkCols by remember { mutableStateOf<List<String>>(emptyList()) }
    var deletePkVals by remember { mutableStateOf<List<String?>>(emptyList()) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            uiState.connectionName.ifEmpty { stringResource(R.string.browser) },
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (uiState.databaseName.isNotEmpty()) {
                            Text(
                                uiState.databaseName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadTables() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == 2 && uiState.selectedTable != null) {
                FloatingActionButton(onClick = { showAddRowDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_row))
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Table selector + tabs
            if (uiState.isLoadingTables) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.tables.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_tables), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Table list as horizontally scrollable chips
                LazyColumn(
                    modifier = Modifier
                        .weight(if (selectedTab == 0) 1f else 0.3f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    items(uiState.tables) { table ->
                        val isSelected = table.name == uiState.selectedTable
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectTable(table.name) }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (table.type == "VIEW") Icons.Outlined.ViewColumn
                                else Icons.Outlined.TableChart,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column {
                                Text(
                                    table.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    table.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Divider()
                    }
                }

                // Content area based on selected tab
                if (uiState.selectedTable != null) {
                    TabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    when (selectedTab) {
                        0 -> { /* Tables already shown above */ }
                        1 -> ColumnsTab(uiState.columns, uiState.isLoadingColumns)
                        2 -> DataTab(
                            columns = uiState.dataColumns,
                            rows = uiState.dataRows,
                            pkColumns = uiState.columns.filter { it.isPrimaryKey }.map { it.name },
                            currentPage = uiState.currentPage,
                            totalPages = uiState.totalPages,
                            totalRows = uiState.totalRows,
                            isLoading = uiState.isLoadingData,
                            onPreviousPage = viewModel::previousPage,
                            onNextPage = viewModel::nextPage,
                            onCellClick = { row, colName, value ->
                                val pkCols = uiState.columns.filter { it.isPrimaryKey }
                                if (pkCols.isEmpty()) {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.error_no_primary_key_edit)) }
                                    return@DataTab
                                }
                                editColName = colName
                                editValue = value ?: ""
                                editPkCols = pkCols.map { it.name }
                                editPkVals = pkCols.map { pk -> row[uiState.dataColumns.indexOf(pk.name)] }
                                showEditDialog = colName
                            },
                            onRowLongClick = { row ->
                                val pkCols = uiState.columns.filter { it.isPrimaryKey }
                                if (pkCols.isEmpty()) {
                                    scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.error_no_primary_key_delete)) }
                                    return@DataTab
                                }
                                deletePkCols = pkCols.map { it.name }
                                deletePkVals = pkCols.map { pk -> row[uiState.dataColumns.indexOf(pk.name)] }
                                showDeleteConfirm = deletePkCols
                            }
                        )
                    }
                } else if (selectedTab != 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.select_table_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // --- Dialogs ---

    // Edit cell dialog
    showEditDialog?.let { col ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text(stringResource(R.string.edit_cell_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.edit_cell_column, col), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editValue,
                        onValueChange = { editValue = it },
                        label = { Text(stringResource(R.string.new_value)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateCell(
                        uiState.selectedTable!!,
                        editColName,
                        editValue.ifBlank { null },
                        editPkCols,
                        editPkVals
                    )
                    showEditDialog = null
                }) { Text(stringResource(R.string.update)) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Delete row confirmation
    showDeleteConfirm?.let {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text(stringResource(R.string.delete_row_title)) },
            text = { Text(stringResource(R.string.delete_row_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteRow(uiState.selectedTable!!, deletePkCols, deletePkVals)
                    showDeleteConfirm = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    // Add row dialog
    if (showAddRowDialog && uiState.selectedTable != null) {
        AddRowDialog(
            columns = uiState.columns,
            onDismiss = { showAddRowDialog = false },
            onInsert = { values ->
                viewModel.insertRow(uiState.selectedTable!!, values)
                showAddRowDialog = false
            }
        )
    }
}

/**
 * Displays column metadata for the selected table.
 */
@Composable
private fun ColumnsTab(columns: List<ColumnInfo>, isLoading: Boolean) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (columns.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_columns), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            item {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp)
                ) {
                    Text(stringResource(R.string.row_number), Modifier.width(30.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(stringResource(R.string.column_name), Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(stringResource(R.string.column_type), Modifier.weight(2f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(stringResource(R.string.primary_key), Modifier.width(30.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(stringResource(R.string.null_short), Modifier.width(36.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            items(columns) { col ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Text(
                        col.ordinalPosition.toString(),
                        Modifier.width(30.dp),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(col.name, Modifier.weight(2f), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(col.dataType, Modifier.weight(2f), fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        if (col.isPrimaryKey) stringResource(R.string.yes) else "",
                        Modifier.width(30.dp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        if (col.isNullable) stringResource(R.string.yes) else stringResource(R.string.no),
                        Modifier.width(36.dp),
                        fontSize = 12.sp
                    )
                }
                Divider()
            }
        }
    }
}

/**
 * Scrollable data grid for browsing table rows with pagination.
 * Supports horizontal+vertical scroll, cell click to edit, long-press row to delete.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DataTab(
    columns: List<String>,
    rows: List<List<String?>>,
    pkColumns: List<String>,
    currentPage: Int,
    totalPages: Int,
    totalRows: Int,
    isLoading: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onCellClick: (List<String?>, String, String?) -> Unit,
    onRowLongClick: (List<String?>) -> Unit
) {
    val columnWidth = 150.dp
    val rowHeaderWidth = 40.dp
    val horizontalScroll = rememberScrollState()
    val nullLabel = stringResource(R.string.null_value)

    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (rows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_data), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            // Pagination controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousPage, enabled = currentPage > 0) {
                    Icon(Icons.Default.KeyboardArrowLeft, stringResource(R.string.previous))
                }
                Text(
                    stringResource(R.string.page_indicator, currentPage + 1, totalPages, totalRows),
                    style = MaterialTheme.typography.bodySmall
                )
                IconButton(onClick = onNextPage, enabled = currentPage < totalPages - 1) {
                    Icon(Icons.Default.KeyboardArrowRight, stringResource(R.string.next))
                }
            }

            // Data table with header
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(horizontalScroll)
            ) {
                // Header row (sticky)
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(0.dp)
                ) {
                    // Row number header
                    Text(
                        stringResource(R.string.row_number),
                        modifier = Modifier
                            .width(rowHeaderWidth)
                            .padding(4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    columns.forEach { col ->
                        Text(
                            col,
                            modifier = Modifier
                                .widthIn(min = columnWidth)
                                .width(columnWidth)
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Divider(thickness = 2.dp)

                // Data rows (vertically scrollable)
                val verticalScrollState = rememberScrollState()
                Column(modifier = Modifier.verticalScroll(verticalScrollState)) {
                    rows.forEachIndexed { rowIndex, row ->
                        val rowNum = currentPage * 50 + rowIndex + 1
                        Row(
                            modifier = Modifier
                                .background(
                                    if (rowIndex % 2 == 0) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                )
                                .combinedClickable(
                                    onClick = { /* selection highlight could go here */ },
                                    onLongClick = { onRowLongClick(row) }
                                )
                        ) {
                            // Row number
                            Text(
                                rowNum.toString(),
                                modifier = Modifier
                                    .width(rowHeaderWidth)
                                    .padding(4.dp),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            // Cells
                            row.forEachIndexed { colIndex, value ->
                                val colName = columns.getOrElse(colIndex) { "" }
                                val isPk = colName in pkColumns
                                Text(
                                    value ?: nullLabel,
                                    modifier = Modifier
                                        .widthIn(min = columnWidth)
                                        .width(columnWidth)
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                        .clickable { onCellClick(row, colName, value) },
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (isPk) FontWeight.Bold else FontWeight.Normal,
                                    color = if (value == null) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Divider(thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

/**
 * Dialog for inserting a new row. Shows a text field for each column.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRowDialog(
    columns: List<ColumnInfo>,
    onDismiss: () -> Unit,
    onInsert: (Map<String, String?>) -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var values by remember { mutableStateOf(columns.associate { it.name to "" }) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(R.string.add_row_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            columns.forEach { col ->
                OutlinedTextField(
                    value = values[col.name] ?: "",
                    onValueChange = { newVal -> values = values + (col.name to newVal) },
                    label = {
                        Text(
                            stringResource(
                                if (col.isPrimaryKey) R.string.add_row_field_pk else R.string.add_row_field,
                                col.name,
                                col.dataType
                            )
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !col.isPrimaryKey || values[col.name].isNullOrBlank()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val nonEmpty = values.mapValues { (_, v) -> v.ifBlank { null } }
                    onInsert(nonEmpty)
                }) { Text(stringResource(R.string.insert)) }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
