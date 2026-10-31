package com.phoenix.remotedbclient.ui.connection

// [改动前] 列表页标题、空状态、密码/删除对话框与无障碍描述均为英文字面量
// [改动后] 全部改为 stringResource 读取 strings.xml，带连接名/主机等参数时使用占位符
// [原因] 文案统一由 strings.xml 提供，支持本地化与屏幕阅读器
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoenix.remotedbclient.R
import com.phoenix.remotedbclient.data.db.ConnectionConfigEntity

/**
 * Displays the list of saved database connections.
 * Each item shows connection name, type icon, and host.
 * Click to connect, long-press to edit, swipe/tap delete icon to remove.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToBrowser: (Long, String) -> Unit,
    viewModel: ConnectionViewModel = hiltViewModel()
) {
    val configs by viewModel.configs.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // One-shot event handling
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ConnectionEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
                is ConnectionEvent.NavigateToConnection -> onNavigateToBrowser(event.configId, event.password)
            }
        }
    }

    // Password dialog state
    var passwordDialogConfig by remember { mutableStateOf<ConnectionConfigEntity?>(null) }
    var passwordInput by remember { mutableStateOf("") }
    var deleteConfirmConfig by remember { mutableStateOf<ConnectionConfigEntity?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                // [改动前] 标题硬编码为 "RemoteDB Client"（上一轮改名时仍保留字面量）
                // [改动后] 改为 stringResource(R.string.connection_list_title)，从 strings.xml 读取
                // [原因] 原注释中的"后续可替换"已完成；标题不再硬编码，可随系统语言切换
                title = { Text(stringResource(R.string.connection_list_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.add_connection),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (configs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_connections),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(configs, key = { it.id }) { config ->
                    ConnectionCard(
                        config = config,
                        hasSavedPassword = viewModel.getSavedPassword(config.id) != null,
                        onClick = {
                            val savedPw = viewModel.getSavedPassword(config.id)
                            if (savedPw != null) {
                                viewModel.connectToDatabase(config, savedPw)
                            } else {
                                passwordDialogConfig = config
                                passwordInput = ""
                            }
                        },
                        onEdit = { onNavigateToEdit(config.id) },
                        onDelete = { deleteConfirmConfig = config }
                    )
                }
            }
        }
    }

    // Password entry dialog
    passwordDialogConfig?.let { config ->
        AlertDialog(
            onDismissRequest = {
                passwordDialogConfig = null
                passwordInput = ""
            },
            title = { Text(stringResource(R.string.enter_password)) },
            text = {
                Column {
                    Text(
                        stringResource(R.string.connect_to_connection, config.name, config.host),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text(stringResource(R.string.password)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pw = passwordInput
                        passwordDialogConfig = null
                        passwordInput = ""
                        viewModel.connectToDatabase(config, pw)
                    },
                    enabled = passwordInput.isNotBlank()
                ) {
                    Text(stringResource(R.string.connect))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    passwordDialogConfig = null
                    passwordInput = ""
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete confirmation dialog
    deleteConfirmConfig?.let { config ->
        AlertDialog(
            onDismissRequest = { deleteConfirmConfig = null },
            title = { Text(stringResource(R.string.delete_confirm_title)) },
            text = { Text(stringResource(R.string.delete_confirm_message, config.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteConnection(config)
                    deleteConfirmConfig = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmConfig = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

/**
 * Card component for a single saved connection.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConnectionCard(
    config: ConnectionConfigEntity,
    hasSavedPassword: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onEdit
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (hasSavedPassword) Icons.Filled.Storage else Icons.Outlined.Storage,
                contentDescription = config.dbType,
                modifier = Modifier.size(40.dp),
                tint = if (config.dbType == "MySQL")
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.tertiary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = config.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.connection_summary, config.dbType, config.host, config.port),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (config.defaultDatabase.isNotEmpty()) {
                    Text(
                        text = config.defaultDatabase,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_connection),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
