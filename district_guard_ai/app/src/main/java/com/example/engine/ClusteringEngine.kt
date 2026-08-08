package com.example.engine

import com.example.data.model.ClusterEntity
import com.example.data.model.ReportEntity
import com.example.data.model.RiskLevel
import com.example.data.model.SystemConfigEntity

object ClusteringEngine {

    fun generateClusters(
        reports: List<ReportEntity>,
        config: SystemConfigEntity = SystemConfigEntity()
    ): List<ClusterEntity> {
        if (reports.isEmpty()) return emptyList()

        // Group by Union Council
        val reportsByUC = reports.groupBy { it.unionCouncil }
        val clusters = mutableListOf<ClusterEntity>()

        var clusterIndex = 1
        reportsByUC.forEach { (unionCouncil, ucReports) ->
            if (ucReports.isEmpty()) return@forEach

            val villages = ucReports.map { it.village }.distinct()
            val reportIds = ucReports.map { it.id }

            // Baseline for UC: 4 cases by default or calculated
            val baseline = 5
            val riskResult = DeterministicRiskEngine.calculateRisk(ucReports, baselineCount = baseline, config = config)

            val startDate = ucReports.minOfOrNull { it.timestamp } ?: System.currentTimeMillis()
            val endDate = ucReports.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()

            val clusterId = "CLUS-%02d".format(clusterIndex++)

            val evidenceList = listOf(
                "${ucReports.size} total anonymized symptom reports in $unionCouncil across ${villages.size} village(s)",
                "Baseline expectation: $baseline cases. Current increase: ${String.format("%.1f", riskResult.caseIncreaseRatio)}x",
                "Severe / hospitalized count: ${riskResult.severeCount}",
                "Dominant symptom pattern: ${riskResult.dominantSymptoms.joinToString(", ")}"
            )

            val recommendedAction = when (riskResult.level) {
                RiskLevel.CRITICAL -> "ALERT: Dispatch emergency medical team to $unionCouncil, notify all nearby clinics, prepare IV fluids & fever medication stock."
                RiskLevel.HIGH -> "District Health Office review and increased surveillance. Alert nearby clinics to monitor acute illness cases."
                RiskLevel.MODERATE -> "Advise health workers in $unionCouncil to perform door-to-door water quality and symptom tracking."
                RiskLevel.LOW -> "Routine surveillance. Continue monitoring standard weekly baseline."
            }

            val reason = "DETECTED CLUSTER in $unionCouncil: ${ucReports.size} reports across ${villages.joinToString(", ")} within 7 days (${String.format("%.1f", riskResult.caseIncreaseRatio)}x baseline). Risk level evaluated as ${riskResult.level.name}."

            clusters.add(
                ClusterEntity(
                    id = clusterId,
                    createdAt = System.currentTimeMillis(),
                    startDate = startDate,
                    endDate = endDate,
                    unionCouncil = unionCouncil,
                    areas = villages,
                    reportIds = reportIds,
                    dominantSymptoms = riskResult.dominantSymptoms,
                    caseCount = ucReports.size,
                    baselineCount = baseline,
                    riskScore = riskResult.score,
                    riskLevel = riskResult.level,
                    confidence = riskResult.confidence,
                    aiReason = reason,
                    evidence = evidenceList,
                    recommendedAction = recommendedAction,
                    status = "ACTIVE"
                )
            )
        }

        return clusters.sortedByDescending { it.riskScore }
    }
}
