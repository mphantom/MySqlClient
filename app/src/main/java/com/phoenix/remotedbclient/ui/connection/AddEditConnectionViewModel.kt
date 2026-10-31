package com.phoenix.remotedbclient.ui.connection

// [改动前] 表单校验错误与保存/测试提示均为英文文案字符串，硬编码在 ViewModel 中
// [改动后] 校验错误改用 @StringRes 资源 ID，Snackbar 事件改用 UiText，由界面解析
// [原因] 表单文案与提示统一由 strings.xml 提供，支持本地化并避免两套文案来源
import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phoenix.remotedbclient.R
import com.phoenix.remotedbclient.data.db.ConnectionConfigEntity
import com.phoenix.remotedbclient.data.remote.RemoteDatabaseManager
import com.phoenix.remotedbclient.data.repository.ConnectionRepository
import com.phoenix.remotedbclient.driver.DatabaseDialect
import com.phoenix.remotedbclient.driver.DriverRegistry
import com.phoenix.remotedbclient.ui.common.UiText
import com.phoenix.remotedbclient.util.PasswordStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Form state for adding or editing a connection configuration.
 */
data class AddEditUiState(
    val name: String = "",
    // [改动前] 默认写死 dbType = "MySQL"、port = "3306"
    // [改动后] 默认留空，由 ViewModel 从 DriverRegistry 的第一个方言初始化
    // [原因] 新增数据库时不需要再改表单默认值
    val dbType: String = "",
    val host: String = "",
    val port: String = "",
    val username: String = "",
    val password: String = "",
    val defaultDatabase: String = "",
    val isEditing: Boolean = false,
    val isTesting: Boolean = false,
    val isSaving: Boolean = false,
    // [改动前] 校验错误保存为英文文案字符串，界面直接显示
    // [改动后] 改为 @StringRes 资源 ID，由 Compose 用 stringResource 解析
    // [原因] 表单错误同样需要本地化，避免 ViewModel 与 strings.xml 出现两套文案
    @StringRes val nameError: Int? = null,
    @StringRes val hostError: Int? = null,
    @StringRes val portError: Int? = null,
    @StringRes val usernameError: Int? = null,
    @StringRes val passwordError: Int? = null
)

/**
 * One-shot events from [AddEditConnectionViewModel].
 */
sealed class AddEditEvent {
    data class ShowSnackbar(val message: UiText) : AddEditEvent()
    data object NavigateBack : AddEditEvent()
}

/**
 * ViewModel for the Add/Edit Connection screen.
 * When editing, the config ID is passed via SavedStateHandle argument "configId".
 */
@HiltViewModel
class AddEditConnectionViewModel @Inject constructor(
    private val repository: ConnectionRepository,
    private val remoteDatabaseManager: RemoteDatabaseManager,
    private val securePreferences: PasswordStore,
    private val driverRegistry: DriverRegistry,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editConfigId: Long? = savedStateHandle.get<Long>("configId")?.takeIf { it > 0 }

    /** 可供数据库类型选择器使用的方言列表。 */
    val dialects: List<DatabaseDialect> = driverRegistry.all()

    private val _uiState = MutableStateFlow(
        AddEditUiState(
            dbType = dialects.firstOrNull()?.id.orEmpty(),
            port = dialects.firstOrNull()?.defaultPort?.toString().orEmpty()
        )
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditEvent>()
    val events = _events.asSharedFlow()

    init {
        if (editConfigId != null) {
            loadExistingConfig(editConfigId)
        }
    }

    /** Update a single field in the form state. */
    fun updateName(value: String) { _uiState.value = _uiState.value.copy(name = value, nameError = null) }
    fun updateDbType(value: String) {
        // [改动前] 用 if (value == "MySQL") 决定 3306/5432
        // [改动后] 从注册表查询方言默认端口，未知方言保持当前端口
        // [原因] 端口规则属于方言，不应写死在表单 ViewModel 中
        val defaultPort = driverRegistry.find(value)?.defaultPort?.toString()
            ?: _uiState.value.port
        _uiState.value = _uiState.value.copy(dbType = value, port = defaultPort)
    }
    fun updateHost(value: String) { _uiState.value = _uiState.value.copy(host = value, hostError = null) }
    fun updatePort(value: String) { _uiState.value = _uiState.value.copy(port = value, portError = null) }
    fun updateUsername(value: String) { _uiState.value = _uiState.value.copy(username = value, usernameError = null) }
    fun updatePassword(value: String) { _uiState.value = _uiState.value.copy(password = value, passwordError = null) }
    fun updateDefaultDatabase(value: String) { _uiState.value = _uiState.value.copy(defaultDatabase = value) }

    /**
     * Validates form fields. Returns true if valid.
     */
    private fun validate(): Boolean {
        val state = _uiState.value
        var valid = true
        val errors = mutableListOf<Pair<String, (AddEditUiState) -> AddEditUiState>>()

        if (state.name.isBlank()) {
            errors.add("name" to { it.copy(nameError = R.string.error_name_required) })
            valid = false
        }
        if (state.host.isBlank()) {
            errors.add("host" to { it.copy(hostError = R.string.error_host_required) })
            valid = false
        }
        val portNum = state.port.toIntOrNull()
        if (portNum == null || portNum < 1 || portNum > 65535) {
            errors.add("port" to { it.copy(portError = R.string.error_port_invalid) })
            valid = false
        }
        if (state.username.isBlank()) {
            errors.add("username" to { it.copy(usernameError = R.string.error_username_required) })
            valid = false
        }
        if (!state.isEditing && state.password.isBlank()) {
            errors.add("password" to { it.copy(passwordError = R.string.error_password_required) })
            valid = false
        }

        if (errors.isNotEmpty()) {
            var newState = _uiState.value
            errors.forEach { newState = it.second(newState) }
            _uiState.value = newState
        }
        return valid
    }

    /**
     * Tests the connection with the current form values.
     */
    fun testConnection() {
        if (!validate()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTesting = true)
            val config = buildConfig()
            val error = remoteDatabaseManager.testConnection(config, _uiState.value.password)
            if (error == null) {
                _events.emit(AddEditEvent.ShowSnackbar(UiText.Res(R.string.connection_test_success)))
            } else {
                _events.emit(
                    AddEditEvent.ShowSnackbar(
                        UiText.Res(R.string.connection_test_failed, listOf(error))
                    )
                )
            }
            _uiState.value = _uiState.value.copy(isTesting = false)
        }
    }

    /**
     * Saves the connection configuration. Validates first, then inserts or updates.
     */
    fun save() {
        if (!validate()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val config = buildConfig()
                if (editConfigId != null) {
                    repository.updateConfig(config.copy(id = editConfigId))
                } else {
                    val newId = repository.insertConfig(config)
                    // Save password securely
                    securePreferences.savePassword(newId, _uiState.value.password)
                }
                // Update password if editing
                if (editConfigId != null && _uiState.value.password.isNotBlank()) {
                    securePreferences.savePassword(editConfigId, _uiState.value.password)
                }
                _events.emit(AddEditEvent.NavigateBack)
            } catch (e: Exception) {
                val detail = e.message
                _events.emit(
                    AddEditEvent.ShowSnackbar(
                        if (detail != null) {
                            UiText.Res(R.string.error_save_failed, listOf(detail))
                        } else {
                            UiText.Res(R.string.error_save_failed_no_detail)
                        }
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isSaving = false)
            }
        }
    }

    /**
     * Loads an existing config for editing.
     */
    private fun loadExistingConfig(id: Long) {
        viewModelScope.launch {
            val config = repository.getConfigById(id) ?: return@launch
            val savedPassword = securePreferences.getPassword(id) ?: ""
            _uiState.value = _uiState.value.copy(
                name = config.name,
                dbType = config.dbType,
                host = config.host,
                port = config.port.toString(),
                username = config.username,
                password = savedPassword,
                defaultDatabase = config.defaultDatabase,
                isEditing = true
            )
        }
    }

    /**
     * Builds a [ConnectionConfigEntity] from current form state (without ID).
     */
    private fun buildConfig(): ConnectionConfigEntity {
        val state = _uiState.value
        return ConnectionConfigEntity(
            name = state.name.trim(),
            dbType = state.dbType,
            host = state.host.trim(),
            port = state.port.toIntOrNull()
                ?: driverRegistry.find(state.dbType)?.defaultPort
                ?: 0,
            username = state.username.trim(),
            defaultDatabase = state.defaultDatabase.trim()
        )
    }
}
