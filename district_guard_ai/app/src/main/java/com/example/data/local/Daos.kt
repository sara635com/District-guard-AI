package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getReportsSince(sinceTimestamp: Long): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingReports(): List<ReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<ReportEntity>)

    @Query("UPDATE reports SET syncStatus = 'SYNCED' WHERE id IN (:ids)")
    suspend fun updateSyncStatus(ids: List<String>)

    @Query("DELETE FROM reports")
    suspend fun clearAll()
}

@Dao
interface ClusterDao {
    @Query("SELECT * FROM clusters ORDER BY createdAt DESC")
    fun getAllClusters(): Flow<List<ClusterEntity>>

    @Query("SELECT * FROM clusters WHERE id = :id")
    suspend fun getClusterById(id: String): ClusterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCluster(cluster: ClusterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClusters(clusters: List<ClusterEntity>)

    @Query("DELETE FROM clusters")
    suspend fun clearAll()
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts ORDER BY createdAt DESC")
    fun getAllAlerts(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE status = 'NEW'")
    fun getNewAlerts(): Flow<List<AlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertEntity)

    @Query("UPDATE alerts SET status = :status, acknowledgedAt = :acknowledgedAt, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun updateAlertStatus(id: String, status: AlertStatus, acknowledgedAt: Long?, resolvedAt: Long?)

    @Query("DELETE FROM alerts")
    suspend fun clearAll()
}

@Dao
interface HealthWorkerDao {
    @Query("SELECT * FROM health_workers")
    fun getAllWorkers(): Flow<List<HealthWorkerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: HealthWorkerEntity)

    @Query("DELETE FROM health_workers WHERE id = :id")
    suspend fun deleteWorker(id: String)
}

@Dao
interface ClinicDao {
    @Query("SELECT * FROM clinics")
    fun getAllClinics(): Flow<List<ClinicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClinic(clinic: ClinicEntity)

    @Query("DELETE FROM clinics WHERE id = :id")
    suspend fun deleteClinic(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)
}

@Dao
interface SystemConfigDao {
    @Query("SELECT * FROM system_configs WHERE id = 'default_config'")
    fun getConfig(): Flow<SystemConfigEntity?>

    @Query("SELECT * FROM system_configs WHERE id = 'default_config'")
    suspend fun getConfigDirect(): SystemConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: SystemConfigEntity)
}
