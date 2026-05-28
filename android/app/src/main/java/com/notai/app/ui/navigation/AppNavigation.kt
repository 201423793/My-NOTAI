package com.notai.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notai.app.ui.history.HistoryScreen
import com.notai.app.ui.home.HomeScreen
import com.notai.app.ui.processing.ProcessingScreen
import com.notai.app.ui.profile.ProfileScreen
import com.notai.app.ui.result.ResultScreen

private data class TabItem(val screen: Screen, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Screen.Home, "首页", Icons.Default.Home),
    TabItem(Screen.History, "历史", Icons.Default.History),
    TabItem(Screen.Profile, "我的", Icons.Default.Person)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route?.let { route ->
        tabs.any { it.screen.route == route }
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(onNavigate = { route -> navController.navigate(route) })
            }
            composable(Screen.History.route) {
                HistoryScreen(onNavigateToResult = { id -> navController.navigate(Screen.Result.createRoute(id)) })
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onNavigate = { route -> navController.navigate(route) })
            }
            composable(
                Screen.Processing.route,
                arguments = listOf(
                    navArgument("fileUri") { type = NavType.StringType },
                    navArgument("platform") { type = NavType.StringType }
                )
            ) {
                ProcessingScreen(
                    onSuccess = { historyId ->
                        navController.navigate(Screen.Result.createRoute(historyId)) {
                            popUpTo(Screen.Home.route) { inclusive = false }
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
            composable(
                Screen.Result.route,
                arguments = listOf(navArgument("historyId") { type = NavType.LongType })
            ) {
                ResultScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onProcessAnother = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
