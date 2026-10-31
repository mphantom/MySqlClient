package com.example.remotedbexplorer.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remotedbexplorer.data.db.ConnectionConfigEntity
import com.example.remotedbexplorer.data.remote.SessionManager
import com.example.remotedbexplorer.data.repository.ConnectionRepository
import com.example.remotedbexplorer.util.SecurePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the main/workspace screen managing active connection tabs.
 */
data class MainUiState(
    /** Ordered list of config IDs for currently open connections. */
    val activeConfigIds: List<Long> = emptyList(),
    /** The currently selected/focused config ID. */
    val selectedConfigId: Long? = null,
    /** Which panel is shown: 0 = Browse, 1 = Query. */
    val selectedPanel: Int = 0,
    /** Map of configId to cached config entity for display. */
    val configCache: Map<Long, ConnectionConfigEntity> = emptyMap()
)

/**
 * ViewModel for the main screen.
 * Tracks which connections are currently active (open as tabs) and
 * which tab + panel is selected.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: ConnectionRepository,
    private val securePreferences: SecurePreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    /**
     * Opens a connection by [configId] — creates a session, navigates to it,
     * and adds it to the active tabs.
     */
    fun openConnection(configId: Long, password: String) {
        // Store password in session memory
        sessionManager.storePassword(configId, password)

        // Create session if not already present
        sessionManager.getOrCreate(configId)

        val currentIds = _uiState.value.activeConfigIds.toMutableList()
        if (configId !in currentIds) {
            currentIds.add(configId)
        }

        viewModelScope.launch {
            val config = repository.getConfigById(configId)
            val cache = _uiState.value.configCache.toMutableMap()
            if (config != null) {
                cache[configId] = config
            }

            _uiState.value = _uiState.value.copy(
                activeConfigIds = currentIds,
                selectedConfigId = configId,
                configCache = cache,
                selectedPanel = 0 // default to Browse
            )
        }
    }

    /**
     * Selects a different active connection tab.
     */
    fun selectConnection(configId: Long) {
        _uiState.value = _uiState.value.copy(selectedConfigId = configId)
    }

    /**
     * Closes a connection: disconnects, removes session, updates tabs.
     */
    fun closeConnection(configId: Long) {
        sessionManager.remove(configId)
        val currentIds = _uiState.value.activeConfigIds.toMutableList()
        currentIds.remove(configId)

        val newSelected = if (_uiState.value.selectedConfigId == configId) {
            currentIds.lastOrNull()
        } else {
            _uiState.value.selectedConfigId
        }

        val cache = _uiState.value.configCache.toMutableMap()
        cache.remove(configId)

        _uiState.value = _uiState.value.copy(
            activeConfigIds = currentIds,
            selectedConfigId = newSelected,
            configCache = cache
        )
    }

    /**
     * Switches the bottom panel: 0 = Browse, 1 = Query.
     */
    fun selectPanel(panel: Int) {
        _uiState.value = _uiState.value.copy(selectedPanel = panel)
    }

    /**
     * Returns the connection config name for a given configId.
     */
    fun getConfigName(configId: Long): String {
        return _uiState.value.configCache[configId]?.name ?: "Connection"
    }
}
