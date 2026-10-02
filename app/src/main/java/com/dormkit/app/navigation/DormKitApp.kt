package com.dormkit.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dormkit.app.ui.screens.DashboardScreen
import com.dormkit.app.ui.screens.InventoryScreen
import com.dormkit.app.ui.screens.LaundryScreen
import com.dormkit.app.ui.screens.PackingScreen
import com.dormkit.app.ui.screens.SettingsScreen
import com.dormkit.app.viewmodel.MainViewModel

private data class BottomDestination(val route: String, val label: String, val icon: ImageVector)

private val bottomDestinations = listOf(
    BottomDestination("home", "首頁", Icons.Default.Home),
    BottomDestination("inventory", "庫存", Icons.Default.Inventory2),
    BottomDestination("packing", "清單", Icons.Default.Checklist),
    BottomDestination("settings", "設定", Icons.Default.Settings)
)

@Composable
fun DormKitApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBottomBar = route in bottomDestinations.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = route == destination.route,
                            onClick = {
                                if (destination.route == "inventory") viewModel.clearInventoryFilters()
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                DashboardScreen(
                    viewModel = viewModel,
                    onInventory = { lowOnly ->
                        viewModel.showLowStockOnly(lowOnly)
                        navController.navigate("inventory")
                    },
                    onPacking = { navController.navigate("packing") },
                    onLaundry = { navController.navigate("laundry") }
                )
            }
            composable("inventory") { InventoryScreen(viewModel) }
            composable("packing") { PackingScreen(viewModel) }
            composable("settings") { SettingsScreen(viewModel) }
            composable("laundry") { LaundryScreen(viewModel, onBack = { navController.popBackStack() }) }
        }
    }
}
