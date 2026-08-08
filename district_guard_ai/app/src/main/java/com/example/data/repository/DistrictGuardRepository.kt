package com.example.data.repository

import com.example.ai.GeminiOutbreakAnalyzer
import com.example.data.local.*
import com.example.data.model.*
import com.example.engine.ClusteringEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class DistrictGuardRepository(
    private val reportDao: ReportDao,
    private val clusterDao: ClusterDao,
    private val alertDao: AlertDao,
    private val healthWorkerDao: HealthWorkerDao,
    private val clinicDao: ClinicDao,
    private val notificationDao: NotificationDao,
    private val systemConfigDao: SystemConfigDao
) {
    val allReports: Flow<List<ReportEntity>> = reportDao.getAllReports()
    val allClusters: Flow<List<ClusterEntity>> = clusterDao.getAllClusters()
    val allAlerts: Flow<List<AlertEntity>> = alertDao.getAllAlerts()
    val newAlerts: Flow<List<AlertEntity>> = alertDao.getNewAlerts()
    val allWorkers: Flow<List<HealthWorkerEntity>> = healthWorkerDao.getAllWorkers()
    val allClinics: Flow<List<ClinicEntity>> = clinicDao.getAllClinics()
    val allNotifications: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val systemConfig: Flow<SystemConfigEntity?> = systemConfigDao.getConfig()

    suspend fun submitReport(report: ReportEntity) {
        reportDao.insertReport(report)
        evaluateDistrictRisk()
    }

    suspend fun syncPendingReports() {
        val pending = reportDao.getPendingReports()
        if (pending.isNotEmpty()) {
            reportDao.updateSyncStatus(pending.map { it.id })
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Offline Reports Synced",
                    message = "Successfully synchronized ${pending.size} local reports with district database.",
                    unionCouncil = "District Central",
                    targetRole = UserRole.HEALTH_WORKER
                )
            )
            evaluateDistrictRisk()
        }
    }

    suspend fun updateAlertStatus(alertId: String, status: AlertStatus) {
        val ackTime = if (status == AlertStatus.ACKNOWLEDGED || status == AlertStatus.INVESTIGATING) System.currentTimeMillis() else null
        val resTime = if (status == AlertStatus.RESOLVED) System.currentTimeMillis() else null
        alertDao.updateAlertStatus(alertId, status, ackTime, resTime)
    }

    suspend fun saveSystemConfig(config: SystemConfigEntity) {
        systemConfigDao.saveConfig(config)
        evaluateDistrictRisk()
    }

    suspend fun addHealthWorker(worker: HealthWorkerEntity) {
        healthWorkerDao.insertWorker(worker)
    }

    suspend fun deleteHealthWorker(id: String) {
        healthWorkerDao.deleteWorker(id)
    }

    suspend fun addClinic(clinic: ClinicEntity) {
        clinicDao.insertClinic(clinic)
    }

    suspend fun deleteClinic(id: String) {
        clinicDao.deleteClinic(id)
    }

    suspend fun evaluateDistrictRisk() {
        val reports = reportDao.getAllReports().first()
        if (reports.isEmpty()) return

        val config = systemConfigDao.getConfigDirect() ?: SystemConfigEntity()

        // Cluster detection by Union Council
        val reportsByUC = reports.groupBy { it.unionCouncil }
        val generatedClusters = mutableListOf<ClusterEntity>()

        reportsByUC.forEach { (unionCouncil, ucReports) ->
            if (ucReports.size >= 3) {
                val cluster = GeminiOutbreakAnalyzer.analyzeCluster(
                    unionCouncil = unionCouncil,
                    reports = ucReports,
                    baselineCount = 5,
                    config = config
                )
                generatedClusters.add(cluster)

                // Check auto-alert threshold
                if (cluster.riskScore >= config.alertThresholdScore) {
                    val alertId = "ALERT #DG-2026-${(1000..9999).random()}"
                    val alert = AlertEntity(
                        id = alertId,
                        clusterId = cluster.id,
                        createdAt = System.currentTimeMillis(),
                        unionCouncil = unionCouncil,
                        riskLevel = cluster.riskLevel,
                        recipientType = "DISTRICT_HEALTH_OFFICE & NEARBY_CLINICS",
                        message = "Outbreak Risk Alert in $unionCouncil (${cluster.caseCount} reports, ${cluster.riskScore}/100 score). ${cluster.aiReason}",
                        recommendedAction = cluster.recommendedAction,
                        status = AlertStatus.NEW
                    )
                    alertDao.insertAlert(alert)

                    // Simulate Clinic & Worker Notifications
                    notificationDao.insertNotification(
                        NotificationEntity(
                            title = "🚨 OUTBREAK RISK ALERT (${cluster.riskLevel.name})",
                            message = "High-risk symptom cluster detected in $unionCouncil (${cluster.caseCount} cases). Prepare supplies and increase surveillance.",
                            unionCouncil = unionCouncil,
                            targetRole = UserRole.DISTRICT_HEALTH_OFFICER
                        )
                    )
                    notificationDao.insertNotification(
                        NotificationEntity(
                            title = "⚠️ Clinic Alert: $unionCouncil",
                            message = "Cluster flagged near your facility: ${cluster.dominantSymptoms.joinToString(", ")}. Recommended action: ${cluster.recommendedAction}",
                            unionCouncil = unionCouncil,
                            targetRole = UserRole.HEALTH_WORKER
                        )
                    )
                }
            }
        }

        if (generatedClusters.isNotEmpty()) {
            clusterDao.insertClusters(generatedClusters)
        }
    }

    suspend fun seedInitialDemoData() {
        // Check if database is already populated
        val existingReports = reportDao.getAllReports().first()
        if (existingReports.isNotEmpty()) return

        // Initial Health Workers
        val workers = listOf(
            HealthWorkerEntity("HW-101", "Amina Bibi", "Senior Field Worker", "UC-01 Alpha"),
            HealthWorkerEntity("HW-102", "Tariq Mahmood", "Community Health Nurse", "UC-02 Beta"),
            HealthWorkerEntity("HW-103", "Saima Khan", "Surveillance Inspector", "UC-03 Gamma")
        )
        workers.forEach { healthWorkerDao.insertWorker(it) }

        // Initial Clinics
        val clinics = listOf(
            ClinicEntity("CLN-01", "Alpha Rural Health Center", "UC-01 Alpha", 31.5204, 74.3587, "+92-42-9920011"),
            ClinicEntity("CLN-02", "Beta Basic Health Unit", "UC-02 Beta", 31.5401, 74.3720, "+92-42-9920022"),
            ClinicEntity("CLN-03", "Gamma Community Dispensary", "UC-03 Gamma", 31.5110, 74.3310, "+92-42-9920033")
        )
        clinics.forEach { clinicDao.insertClinic(it) }

        systemConfigDao.saveConfig(SystemConfigEntity())

        // Load standard baseline demo reports
        generateNormalDemoScenario()
    }

    suspend fun generateNormalDemoScenario() {
        reportDao.clearAll()
        clusterDao.clearAll()
        alertDao.clearAll()

        val now = System.currentTimeMillis()
        val day = 24 * 3600 * 1000L

        val normalReports = listOf(
            ReportEntity(
                id = "RPT-1001",
                timestamp = now - 5 * day,
                village = "Village Cedar",
                unionCouncil = "UC-01 Alpha",
                symptoms = listOf("Fever", "Headache"),
                severity = "Mild",
                fever = true,
                syncStatus = SyncStatus.SYNCED
            ),
            ReportEntity(
                id = "RPT-1002",
                timestamp = now - 3 * day,
                village = "Village Pine",
                unionCouncil = "UC-02 Beta",
                symptoms = listOf("Cough", "Body aches"),
                severity = "Mild",
                syncStatus = SyncStatus.SYNCED
            ),
            ReportEntity(
                id = "RPT-1003",
                timestamp = now - 1 * day,
                village = "Village Oak",
                unionCouncil = "UC-03 Gamma",
                symptoms = listOf("Headache"),
                severity = "Mild",
                syncStatus = SyncStatus.SYNCED
            )
        )
        reportDao.insertReports(normalReports)
        evaluateDistrictRisk()
    }

    suspend fun generateOutbreakDemoScenario() {
        reportDao.clearAll()
        clusterDao.clearAll()
        alertDao.clearAll()

        val now = System.currentTimeMillis()
        val hour = 3600 * 1000L
        val day = 24 * hour

        val reports = mutableListOf<ReportEntity>()

        // 18 severe / acute watery diarrhea & vomiting reports in UC-02 Beta across 3 villages
        val villagesInBeta = listOf("Village Green", "Village Riverside", "Village Orchard")
        for (i in 1..18) {
            val village = villagesInBeta[i % villagesInBeta.size]
            val timeOffset = (1..96).random() * hour
            val isSevere = i <= 6
            reports.add(
                ReportEntity(
                    id = "RPT-OUTBREAK-%03d".format(i),
                    timestamp = now - timeOffset,
                    village = village,
                    unionCouncil = "UC-02 Beta",
                    latitudeApprox = 31.5400 + ((-20..20).random() * 0.001),
                    longitudeApprox = 74.3720 + ((-20..20).random() * 0.001),
                    symptoms = listOf("Fever", "Diarrhea", "Vomiting", if (isSevere) "Body aches" else "Headache"),
                    symptomDescription = "Sudden onset of severe watery diarrhea and high fever reported by local community worker.",
                    severity = if (isSevere) "Severe" else "Moderate",
                    fever = true,
                    temperature = 38.8 + (i % 5) * 0.2,
                    diarrhea = true,
                    vomiting = true,
                    hospitalized = isSevere,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }

        // Add 4 scattered reports in UC-01 Alpha
        for (i in 1..4) {
            reports.add(
                ReportEntity(
                    id = "RPT-ALPHA-%03d".format(i),
                    timestamp = now - (i * 12 * hour),
                    village = "Village Cedar",
                    unionCouncil = "UC-01 Alpha",
                    symptoms = listOf("Cough", "Fever"),
                    severity = "Mild",
                    fever = true,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }

        reportDao.insertReports(reports)
        evaluateDistrictRisk()
    }
}
