package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.DistrictGuardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DistrictGuardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = DistrictGuardRepository(
        reportDao = db.reportDao(),
        clusterDao = db.clusterDao(),
        alertDao = db.alertDao(),
        healthWorkerDao = db.healthWorkerDao(),
        clinicDao = db.clinicDao(),
        notificationDao = db.notificationDao(),
        systemConfigDao = db.systemConfigDao()
    )

    private val _currentRole = MutableStateFlow(UserRole.DISTRICT_HEALTH_OFFICER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    val allReports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClusters: StateFlow<List<ClusterEntity>> = repository.allClusters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<AlertEntity>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newAlerts: StateFlow<List<AlertEntity>> = repository.newAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkers: StateFlow<List<HealthWorkerEntity>> = repository.allWorkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClinics: StateFlow<List<ClinicEntity>> = repository.allClinics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemConfig: StateFlow<SystemConfigEntity?> = repository.systemConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SystemConfigEntity())

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _submissionSuccessMessage = MutableStateFlow<String?>(null)
    val submissionSuccessMessage: StateFlow<String?> = _submissionSuccessMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDemoData()
        }
    }

    fun selectRole(role: UserRole) {
        _currentRole.value = role
    }

    fun submitReport(
        village: String,
        unionCouncil: String,
        symptoms: List<String>,
        symptomDescription: String,
        severity: String,
        fever: Boolean,
        temperature: Double?,
        vomiting: Boolean,
        diarrhea: Boolean,
        cough: Boolean,
        breathingDifficulty: Boolean,
        rash: Boolean,
        headache: Boolean,
        bodyAches: Boolean,
        otherSymptoms: String,
        hospitalized: Boolean,
        isOffline: Boolean = false
    ) {
        viewModelScope.launch {
            val reportId = "RPT-2026-%04d".format((1000..9999).random())
            val report = ReportEntity(
                id = reportId,
                timestamp = System.currentTimeMillis(),
                village = village.ifBlank { "Village Cedar" },
                unionCouncil = unionCouncil.ifBlank { "UC-01 Alpha" },
                symptoms = symptoms.ifEmpty { listOf("Fever") },
                symptomDescription = symptomDescription,
                severity = severity,
                fever = fever,
                temperature = temperature,
                vomiting = vomiting,
                diarrhea = diarrhea,
                cough = cough,
                breathingDifficulty = breathingDifficulty,
                rash = rash,
                headache = headache,
                bodyAches = bodyAches,
                otherSymptoms = otherSymptoms,
                hospitalized = hospitalized,
                healthWorkerId = "HW-001",
                syncStatus = if (isOffline) SyncStatus.SAVED_LOCAL else SyncStatus.SYNCED
            )

            repository.submitReport(report)
            _submissionSuccessMessage.value = "Report submitted successfully. The system will include this report in district-level outbreak analysis."
        }
    }

    fun clearSubmissionMessage() {
        _submissionSuccessMessage.value = null
    }

    fun syncPendingReports() {
        viewModelScope.launch {
            repository.syncPendingReports()
        }
    }

    fun updateAlertStatus(alertId: String, newStatus: AlertStatus) {
        viewModelScope.launch {
            repository.updateAlertStatus(alertId, newStatus)
        }
    }

    fun generateDemoOutbreak() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            repository.generateOutbreakDemoScenario()
            _isAnalyzing.value = false
        }
    }

    fun resetNormalScenario() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            repository.generateNormalDemoScenario()
            _isAnalyzing.value = false
        }
    }

    fun saveConfig(config: SystemConfigEntity) {
        viewModelScope.launch {
            repository.saveSystemConfig(config)
        }
    }

    fun addHealthWorker(name: String, role: String, area: String) {
        viewModelScope.launch {
            val id = "HW-%03d".format((100..999).random())
            repository.addHealthWorker(HealthWorkerEntity(id, name, role, area))
        }
    }

    fun deleteHealthWorker(id: String) {
        viewModelScope.launch {
            repository.deleteHealthWorker(id)
        }
    }

    fun addClinic(name: String, uc: String, phone: String) {
        viewModelScope.launch {
            val id = "CLN-%03d".format((100..999).random())
            repository.addClinic(ClinicEntity(id, name, uc, 31.5200, 74.3500, phone))
        }
    }

    fun deleteClinic(id: String) {
        viewModelScope.launch {
            repository.deleteClinic(id)
        }
    }
}
