package com.example.incometracker.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import java.io.File
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale

class DriveBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val account = GoogleSignIn.getLastSignedInAccount(applicationContext) ?: return Result.failure()
            val credential = GoogleAccountCredential.usingOAuth2(
                applicationContext,
                Collections.singletonList(DriveScopes.DRIVE_APPDATA)
            ).setSelectedAccount(account.account)

            val driveService = Drive.Builder(
                com.google.api.client.http.javanet.NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("IncomeExpenseTracker").build()

            val dbFile = applicationContext.getDatabasePath("income_expense_tracker_db")
            if (!dbFile.exists()) return Result.failure()

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = "tracker_backup_$timeStamp.db"
                parents = listOf("appDataFolder")
            }

            val mediaContent = FileContent("application/x-sqlite3", dbFile)
            driveService.files().create(fileMetadata, mediaContent).execute()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
