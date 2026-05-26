package com.beszel.android.navigation

import androidx.compose.animation.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.beszel.android.data.model.Session
import com.beszel.android.data.model.System
import com.beszel.android.di.AppContainer
import com.beszel.android.ui.components.AddSystemSheet
import com.beszel.android.ui.screens.alerts.AlertsScreen
import com.beszel.android.ui.screens.alerts.AlertsViewModel
import com.beszel.android.ui.screens.detail.SystemDetailScreen
import com.beszel.android.ui.screens.detail.SystemDetailViewModel
import com.beszel.android.ui.screens.login.LoginScreen
import com.beszel.android.ui.screens.login.LoginViewModel
import com.beszel.android.ui.screens.settings.SettingsScreen
import com.beszel.android.ui.screens.settings.SettingsViewModel
import com.beszel.android.ui.screens.systems.SystemsScreen
import com.beszel.android.ui.screens.systems.SystemsViewModel

sealed class Screen(val route: String) {
    object Login      : Screen("login")
    object Systems    : Screen("systems")
    object Alerts     : Screen("alerts")
    object Settings   : Screen("settings")
    object Detail     : Screen("detail/{systemId}") {
        fun route(id: String) = "detail/$id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph(
    container: AppContainer,
    initialSession: Session?,
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // Track current session in a state so sign-in/out updates the whole graph
    var session by remember { mutableStateOf(initialSession) }

    val startDestination = if (session != null) Screen.Systems.route else Screen.Login.route

    // Systems list is shared state so the FAB and detail can reference the same vm
    var pendingDetailSystem by remember { mutableStateOf<System?>(null) }

    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    val bottomNavItems = listOf(
        Triple(Screen.Systems.route, Icons.Default.Storage, "Systems"),
        Triple(Screen.Alerts.route, Icons.Default.Notifications, "Alerts"),
        Triple(Screen.Settings.route, Icons.Default.Settings, "Settings"),
    )
    val showBottomBar = session != null && currentRoute in bottomNavItems.map { it.first }

    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { (route, icon, label) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(Screen.Systems.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it / 3 } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it } + fadeOut() },
        ) {
            composable(Screen.Login.route) {
                val vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory(container.sessionRepository))
                LoginScreen(viewModel = vm) { newSession ->
                    session = newSession
                    container.initClient(newSession)
                    navController.navigate(Screen.Systems.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }

            composable(Screen.Systems.route) {
                val vm: SystemsViewModel = viewModel(factory = SystemsViewModel.Factory(container.systemsRepository))
                val settingsVm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(container.settingsRepository))
                val settings by settingsVm.settings.collectAsStateWithLifecycle()
                SystemsScreen(
                    viewModel = vm,
                    onOpenSystem = { systemId ->
                        val sys = vm.uiState.value.systems.find { it.id == systemId }
                        if (sys != null) {
                            pendingDetailSystem = sys
                            navController.navigate(Screen.Detail.route(systemId))
                        }
                    },
                    onAddSystem = { showAddSheet = true },
                    compact = settings.density == "compact",
                )

                if (showAddSheet) {
                    val currentSession = session
                    if (currentSession != null) {
                        AddSystemSheet(
                            serverUrl = currentSession.serverUrl,
                            hubPublicKey = "",
                            onDismiss = { showAddSheet = false },
                            onAdd = { name, host, port ->
                                showAddSheet = false
                                // In a production flow: POST to PocketBase to create system record
                                // For now show a snackbar with instructions
                            },
                            snackbarHostState = snackbarHostState,
                        )
                    }
                }
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("systemId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val systemId = backStackEntry.arguments?.getString("systemId") ?: return@composable
                val system = pendingDetailSystem ?: return@composable
                val vm: SystemDetailViewModel = viewModel(
                    key = systemId,
                    factory = SystemDetailViewModel.Factory(systemId, container.systemsRepository),
                )
                SystemDetailScreen(
                    system = system,
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Screen.Alerts.route) {
                val currentSession = session ?: return@composable
                val vm: AlertsViewModel = viewModel(
                    factory = AlertsViewModel.Factory(currentSession.userId, container.alertsRepository),
                )
                AlertsScreen(viewModel = vm)
            }

            composable(Screen.Settings.route) {
                val currentSession = session ?: return@composable
                val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(container.settingsRepository))
                SettingsScreen(
                    viewModel = vm,
                    session = currentSession,
                    onSignOut = {
                        session = null
                        container.clearClient()
                        scope.launch { container.sessionRepository.clearSession() }
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
