package com.phoenix.remotedbclient.ui.connection

// [改动前] 表单标题、字段标签、按钮与校验错误均为英文字面量
// [改动后] 改用 stringResource 读取 strings.xml；校验错误由 UiState 的资源 ID 解析
// [原因] 统一由 strings.xml 提供文案，表单错误可随系统语言切换
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.phoenix.remotedbclient.R

/**
 * Screen for creating a new connection or editing an existing one.
 * The presence of a "configId" navigation argument determines edit mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditConnectionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditConnectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
                is AddEditEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (uiState.isEditing) R.string.edit_connection else R.string.add_connection
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Connection Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.connection_name)) },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { res -> { Text(stringResource(res)) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Database Type dropdown
            DatabaseTypeDropdown(
                selectedType = uiState.dbType,
                onTypeSelected = viewModel::updateDbType,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Host and Port side by side
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.host,
                    onValueChange = viewModel::updateHost,
                    label = { Text(stringResource(R.string.host)) },
                    isError = uiState.hostError != null,
                    supportingText = uiState.hostError?.let { res -> { Text(stringResource(res)) } },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = uiState.port,
                    onValueChange = viewModel::updatePort,
                    label = { Text(stringResource(R.string.port)) },
                    isError = uiState.portError != null,
                    supportingText = uiState.portError?.let { res -> { Text(stringResource(res)) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Username
            OutlinedTextField(
                value = uiState.username,
                onValueChange = viewModel::updateUsername,
                label = { Text(stringResource(R.string.username)) },
                isError = uiState.usernameError != null,
                supportingText = uiState.usernameError?.let { res -> { Text(stringResource(res)) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::updatePassword,
                label = { Text(stringResource(R.string.password)) },
                isError = uiState.passwordError != null,
                supportingText = {
                    val errorRes = uiState.passwordError
                    Text(
                        when {
                            errorRes != null -> stringResource(errorRes)
                            uiState.isEditing -> stringResource(R.string.password_keep_existing)
                            else -> ""
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Default Database
            OutlinedTextField(
                value = uiState.defaultDatabase,
                onValueChange = viewModel::updateDefaultDatabase,
                label = { Text(stringResource(R.string.default_database)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons row
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = viewModel::testConnection,
                    enabled = !uiState.isTesting,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(16.dp).width(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.test_connection))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = viewModel::save,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(16.dp).width(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

/**
 * Segmented button-style selector for database type (MySQL / PostgreSQL).
 */
@Composable
private fun DatabaseTypeDropdown(
    selectedType: String,
    onTypeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf(
        "MySQL" to R.string.db_type_mysql,
        "PostgreSQL" to R.string.db_type_postgresql
    )
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.database_type),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEach { (value, labelRes) ->
                OutlinedButton(
                    onClick = { onTypeSelected(value) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        stringResource(labelRes),
                        color = if (selectedType == value)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
