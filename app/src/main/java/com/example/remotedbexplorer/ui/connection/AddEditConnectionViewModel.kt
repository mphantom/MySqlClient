package com.example.remotedbexplorer.ui.connection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remotedbexplorer.data.db.ConnectionConfigEntity
import com.example.remotedbexplorer.data.remote.RemoteDatabaseManager
import com.example.remotedbexplorer.data.repository.ConnectionRepository
import com.example.remotedbexplorer.util.SecurePreferences
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
    val dbType: String = "MySQL",
    val host: String = "",
    val port: String = "3306",
    val username: String = "",
    val password: String = "",
    val defaultDatabase: String = "",
    val isEditing: Boolean = false,
    val isTesting: Boolean = false,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val hostError: String? = null,
    val portError: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null
)

/**
 * One-shot events from [AddEditConnectionViewModel].
 */
sealed class AddEditEvent {
    data class ShowSnackbar(val message: String) : AddEditEvent()
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
    private val securePreferences: SecurePreferences,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val editConfigId: Long? = savedStateHandle.get<Long>("configId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(AddEditUiState())
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
        val defaultPort = if (value == "MySQL") "3306" else "5432"
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
            errors.add("name" to { it.copy(nameError = "Connection name is required") })
            valid = false
        }
        if (state.host.isBlank()) {
            errors.add("host" to { it.copy(hostError = "Host address is required") })
            valid = false
        }
        val portNum = state.port.toIntOrNull()
        if (portNum == null || portNum < 1 || portNum > 65535) {
            errors.add("port" to { it.copy(portError = "Valid port (1-65535) is required") })
            valid = false
        }
        if (state.username.isBlank()) {
            errors.add("username" to { it.copy(usernameError = "Username is required") })
            valid = false
        }
        if (!state.isEditing && state.password.isBlank()) {
            errors.add("password" to { it.copy(passwordError = "Password is required") })
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
                _events.emit(AddEditEvent.ShowSnackbar("Connection test successful!"))
            } else {
                _events.emit(AddEditEvent.ShowSnackbar("Connection test failed: $error"))
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
                _events.emit(
                    AddEditEvent.ShowSnackbar(
                        "Failed to save: ${e.message ?: "Unknown error"}"
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
            port = state.port.toIntOrNull() ?: (if (state.dbType == "MySQL") 3306 else 5432),
            username = state.username.trim(),
            defaultDatabase = state.defaultDatabase.trim()
        )
    }
}
