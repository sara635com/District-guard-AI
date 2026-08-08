package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertEntity
import com.example.data.model.AlertStatus
import com.example.data.model.UserRole
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.RiskBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.DistrictGuardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: DistrictGuardViewModel,
    onNavigateBack: () -> Unit
) {
    val alerts by viewModel.allAlerts.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()
    val role by viewModel.currentRole.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Alert Tickets, 1: Clinic Notifications Log

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alert Tickets & Clinic Dispatch", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_alerts")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HealthNavyPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HealthLightBg)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Alert Tickets (${alerts.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Clinic Dispatch (${notifications.size})") }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    DisclaimerBanner()
                }

                if (selectedTab == 0) {
                    if (alerts.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BentoBorderColor),
                                elevation = CardDefaults.cardElevation(0.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No active outbreak alert tickets.", color = HealthTextSecondary)
                                }
                            }
                        }
                    } else {
                        items(alerts) { alert ->
                            AlertTicketCard(
                                alert = alert,
                                isOfficer = role == UserRole.DISTRICT_HEALTH_OFFICER || role == UserRole.ADMINISTRATOR,
                                onStatusChanged = { newStatus ->
                                    viewModel.updateAlertStatus(alert.id, newStatus)
                                }
                            )
                        }
                    }
                } else {
                    if (notifications.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BentoBorderColor),
                                elevation = CardDefaults.cardElevation(0.dp),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No clinic notifications logged.", color = HealthTextSecondary)
                                }
                            }
                        }
                    } else {
                        items(notifications) { notif ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("notif_item_${notif.id}"),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, BentoBorderColor),
                                elevation = CardDefaults.cardElevation(0.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = HealthBlueAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = notif.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = HealthNavyPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = notif.message,
                                        fontSize = 12.sp,
                                        color = Color(0xFF334155)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Target Area: ${notif.unionCouncil} • ${notif.targetRole.displayName}",
                                        fontSize = 10.sp,
                                        color = HealthTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertTicketCard(
    alert: AlertEntity,
    isOfficer: Boolean,
    onStatusChanged: (AlertStatus) -> Unit
) {
    val statusColor = when (alert.status) {
        AlertStatus.NEW -> RiskCriticalRed
        AlertStatus.ACKNOWLEDGED -> RiskHighOrange
        AlertStatus.INVESTIGATING -> RiskModerateYellow
        AlertStatus.RESOLVED -> RiskLowGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_card_${alert.id.replace("#", "").replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoBorderColor),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Alert Ticket",
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.id,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HealthNavyPrimary
                    )
                }

                RiskBadge(riskLevel = alert.riskLevel)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target Area: ${alert.unionCouncil}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = HealthNavySecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = alert.message,
                fontSize = 12.sp,
                color = Color(0xFF334155)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Recommended Action: ${alert.recommendedAction}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0284C7)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", fontSize = 11.sp, color = HealthTextSecondary)
                    Text(
                        text = alert.status.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                if (isOfficer) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AlertStatus.values().forEach { statusOption ->
                            val isCurrent = alert.status == statusOption
                            FilterChip(
                                selected = isCurrent,
                                onClick = { onStatusChanged(statusOption) },
                                label = { Text(statusOption.name, fontSize = 9.sp) },
                                modifier = Modifier.testTag("status_chip_${alert.id}_${statusOption.name}")
                            )
                        }
                    }
                }
            }
        }
    }
}
