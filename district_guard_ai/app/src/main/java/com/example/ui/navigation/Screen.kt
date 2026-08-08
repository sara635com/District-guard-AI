package com.example.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object ReportForm : Screen("report_form", "Submit Report")
    object ClusterDetail : Screen("cluster_detail/{clusterId}", "Cluster Details") {
        fun createRoute(clusterId: String) = "cluster_detail/$clusterId"
    }
    object Alerts : Screen("alerts", "Alert Tickets")
    object TimelineTrend : Screen("timeline_trend", "Timeline & Trends")
    object AdminConfig : Screen("admin_config", "Admin Settings")
    object Notifications : Screen("notifications", "Notifications")
}
