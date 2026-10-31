package com.phoenix.remotedbclient.ui.main

// [改动前] 底部导航、关闭按钮描述、空状态与标签兜底名称为英文字面量
// [改动后] 改为 stringResource 读取 strings.xml；标签兜底名由 ViewModel 返回 null 后在此解析
// [原因] 工作区文案统一由 strings.xml 提供，避免与连接列表页出现两套文案
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.phoenix.remotedbclient.R
import com.phoenix.remotedbclient.data.remote.SessionManager
import com.phoenix.remotedbclient.ui.browser.BrowserViewModel
import com.phoenix.remotedbclient.ui.browser.DatabaseBrowserScreen
import com.phoenix.remotedbclient.ui.connection.AddEditConnectionScreen
import com.phoenix.remotedbclient.ui.connection.ConnectionListScreen
import com.phoenix.remotedbclient.ui.query.QueryScreen
import com.phoenix.remotedbclient.ui.query.QueryViewModel

/**
 * Root composable hosting the navigation graph and workspace with connection tabs.
 */
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val uiState by mainViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "connections"
    ) {
        composable("connections") {
            ConnectionListScreen(
                onNavigateToAdd = { navController.navigate("add_connection") },
                onNavigateToEdit = { id -> navController.navigate("edit_connection/$id") },
                onNavigateToBrowser = { configId, password ->
                    mainViewModel.openConnection(configId, password)
                    navController.navigate("workspace") {
                        popUpTo("connections") { inclusive = false }
                    }
                }
            )
        }

        composable("add_connection") {
            AddEditConnectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "edit_connection/{configId}",
            arguments = listOf(navArgument("configId") { type = NavType.LongType })
        ) {
            AddEditConnectionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("workspace") {
            WorkspaceScreen(
                mainViewModel = mainViewModel,
                onNavigateToConnections = {
                    navController.popBackStack("connections", inclusive = false)
                }
            )
        }
    }
}

/**
 * Workspace screen showing active connection tabs at the top,
 * bottom navigation (Browse / Query), and the corresponding content.
 */
@Composable
private fun WorkspaceScreen(
    mainViewModel: MainViewModel,
    onNavigateToConnections: () -> Unit
) {
    val uiState by mainViewModel.uiState.collectAsState()
    val selectedId = uiState.selectedConfigId
    val activeIds = uiState.activeConfigIds

    Column(modifier = Modifier.fillMaxSize()) {
        // Connection tabs
        if (activeIds.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = activeIds.indexOf(selectedId).coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 0.dp
            ) {
                activeIds.forEach { configId ->
                    val configName = mainViewModel.getConfigName(configId) ?: stringResource(R.string.connection)
                    Tab(
                        selected = configId == selectedId,
                        onClick = { mainViewModel.selectConnection(configId) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Storage,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    configName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (configId == selectedId) FontWeight.Bold
                                    else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = {
                                        mainViewModel.closeConnection(configId)
                                        if (uiState.activeConfigIds.size <= 1) {
                                            onNavigateToConnections()
                                        }
                                    },
                                    modifier = Modifier.padding(0.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.close_connection),
                                        modifier = Modifier.padding(0.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        // Content based on selected panel
        if (selectedId != null && activeIds.contains(selectedId)) {
            when (uiState.selectedPanel) {
                0 -> {
                    val browserViewModel: BrowserViewModel = hiltViewModel()
                    LaunchedEffect(selectedId) {
                        browserViewModel.load(selectedId)
                    }
                    DatabaseBrowserScreen(
                        onDisconnected = {
                            mainViewModel.closeConnection(selectedId)
                            if (uiState.activeConfigIds.size <= 1) {
                                onNavigateToConnections()
                            }
                        },
                        viewModel = browserViewModel
                    )
                }
                1 -> {
                    val queryViewModel: QueryViewModel = hiltViewModel()
                    LaunchedEffect(selectedId) {
                        queryViewModel.load(selectedId)
                    }
                    QueryScreen(
                        onDisconnected = {
                            mainViewModel.closeConnection(selectedId)
                            if (uiState.activeConfigIds.size <= 1) {
                                onNavigateToConnections()
                            }
                        },
                        viewModel = queryViewModel
                    )
                }
            }

            // Bottom navigation
            NavigationBar {
                NavigationBarItem(
                    selected = uiState.selectedPanel == 0,
                    onClick = { mainViewModel.selectPanel(0) },
                    icon = { Icon(Icons.Default.TableChart, contentDescription = null) },
                    label = { Text(stringResource(R.string.browse)) }
                )
                NavigationBarItem(
                    selected = uiState.selectedPanel == 1,
                    onClick = { mainViewModel.selectPanel(1) },
                    icon = { Icon(Icons.Default.Code, contentDescription = null) },
                    label = { Text(stringResource(R.string.query)) }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_active_connections),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
