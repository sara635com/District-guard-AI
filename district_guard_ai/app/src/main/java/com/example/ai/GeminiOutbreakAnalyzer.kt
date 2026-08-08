package com.example.ai

import com.example.data.model.ClusterEntity
import com.example.data.model.ReportEntity
import com.example.data.model.RiskLevel
import com.example.data.model.SystemConfigEntity
import com.example.engine.DeterministicRiskEngine
import org.json.JSONArray
import org.json.JSONObject

object GeminiOutbreakAnalyzer {

    suspend fun analyzeCluster(
        unionCouncil: String,
        reports: List<ReportEntity>,
        baselineCount: Int = 5,
        config: SystemConfigEntity = SystemConfigEntity()
    ): ClusterEntity {
        val deterministicResult = DeterministicRiskEngine.calculateRisk(reports, baselineCount, config)

        // Build aggregated summary JSON
        val villages = reports.map { it.village }.distinct()
        val symptomCounts = mutableMapOf<String, Int>()
        reports.forEach { r ->
            r.symptoms.forEach { s -> symptomCounts[s] = (symptomCounts[s] ?: 0) + 1 }
        }

        val aggregatedJson = JSONObject().apply {
            put("union_council", unionCouncil)
            put("affected_villages_count", villages.size)
            put("villages_list", JSONArray(villages))
            put("total_reports", reports.size)
            put("baseline_expected_cases", baselineCount)
            put("increase_ratio", String.format("%.2f", deterministicResult.caseIncreaseRatio))
            put("severe_cases_count", deterministicResult.severeCount)
            put("symptom_breakdown", JSONObject().apply {
                symptomCounts.forEach { (sym, count) -> put(sym, count) }
            })
            put("time_window_days", 7)
        }.toString()

        val aiResult = GeminiApiService.analyzeOutbreakData(aggregatedJson)

        val finalRiskLevel: RiskLevel
        val finalRiskScore: Int
        val finalConfidence: Int
        val finalReason: String
        val finalEvidence: List<String>
        val finalAction: String

        if (aiResult != null) {
            finalRiskLevel = try {
                RiskLevel.valueOf(aiResult.riskLevel.uppercase())
            } catch (e: Exception) {
                deterministicResult.level
            }
            finalRiskScore = aiResult.riskScore
            finalConfidence = aiResult.confidence
            finalReason = aiResult.reason
            finalEvidence = if (aiResult.evidence.isNotEmpty()) aiResult.evidence else listOf("Cluster confirmed via Gemini AI risk model")
            finalAction = aiResult.recommendedAction
        } else {
            // Fallback to Deterministic Engine
            finalRiskLevel = deterministicResult.level
            finalRiskScore = deterministicResult.score
            finalConfidence = deterministicResult.confidence
            finalReason = "AI analysis temporarily unavailable. Showing analysis from the local outbreak-risk engine. " + deterministicResult.breakdown
            finalEvidence = listOf(
                "Local rule-based engine output: $finalRiskScore/100 risk score",
                "${reports.size} cases reported vs $baselineCount baseline in $unionCouncil",
                "${deterministicResult.severeCount} severe/hospitalized case(s)",
                "Top reported symptoms: ${deterministicResult.dominantSymptoms.joinToString(", ")}"
            )
            finalAction = when (finalRiskLevel) {
                RiskLevel.CRITICAL -> "CRITICAL: Urgent District Health Officer intervention required. Notify all nearby health facilities."
                RiskLevel.HIGH -> "HIGH: Conduct door-to-door water/symptom surveillance and notify Union Council clinics."
                RiskLevel.MODERATE -> "MODERATE: Monitor daily report trends and issue health hygiene guidance."
                RiskLevel.LOW -> "LOW: Maintain standard baseline monitoring."
            }
        }

        val startDate = reports.minOfOrNull { it.timestamp } ?: System.currentTimeMillis()
        val endDate = reports.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()

        return ClusterEntity(
            id = "CLUS-${unionCouncil.take(3).uppercase()}-${(System.currentTimeMillis() % 1000)}",
            createdAt = System.currentTimeMillis(),
            startDate = startDate,
            endDate = endDate,
            unionCouncil = unionCouncil,
            areas = villages,
            reportIds = reports.map { it.id },
            dominantSymptoms = deterministicResult.dominantSymptoms,
            caseCount = reports.size,
            baselineCount = baselineCount,
            riskScore = finalRiskScore,
            riskLevel = finalRiskLevel,
            confidence = finalConfidence,
            aiReason = finalReason,
            evidence = finalEvidence,
            recommendedAction = finalAction,
            status = "ACTIVE"
        )
    }
}
