package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.DistrictGuardViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: DistrictGuardViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToReportForm = { navController.navigate(Screen.ReportForm.route) },
                onNavigateToClusterDetail = { clusterId -> navController.navigate(Screen.ClusterDetail.createRoute(clusterId)) },
                onNavigateToAlerts = { navController.navigate(Screen.Alerts.route) },
                onNavigateToTimeline = { navController.navigate(Screen.TimelineTrend.route) },
                onNavigateToAdmin = { navController.navigate(Screen.AdminConfig.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) }
            )
        }

        composable(Screen.ReportForm.route) {
            ReportFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ClusterDetail.route,
            arguments = listOf(navArgument("clusterId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clusterId = backStackEntry.arguments?.getString("clusterId") ?: ""
            ClusterDetailScreen(
                clusterId = clusterId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Alerts.route) {
            AlertsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TimelineTrend.route) {
            TimelineTrendScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.AdminConfig.route) {
            AdminConfigScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
