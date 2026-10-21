package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.JobApplicationEntity
import com.example.data.local.JobDao
import com.example.data.model.DashboardStats
import com.example.data.model.ExamCategory
import com.example.data.model.JobApplication
import com.example.data.model.StageInfo
import com.example.data.model.StageStatus
import com.example.data.remote.GoogleAppsScriptClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JobRepository(
    private val context: Context,
    private val dao: JobDao,
    val remoteClient: GoogleAppsScriptClient
) {
    val allApplications: Flow<List<JobApplication>> = dao.getAllApplications().map { list ->
        list.map { it.toDomain() }
    }

    val stats: Flow<DashboardStats> = allApplications.map { list ->
        calculateStats(list)
    }

    private fun calculateStats(list: List<JobApplication>): DashboardStats {
        var mP = 0; var mF = 0; var mPen = 0
        var wP = 0; var wF = 0; var wPen = 0
        var tP = 0; var tF = 0; var tPen = 0
        var vP = 0; var vF = 0; var vPen = 0
        var totF = 0; var totC = 0; var totA = list.size; var totE = 0

        for (app in list) {
            if (app.isCanceled) {
                totC++
            } else {
                totE++
                if (app.category == ExamCategory.FAILED) {
                    totF++
                }

                when (app.mcq.status) {
                    StageStatus.PASSED -> mP++
                    StageStatus.FAILED -> mF++
                    StageStatus.NO -> mPen++
                }
                when (app.written.status) {
                    StageStatus.PASSED -> wP++
                    StageStatus.FAILED -> wF++
                    StageStatus.NO -> wPen++
                }
                when (app.typing.status) {
                    StageStatus.PASSED -> tP++
                    StageStatus.FAILED -> tF++
                    StageStatus.NO -> tPen++
                }
                when (app.viva.status) {
                    StageStatus.PASSED -> vP++
                    StageStatus.FAILED -> vF++
                    StageStatus.NO -> vPen++
                }
            }
        }

        return DashboardStats(
            totalApplications = totA,
            totalExams = totE,
            totalFailed = totF,
            totalCanceled = totC,
            mcqPassed = mP,
            mcqFailed = mF,
            mcqPending = mPen,
            writtenPassed = wP,
            writtenFailed = wF,
            writtenPending = wPen,
            typingPassed = tP,
            typingFailed = tF,
            typingPending = tPen,
            vivaPassed = vP,
            vivaFailed = vF,
            vivaPending = vPen
        )
    }

    suspend fun getApplication(id: Long): JobApplication? {
        return dao.getById(id)?.toDomain()
    }

    suspend fun saveApplication(
        app: JobApplication,
        appPdfUri: Uri? = null,
        admitPdfUri: Uri? = null
    ): Result<Unit> {
        val todayStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val updatedApp = app.copy(
            lastUpdated = todayStr,
            localAppPdfUri = appPdfUri?.toString() ?: app.localAppPdfUri,
            localAdmitPdfUri = admitPdfUri?.toString() ?: app.localAdmitPdfUri
        )

        // Save locally first
        val entity = JobApplicationEntity.fromDomain(updatedApp)
        dao.insertOrUpdate(entity)

        // If remote Google Apps Script is configured, push to Google Sheet / Drive
        if (remoteClient.isConfigured()) {
            val appBytes = appPdfUri?.let { readBytesFromUri(it) }
            val admitBytes = admitPdfUri?.let { readBytesFromUri(it) }
            val appFileName = appPdfUri?.let { getFileName(it) } ?: "app.pdf"
            val admitFileName = admitPdfUri?.let { getFileName(it) } ?: "admit.pdf"

            val remoteResult = remoteClient.saveOrUpdateApplication(
                app = updatedApp,
                appPdfBytes = appBytes,
                appPdfName = appFileName,
                admitPdfBytes = admitBytes,
                admitPdfName = admitFileName
            )

            if (remoteResult.isSuccess) {
                // If remote returned updated info or we can sync back:
                dao.insertOrUpdate(entity.copy(syncStatus = "Synced"))
            }
        }

        return Result.success(Unit)
    }

    suspend fun deleteApplication(app: JobApplication): Result<Unit> {
        dao.deleteByOrgAndPost(app.orgName, app.postName)
        if (remoteClient.isConfigured()) {
            remoteClient.deleteRow(app.orgName, app.postName)
        }
        return Result.success(Unit)
    }

    suspend fun clearDriveFiles(app: JobApplication): Result<Unit> {
        dao.clearDriveFiles(app.orgName, app.postName)
        if (remoteClient.isConfigured()) {
            remoteClient.deleteDriveFiles(app.orgName, app.postName, app.appPdfUrl, app.admitPdfUrl)
        }
        return Result.success(Unit)
    }

    suspend fun syncFromGoogleSheet(): Result<Int> {
        if (!remoteClient.isConfigured()) {
            return Result.failure(IllegalStateException("Web App URL is not set. Please enter your Google Apps Script URL."))
        }

        val remoteData = remoteClient.fetchSheetData()
        return if (remoteData.isSuccess) {
            val items = remoteData.getOrNull() ?: emptyList()
            val entities = items.map { JobApplicationEntity.fromDomain(it) }
            dao.insertAll(entities)
            Result.success(items.size)
        } else {
            Result.failure(remoteData.exceptionOrNull() ?: Exception("Unknown sync error"))
        }
    }

    private fun readBytesFromUri(uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "document.pdf"
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex)
                    }
                }
            }
        } catch (e: Exception) {
            // fallback
        }
        return name
    }
}
