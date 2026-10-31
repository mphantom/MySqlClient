package com.example.remotedbexplorer.ui.query

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Screen for executing custom SQL queries against the connected database.
 * Features a multi-line SQL editor, query history, and scrollable result table.
 * Supports exporting results to CSV.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryScreen(
    onDisconnected: () -> Unit,
    viewModel: QueryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is QueryEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is QueryEvent.Disconnected -> onDisconnected()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            uiState.connectionName.ifEmpty { "Query" },
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (uiState.databaseName.isNotEmpty()) {
                            Text(uiState.databaseName, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                actions = {
                    if (uiState.queryResult != null && uiState.queryResult!!.isSelectResult) {
                        IconButton(onClick = {
                            scope.launch {
                                val result = uiState.queryResult!!
                                val file = exportToCsv(context, result)
                                if (file != null) {
                                    shareFile(context, file)
                                } else {
                                    snackbarHostState.showSnackbar("Failed to export CSV")
                                }
                            }
                        }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Export CSV")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            // SQL input area
            OutlinedTextField(
                value = uiState.sqlText,
                onValueChange = viewModel::updateSqlText,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f),
                label = { Text("SQL Query") },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                ),
                maxLines = Int.MAX_VALUE,
                singleLine = false,
                supportingText = {
                    if (uiState.history.isNotEmpty()) {
                        Text("History: tap a query below to reuse")
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Execute + clear buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (uiState.queryResult != null) {
                    OutlinedButton(onClick = viewModel::clearResult) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Button(
                    onClick = viewModel::executeQuery,
                    enabled = !uiState.isLoading && uiState.sqlText.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(16.dp).width(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Execute")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Query history
            if (uiState.history.isNotEmpty() && uiState.queryResult == null) {
                Text(
                    "Recent Queries",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.15f)
                ) {
                    items(uiState.history) { sql ->
                        Text(
                            sql,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectHistory(sql) }
                                .padding(8.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Divider()
                    }
                }
            }

            // Results area
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(0.5f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.queryResult != null) {
                val result = uiState.queryResult!!
                if (result.errorMessage != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(0.5f).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            result.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else if (result.isSelectResult) {
                    QueryResultTable(
                        columns = result.columns ?: emptyList(),
                        rows = result.rows ?: emptyList(),
                        modifier = Modifier.weight(0.5f)
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(0.5f).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${result.affectedRows} row(s) affected",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Scrollable result table for SELECT query output.
 */
@Composable
private fun QueryResultTable(
    columns: List<String>,
    rows: List<List<String?>>,
    modifier: Modifier = Modifier
) {
    val horizontalScroll = rememberScrollState()
    val columnWidth = 140.dp

    Column(modifier = modifier) {
        Text(
            "${rows.size} row(s) returned",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(horizontalScroll)
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                columns.forEach { col ->
                    Text(
                        col,
                        modifier = Modifier
                            .widthIn(min = columnWidth)
                            .width(columnWidth)
                            .padding(6.dp)
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

            // Data rows
            val verticalScroll = rememberScrollState()
            Column(modifier = Modifier.verticalScroll(verticalScroll)) {
                rows.forEachIndexed { idx, row ->
                    Row(
                        modifier = Modifier.background(
                            if (idx % 2 == 0) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        row.forEach { value ->
                            Text(
                                value ?: "NULL",
                                modifier = Modifier
                                    .widthIn(min = columnWidth)
                                    .width(columnWidth)
                                    .padding(6.dp)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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

/**
 * Exports the [QueryResult] to a CSV file in the app's cache directory.
 *
 * @return The exported [File], or null on failure.
 */
private suspend fun exportToCsv(context: Context, result: com.example.remotedbexplorer.data.remote.QueryResult): File? {
    return withContext(Dispatchers.IO) {
        try {
            val file = File(context.cacheDir, "query_export_${System.currentTimeMillis()}.csv")
            file.bufferedWriter().use { writer ->
                // Header
                writer.write(result.columns?.joinToString(",") { escapeCsv(it) } ?: "")
                writer.newLine()
                // Rows
                result.rows?.forEach { row ->
                    writer.write(row.joinToString(",") { escapeCsv(it ?: "NULL") })
                    writer.newLine()
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}

/** Escapes a value for CSV output. */
private fun escapeCsv(value: String): String {
    return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
        "\"${value.replace("\"", "\"\"")}\""
    } else {
        value
    }
}

/**
 * Shares a file via Android's share intent.
 */
private fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Export CSV"))
}
