package com.example.remotedbexplorer.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remotedbexplorer.data.db.ConnectionConfigEntity
import com.example.remotedbexplorer.data.remote.RemoteDatabaseManager
import com.example.remotedbexplorer.data.repository.ConnectionRepository
import com.example.remotedbexplorer.util.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the connection list screen.
 */
data class ConnectionListUiState(
    val configs: List<ConnectionConfigEntity> = emptyList(),
    val isTestingConnection: Boolean = false
)

/**
 * One-shot UI events emitted by [ConnectionViewModel].
 */
sealed class ConnectionEvent {
    data class ShowSnackbar(val message: String) : ConnectionEvent()
    data class NavigateToConnection(val configId: Long, val password: String) : ConnectionEvent()
}

/**
 * ViewModel for the connection list screen.
 * Manages saved connections, deletion, and password entry for connecting.
 */
@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val repository: ConnectionRepository,
    private val securePreferences: SecurePreferences,
    private val remoteDatabaseManager: RemoteDatabaseManager
) : ViewModel() {

    /** Observable list of saved configs. */
    val configs: StateFlow<List<ConnectionConfigEntity>> = repository.observeAllConfigs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(ConnectionListUiState())
    val uiState: StateFlow<ConnectionListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ConnectionEvent>()
    val events = _events.asSharedFlow()

    /**
     * Returns the saved password for [configId], or null if not stored.
     */
    fun getSavedPassword(configId: Long): String? {
        return securePreferences.getPassword(configId)
    }

    /**
     * Attempts to connect to the database using [config] and [password].
     * On success emits [ConnectionEvent.NavigateToConnection].
     * On failure emits [ConnectionEvent.ShowSnackbar].
     */
    fun connectToDatabase(config: ConnectionConfigEntity, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true)
            try {
                val error = remoteDatabaseManager.testConnection(config, password)
                if (error == null) {
                    // Save password for future use
                    securePreferences.savePassword(config.id, password)
                    _events.emit(ConnectionEvent.NavigateToConnection(config.id, password))
                } else {
                    _events.emit(ConnectionEvent.ShowSnackbar("Connection failed: $error"))
                }
            } catch (e: Exception) {
                _events.emit(
                    ConnectionEvent.ShowSnackbar(
                        e.message ?: "An unexpected error occurred"
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isTestingConnection = false)
            }
        }
    }

    /**
     * Deletes a connection config and its stored password.
     */
    fun deleteConnection(config: ConnectionConfigEntity) {
        viewModelScope.launch {
            repository.deleteConfig(config)
            securePreferences.deletePassword(config.id)
        }
    }

    /**
     * Tests a connection without navigating — just shows success/failure via snackbar.
     */
    fun testConnectionOnly(config: ConnectionConfigEntity, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingConnection = true)
            try {
                val error = remoteDatabaseManager.testConnection(config, password)
                if (error == null) {
                    securePreferences.savePassword(config.id, password)
                    _events.emit(ConnectionEvent.ShowSnackbar("Connection test successful!"))
                } else {
                    _events.emit(ConnectionEvent.ShowSnackbar("Connection test failed: $error"))
                }
            } catch (e: Exception) {
                _events.emit(
                    ConnectionEvent.ShowSnackbar(
                        "Connection test failed: ${e.message ?: "Unknown error"}"
                    )
                )
            } finally {
                _uiState.value = _uiState.value.copy(isTestingConnection = false)
            }
        }
    }
}
