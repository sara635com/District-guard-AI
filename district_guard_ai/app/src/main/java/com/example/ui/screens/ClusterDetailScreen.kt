package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.RiskBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.DistrictGuardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClusterDetailScreen(
    clusterId: String,
    viewModel: DistrictGuardViewModel,
    onNavigateBack: () -> Unit
) {
    val clusters by viewModel.allClusters.collectAsState()
    val allReports by viewModel.allReports.collectAsState()

    val cluster = clusters.find { it.id == clusterId } ?: clusters.firstOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Cluster Details #${cluster?.id ?: clusterId}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("btn_back_cluster_detail")) {
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
        if (cluster == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cluster record not found.")
            }
        } else {
            val contributingReports = allReports.filter { r -> cluster.reportIds.contains(r.id) }
            val increasePct = if (cluster.baselineCount > 0) {
                ((cluster.caseCount - cluster.baselineCount).toDouble() / cluster.baselineCount) * 100
            } else 0.0

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(HealthLightBg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    DisclaimerBanner()
                }

                // Cluster Summary Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
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
                                Column {
                                    Text(
                                        text = "Cluster #${cluster.id}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = HealthNavyPrimary
                                    )
                                    Text(
                                        text = "Union Council: ${cluster.unionCouncil}",
                                        fontSize = 13.sp,
                                        color = HealthTextSecondary
                                    )
                                }

                                RiskBadge(riskLevel = cluster.riskLevel, score = cluster.riskScore)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Affected Villages", fontSize = 11.sp, color = HealthTextSecondary)
                                    Text(
                                        text = cluster.areas.joinToString("\n"),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HealthNavyPrimary
                                    )
                                }

                                Column {
                                    Text("Baseline Expected", fontSize = 11.sp, color = HealthTextSecondary)
                                    Text(
                                        text = "${cluster.baselineCount} cases",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = HealthTextSecondary
                                    )
                                }

                                Column {
                                    Text("Current Total", fontSize = 11.sp, color = HealthTextSecondary)
                                    Text(
                                        text = "${cluster.caseCount} cases",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RiskCriticalRed
                                    )
                                    Text(
                                        text = "+${String.format("%.0f", increasePct)}% increase",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RiskCriticalRed
                                    )
                                }
                            }
                        }
                    }
                }

                // Why Was This Flagged? AI Explanation Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Flagged Reason",
                                    tint = HealthBlueAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Why was this flagged?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HealthNavySecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = cluster.aiReason,
                                fontSize = 13.sp,
                                color = Color(0xFF1E3A8A),
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Recommended Action:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = HealthNavyPrimary
                            )
                            Text(
                                text = cluster.recommendedAction,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }
                }

                // Epidemiological Evidence List
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BentoBorderColor),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Epidemiological Evidence Points",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HealthNavyPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            cluster.evidence.forEach { ev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = HealthBlueAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = ev, fontSize = 12.sp, color = Color(0xFF334155))
                                }
                            }
                        }
                    }
                }

                // Contributing Anonymized Reports Table
                item {
                    Text(
                        text = "Contributing Anonymized Reports (${contributingReports.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HealthNavyPrimary
                    )
                }

                items(contributingReports) { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contributing_report_${report.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BentoBorderColor),
                        elevation = CardDefaults.cardElevation(0.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${report.id} — ${report.village}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = HealthNavyPrimary
                                )
                                Text(
                                    text = "Symptoms: ${report.symptoms.joinToString(", ")}",
                                    fontSize = 11.sp,
                                    color = HealthTextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = report.severity,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (report.severity == "Severe") RiskCriticalRed else HealthNavySecondary
                                )
                                if (report.hospitalized) {
                                    Text("Hospitalized", fontSize = 10.sp, color = RiskCriticalRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
