package com.example.incometracker

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.incometracker.backup.DriveBackupWorker
import com.example.incometracker.data.AppDatabase
import java.util.concurrent.TimeUnit

class TrackerApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        scheduleAutomaticBackup()
    }

    private fun scheduleAutomaticBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicSync = PeriodicWorkRequestBuilder<DriveBackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DriveAutoBackupWork",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSync
        )
    }
}
