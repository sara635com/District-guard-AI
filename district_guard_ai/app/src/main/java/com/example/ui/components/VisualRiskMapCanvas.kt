package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClusterEntity
import com.example.data.model.RiskLevel
import com.example.ui.theme.*

data class UcMapNode(
    val id: String,
    val name: String,
    val xPercent: Float, // 0.0 to 1.0
    val yPercent: Float,
    val villages: List<String>
)

val DISTRICT_MAP_NODES = listOf(
    UcMapNode("UC-01 Alpha", "UC-01 Alpha", 0.25f, 0.35f, listOf("Village Cedar", "Village Pine")),
    UcMapNode("UC-02 Beta", "UC-02 Beta", 0.70f, 0.40f, listOf("Village Green", "Village Riverside", "Village Orchard")),
    UcMapNode("UC-03 Gamma", "UC-03 Gamma", 0.35f, 0.75f, listOf("Village Oak", "Village Hill")),
    UcMapNode("UC-04 Delta", "UC-04 Delta", 0.75f, 0.80f, listOf("Village Sunrise", "Village Valley"))
)

@Composable
fun VisualRiskMapCanvas(
    clusters: List<ClusterEntity>,
    onClusterSelected: (ClusterEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedClusterState by remember { mutableStateOf<ClusterEntity?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("visual_risk_map_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "District Outbreak Risk Map",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Real-time Union Council surveillance heat map",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MapLegendChip("Low", RiskLowGreen)
                    MapLegendChip("Mod", RiskModerateYellow)
                    MapLegendChip("High", RiskHighOrange)
                    MapLegendChip("Crit", RiskCriticalRed)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(clusters) {
                            detectTapGestures { tapOffset ->
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()

                                DISTRICT_MAP_NODES.forEach { node ->
                                    val nodeX = node.xPercent * width
                                    val nodeY = node.yPercent * height
                                    val distance = Math.hypot(
                                        (tapOffset.x - nodeX).toDouble(),
                                        (tapOffset.y - nodeY).toDouble()
                                    )

                                    if (distance < 60) {
                                        val match = clusters.find { it.unionCouncil == node.id }
                                        if (match != null) {
                                            selectedClusterState = match
                                            onClusterSelected(match)
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw District Grid Lines
                    val strokeGrid = Stroke(width = 1f)
                    for (i in 1..4) {
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(w * (i * 0.2f), 0f),
                            end = Offset(w * (i * 0.2f), h),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(0f, h * (i * 0.2f)),
                            end = Offset(w, h * (i * 0.2f)),
                            strokeWidth = 1f
                        )
                    }

                    // Draw Boundary Connection Lines between UCs
                    val p1 = Offset(DISTRICT_MAP_NODES[0].xPercent * w, DISTRICT_MAP_NODES[0].yPercent * h)
                    val p2 = Offset(DISTRICT_MAP_NODES[1].xPercent * w, DISTRICT_MAP_NODES[1].yPercent * h)
                    val p3 = Offset(DISTRICT_MAP_NODES[2].xPercent * w, DISTRICT_MAP_NODES[2].yPercent * h)
                    val p4 = Offset(DISTRICT_MAP_NODES[3].xPercent * w, DISTRICT_MAP_NODES[3].yPercent * h)

                    drawLine(Color(0xFF94A3B8), p1, p2, strokeWidth = 2f)
                    drawLine(Color(0xFF94A3B8), p1, p3, strokeWidth = 2f)
                    drawLine(Color(0xFF94A3B8), p2, p4, strokeWidth = 2f)
                    drawLine(Color(0xFF94A3B8), p3, p4, strokeWidth = 2f)

                    // Draw Map Nodes with risk bubble colors
                    DISTRICT_MAP_NODES.forEach { node ->
                        val center = Offset(node.xPercent * w, node.yPercent * h)
                        val cluster = clusters.find { it.unionCouncil == node.id }

                        val (nodeColor, radius) = when (cluster?.riskLevel) {
                            RiskLevel.CRITICAL -> RiskCriticalRed to 36f
                            RiskLevel.HIGH -> RiskHighOrange to 32f
                            RiskLevel.MODERATE -> RiskModerateYellow to 26f
                            RiskLevel.LOW -> RiskLowGreen to 22f
                            null -> RiskLowGreen to 20f
                        }

                        // Outer Heat Halo
                        drawCircle(
                            color = nodeColor.copy(alpha = 0.25f),
                            radius = radius * 1.8f,
                            center = center
                        )

                        // Outer Pulse Ring for High/Critical
                        if (cluster?.riskLevel == RiskLevel.HIGH || cluster?.riskLevel == RiskLevel.CRITICAL) {
                            drawCircle(
                                color = nodeColor,
                                radius = radius * 2.2f,
                                center = center,
                                style = Stroke(width = 3f)
                            )
                        }

                        // Node Core
                        drawCircle(
                            color = nodeColor,
                            radius = radius,
                            center = center
                        )

                        drawCircle(
                            color = Color.White,
                            radius = radius * 0.4f,
                            center = center
                        )
                    }
                }

                // Overlay Text Labels
                DISTRICT_MAP_NODES.forEach { node ->
                    val cluster = clusters.find { it.unionCouncil == node.id }
                    val labelColor = when (cluster?.riskLevel) {
                        RiskLevel.CRITICAL -> RiskCriticalRed
                        RiskLevel.HIGH -> RiskHighOrange
                        RiskLevel.MODERATE -> Color(0xFF854D0E)
                        else -> RiskLowGreen
                    }

                    Box(
                        modifier = Modifier
                            .offset(
                                x = (node.xPercent * 300).dp,
                                y = (node.yPercent * 160).dp
                            )
                    ) {
                        Column {
                            Text(
                                text = node.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (cluster != null) "${cluster.caseCount} cases (${cluster.riskLevel.name})" else "0 cases (LOW)",
                                fontSize = 9.sp,
                                color = labelColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "💡 Tap any Union Council cluster marker to inspect epidemiological AI breakdown & evidence.",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun MapLegendChip(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = label, fontSize = 9.sp, color = Color(0xFF475569))
    }
}
