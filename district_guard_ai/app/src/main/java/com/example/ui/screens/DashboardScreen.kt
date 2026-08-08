package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.DistrictGuardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DistrictGuardViewModel,
    onNavigateToReportForm: () -> Unit,
    onNavigateToClusterDetail: (String) -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToNotifications: () -> Unit
) {
    val role by viewModel.currentRole.collectAsState()
    val reports by viewModel.allReports.collectAsState()
    val clusters by viewModel.allClusters.collectAsState()
    val alerts by viewModel.allAlerts.collectAsState()
    val newAlerts by viewModel.newAlerts.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    val pendingSyncs = reports.count { it.syncStatus != SyncStatus.SYNCED }
    val totalReports = reports.size
    val activeClustersCount = clusters.count { it.status == "ACTIVE" }
    val highRiskAreasCount = clusters.count { it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.CRITICAL }
    val criticalAlertsCount = alerts.count { it.riskLevel == RiskLevel.CRITICAL && it.status != AlertStatus.RESOLVED }

    Scaffold(
        topBar = {
            RoleSelectorBar(
                currentRole = role,
                onRoleSelected = { viewModel.selectRole(it) },
                onGenerateDemoOutbreak = { viewModel.generateDemoOutbreak() },
                onSyncOffline = { viewModel.syncPendingReports() },
                pendingSyncCount = pendingSyncs
            )
        },
        floatingActionButton = {
            if (role == UserRole.HEALTH_WORKER) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToReportForm,
                    icon = { Icon(Icons.Default.Add, contentDescription = "Submit Report") },
                    text = { Text("Submit Report", fontWeight = FontWeight.Bold) },
                    containerColor = HealthNavySecondary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_submit_report")
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(HealthLightBg)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
            ) {
                // Disclaimer Banner
                item {
                    DisclaimerBanner()
                }

                // AI Processing Loading Bar
                if (isAnalyzing) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFFD97706)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Analyzing district report clusters with Gemini Epidemiological Engine...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                }

                // ROLE 1: HEALTH WORKER VIEW
                if (role == UserRole.HEALTH_WORKER) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, BentoBorderColor),
                            elevation = CardDefaults.cardElevation(0.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(HealthBlueAccent.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MedicalServices,
                                            contentDescription = "Field Portal",
                                            tint = HealthBlueAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Health Worker Field Reporting Portal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Submit anonymized field symptom observations. Reports work offline and sync automatically.",
                                    fontSize = 12.sp,
                                    color = HealthTextSecondary
                                )

                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = onNavigateToReportForm,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_health_worker_submit"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = HealthNavySecondary)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("New Patient Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = onNavigateToNotifications,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_health_worker_alerts"),
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, BentoBorderColor)
                                    ) {
                                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Alerts (${notifications.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    // Nearby Outbreak Risk Areas
                    item {
                        Text(
                            text = "Nearby Outbreak Risk Areas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = HealthNavyPrimary
                        )
                    }

                    items(clusters) { cluster ->
                        ClusterCardItem(cluster = cluster, onClick = { onNavigateToClusterDetail(cluster.id) })
                    }
                }

                // ROLE 2 & 3: DISTRICT HEALTH OFFICER & ADMIN DASHBOARD VIEW
                if (role == UserRole.DISTRICT_HEALTH_OFFICER || role == UserRole.ADMINISTRATOR) {
                    // Top 4 KPI Stat Cards
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(
                                    title = "Total Reports",
                                    value = "$totalReports",
                                    icon = Icons.Default.Description,
                                    iconBgColor = HealthBlueAccent,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Active Clusters",
                                    value = "$activeClustersCount",
                                    icon = Icons.Default.BubbleChart,
                                    iconBgColor = RiskModerateYellow,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatCard(
                                    title = "High Risk Areas",
                                    value = "$highRiskAreasCount",
                                    icon = Icons.Default.Warning,
                                    iconBgColor = RiskHighOrange,
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    title = "Critical Alerts",
                                    value = "$criticalAlertsCount",
                                    icon = Icons.Default.Campaign,
                                    iconBgColor = RiskCriticalRed,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Visual Risk Map
                    item {
                        VisualRiskMapCanvas(
                            clusters = clusters,
                            onClusterSelected = { onNavigateToClusterDetail(it.id) }
                        )
                    }

                    // Quick Navigation Action Chips
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToTimeline,
                                colors = ButtonDefaults.buttonColors(containerColor = HealthNavySecondary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("nav_timeline_btn")
                            ) {
                                Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Timeline", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onNavigateToAlerts,
                                colors = ButtonDefaults.buttonColors(containerColor = HealthNavyPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).testTag("nav_alerts_btn")
                            ) {
                                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Alerts (${alerts.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            if (role == UserRole.ADMINISTRATOR) {
                                Button(
                                    onClick = onNavigateToAdmin,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f).testTag("nav_admin_btn")
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Detected Active Outbreak Clusters Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Detected Clusters & AI Evidence",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HealthNavyPrimary
                            )

                            TextButton(onClick = { viewModel.resetNormalScenario() }) {
                                Text("Reset Baseline", fontSize = 11.sp, color = HealthBlueAccent)
                            }
                        }
                    }

                    if (clusters.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BentoBorderColor),
                                elevation = CardDefaults.cardElevation(0.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = RiskLowGreen, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No Outbreak Clusters Detected", fontWeight = FontWeight.Bold)
                                    Text("Current report baseline remains within normal limits across all Union Councils.", fontSize = 11.sp, color = HealthTextSecondary)
                                }
                            }
                        }
                    } else {
                        items(clusters) { cluster ->
                            ClusterCardItem(
                                cluster = cluster,
                                onClick = { onNavigateToClusterDetail(cluster.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClusterCardItem(
    cluster: ClusterEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cluster_card_${cluster.id}")
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cluster #${cluster.id} — ${cluster.unionCouncil}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Villages: ${cluster.areas.joinToString(", ")}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                RiskBadge(riskLevel = cluster.riskLevel, score = cluster.riskScore)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text("Cases / Baseline", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Text(
                        text = "${cluster.caseCount} / ${cluster.baselineCount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Column {
                    Text("Dominant Symptoms", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    Text(
                        text = cluster.dominantSymptoms.joinToString(", "),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HealthNavySecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // AI Explanation Snippet - Bento Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Explanation",
                            tint = HealthBlueAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Outbreak Explanation",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HealthNavySecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = cluster.aiReason,
                        fontSize = 11.sp,
                        color = Color(0xFF334155),
                        maxLines = 2
                    )
                }
            }
        }
    }
}
