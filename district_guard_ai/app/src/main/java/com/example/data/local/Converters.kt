package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AlertStatus
import com.example.data.model.RiskLevel
import com.example.data.model.SyncStatus
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString("||") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("||")
    }

    @TypeConverter
    fun fromRiskLevel(level: RiskLevel): String = level.name

    @TypeConverter
    fun toRiskLevel(value: String): RiskLevel = try {
        RiskLevel.valueOf(value)
    } catch (e: Exception) {
        RiskLevel.LOW
    }

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = try {
        SyncStatus.valueOf(value)
    } catch (e: Exception) {
        SyncStatus.SYNCED
    }

    @TypeConverter
    fun fromAlertStatus(status: AlertStatus): String = status.name

    @TypeConverter
    fun toAlertStatus(value: String): AlertStatus = try {
        AlertStatus.valueOf(value)
    } catch (e: Exception) {
        AlertStatus.NEW
    }

    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.HEALTH_WORKER
    }
}
