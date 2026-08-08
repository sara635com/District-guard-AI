package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.*

@Database(
    entities = [
        ReportEntity::class,
        ClusterEntity::class,
        AlertEntity::class,
        HealthWorkerEntity::class,
        ClinicEntity::class,
        NotificationEntity::class,
        SystemConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun clusterDao(): ClusterDao
    abstract fun alertDao(): AlertDao
    abstract fun healthWorkerDao(): HealthWorkerDao
    abstract fun clinicDao(): ClinicDao
    abstract fun notificationDao(): NotificationDao
    abstract fun systemConfigDao(): SystemConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "district_guard_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
