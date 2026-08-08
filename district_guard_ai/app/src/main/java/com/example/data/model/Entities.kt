package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val displayName: String, val description: String) {
    HEALTH_WORKER("Health Worker", "Field report submission & local outbreak monitoring"),
    DISTRICT_HEALTH_OFFICER("District Health Officer", "District surveillance, cluster analysis & alert triage"),
    ADMINISTRATOR("Administrator", "System configuration, worker/clinic management & thresholds")
}

enum class RiskLevel(val label: String, val minScore: Int, val maxScore: Int) {
    LOW("LOW", 0, 29),
    MODERATE("MODERATE", 30, 59),
    HIGH("HIGH", 60, 79),
    CRITICAL("CRITICAL", 80, 100)
}

enum class SyncStatus {
    SAVED_LOCAL,
    PENDING_SYNC,
    SYNCED
}

enum class AlertStatus {
    NEW,
    ACKNOWLEDGED,
    INVESTIGATING,
    RESOLVED
}

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String, // e.g. "RPT-2026-8812"
    val timestamp: Long = System.currentTimeMillis(),
    val village: String,
    val unionCouncil: String,
    val latitudeApprox: Double = 0.0,
    val longitudeApprox: Double = 0.0,
    val symptoms: List<String>,
    val symptomDescription: String = "",
    val severity: String, // Mild, Moderate, Severe
    val fever: Boolean = false,
    val temperature: Double? = null,
    val vomiting: Boolean = false,
    val diarrhea: Boolean = false,
    val cough: Boolean = false,
    val breathingDifficulty: Boolean = false,
    val rash: Boolean = false,
    val headache: Boolean = false,
    val bodyAches: Boolean = false,
    val otherSymptoms: String = "",
    val hospitalized: Boolean = false,
    val healthWorkerId: String = "HW-001",
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

@Entity(tableName = "clusters")
data class ClusterEntity(
    @PrimaryKey val id: String, // e.g. "CLUS-04"
    val createdAt: Long = System.currentTimeMillis(),
    val startDate: Long = System.currentTimeMillis() - 4 * 24 * 3600 * 1000L,
    val endDate: Long = System.currentTimeMillis(),
    val unionCouncil: String,
    val areas: List<String>, // Affected villages
    val reportIds: List<String>,
    val dominantSymptoms: List<String>,
    val caseCount: Int,
    val baselineCount: Int,
    val riskScore: Int, // 0-100
    val riskLevel: RiskLevel,
    val confidence: Int, // 0-100
    val aiReason: String,
    val evidence: List<String>,
    val recommendedAction: String,
    val status: String = "ACTIVE" // ACTIVE, RESOLVED
)

@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey val id: String, // e.g. "ALERT #DG-2026-0017"
    val clusterId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val unionCouncil: String,
    val riskLevel: RiskLevel,
    val recipientType: String = "DISTRICT_HEALTH_OFFICE",
    val message: String,
    val recommendedAction: String = "",
    val status: AlertStatus = AlertStatus.NEW,
    val acknowledgedAt: Long? = null,
    val resolvedAt: Long? = null
)

@Entity(tableName = "health_workers")
data class HealthWorkerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,
    val assignedArea: String,
    val phone: String = "+92-300-1234567"
)

@Entity(tableName = "clinics")
data class ClinicEntity(
    @PrimaryKey val id: String,
    val name: String,
    val unionCouncil: String,
    val approxLatitude: Double,
    val approxLongitude: Double,
    val contactPhone: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val unionCouncil: String,
    val timestamp: Long = System.currentTimeMillis(),
    val targetRole: UserRole = UserRole.HEALTH_WORKER,
    val isRead: Boolean = false
)

@Entity(tableName = "system_configs")
data class SystemConfigEntity(
    @PrimaryKey val id: String = "default_config",
    val lowMaxScore: Int = 29,
    val moderateMaxScore: Int = 59,
    val highMaxScore: Int = 79,
    val criticalMaxScore: Int = 100,
    val alertThresholdScore: Int = 60, // Auto alert created if score >= this
    val baselineMultiplierThreshold: Double = 2.0
)
