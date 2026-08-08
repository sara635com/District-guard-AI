package com.example.engine

import com.example.data.model.ReportEntity
import com.example.data.model.RiskLevel
import com.example.data.model.SystemConfigEntity
import kotlin.math.max

data class DeterministicResult(
    val score: Int,
    val level: RiskLevel,
    val confidence: Int,
    val breakdown: String,
    val dominantSymptoms: List<String>,
    val caseIncreaseRatio: Double,
    val totalCases: Int,
    val baseline: Int,
    val severeCount: Int
)

object DeterministicRiskEngine {

    fun calculateRisk(
        reports: List<ReportEntity>,
        baselineCount: Int = 5,
        config: SystemConfigEntity = SystemConfigEntity()
    ): DeterministicResult {
        if (reports.isEmpty()) {
            return DeterministicResult(
                score = 0,
                level = RiskLevel.LOW,
                confidence = 100,
                breakdown = "No reports submitted in evaluation period.",
                dominantSymptoms = emptyList(),
                caseIncreaseRatio = 0.0,
                totalCases = 0,
                baseline = baselineCount,
                severeCount = 0
            )
        }

        val totalCases = reports.size
        val effectiveBaseline = max(1, baselineCount)
        val caseIncreaseRatio = totalCases.toDouble() / effectiveBaseline

        // 1. Case Increase Points (Max 30)
        val increasePoints = when {
            caseIncreaseRatio > 3.0 -> 30
            caseIncreaseRatio >= 2.0 -> 20
            caseIncreaseRatio >= 1.5 -> 10
            else -> 0
        }

        // 2. Geographic Clustering Points (Max 20)
        val distinctVillages = reports.map { it.village }.distinct().size
        val geoPoints = when {
            distinctVillages >= 3 -> 20
            distinctVillages == 2 -> 10
            else -> 5
        }

        // 3. Symptom Similarity Analysis (Max 20)
        val symptomFrequencies = mutableMapOf<String, Int>()
        reports.forEach { r ->
            r.symptoms.forEach { symptom ->
                symptomFrequencies[symptom] = (symptomFrequencies[symptom] ?: 0) + 1
            }
        }
        val topSymptoms = symptomFrequencies.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        val highestSymptomRatio = if (topSymptoms.isNotEmpty()) {
            (symptomFrequencies[topSymptoms.first()] ?: 0).toDouble() / totalCases
        } else 0.0

        val symptomPoints = when {
            highestSymptomRatio >= 0.75 -> 20
            highestSymptomRatio >= 0.50 -> 10
            else -> 5
        }

        // 4. Severity Points (Max 20)
        val severeCount = reports.count { it.severity.equals("Severe", ignoreCase = true) || it.hospitalized }
        val severityPoints = when {
            severeCount >= 3 -> 20
            severeCount >= 1 -> 10
            else -> 0
        }

        // 5. Rapid Increase Spike in 48 hours (Max 10)
        val twoDaysAgo = System.currentTimeMillis() - (48 * 3600 * 1000L)
        val recentSpikeCount = reports.count { it.timestamp >= twoDaysAgo }
        val rapidPoints = if (recentSpikeCount >= 5) 10 else 0

        val totalScore = (increasePoints + geoPoints + symptomPoints + severityPoints + rapidPoints)
            .coerceIn(0, 100)

        val level = when {
            totalScore >= config.criticalMaxScore - 20 -> RiskLevel.CRITICAL
            totalScore >= config.alertThresholdScore -> RiskLevel.HIGH
            totalScore >= config.lowMaxScore + 1 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        val confidence = when {
            totalCases >= 15 -> 95
            totalCases >= 8 -> 85
            totalCases >= 3 -> 70
            else -> 50
        }

        val breakdown = "Local Deterministic Evaluation: Baseline $effectiveBaseline vs Current $totalCases cases (${String.format("%.1f", caseIncreaseRatio)}x increase). Villages affected: $distinctVillages. Severe cases: $severeCount. Top symptoms: ${topSymptoms.joinToString(", ")}."

        return DeterministicResult(
            score = totalScore,
            level = level,
            confidence = confidence,
            breakdown = breakdown,
            dominantSymptoms = topSymptoms,
            caseIncreaseRatio = caseIncreaseRatio,
            totalCases = totalCases,
            baseline = effectiveBaseline,
            severeCount = severeCount
        )
    }
}
