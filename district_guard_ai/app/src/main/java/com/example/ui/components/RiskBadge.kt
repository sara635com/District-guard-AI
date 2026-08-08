package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskLevel
import com.example.ui.theme.*

@Composable
fun RiskBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier,
    score: Int? = null
) {
    val (bgColor, textColor) = when (riskLevel) {
        RiskLevel.LOW -> RiskLowBg to RiskLowGreen
        RiskLevel.MODERATE -> RiskModerateBg to RiskModerateYellow
        RiskLevel.HIGH -> RiskHighBg to RiskHighOrange
        RiskLevel.CRITICAL -> RiskCriticalBg to RiskCriticalRed
    }

    val labelText = if (score != null) "${riskLevel.name} ($score/100)" else riskLevel.name

    Box(
        modifier = modifier
            .testTag("risk_badge_${riskLevel.name.lowercase()}")
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = labelText,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
