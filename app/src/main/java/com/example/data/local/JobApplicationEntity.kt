package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.JobApplication
import com.example.data.model.StageInfo

@Entity(
    tableName = "job_applications",
    indices = [Index(value = ["orgName", "postName"], unique = true)]
)
data class JobApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val applyDate: String,
    val day: String,
    val orgName: String,
    val postName: String,
    val userId: String,
    val password: String,
    val mcqStatusString: String,
    val writtenStatusString: String,
    val typingStatusString: String,
    val vivaStatusString: String,
    val appPdfUrl: String,
    val admitPdfUrl: String,
    val localAppPdfUri: String,
    val localAdmitPdfUri: String,
    val isCanceled: Boolean,
    val syncStatus: String,
    val lastUpdated: String
) {
    fun toDomain(): JobApplication {
        return JobApplication(
            id = id,
            applyDate = applyDate,
            day = day,
            orgName = orgName,
            postName = postName,
            userId = userId,
            password = password,
            mcq = StageInfo.fromSheetString(mcqStatusString),
            written = StageInfo.fromSheetString(writtenStatusString),
            typing = StageInfo.fromSheetString(typingStatusString),
            viva = StageInfo.fromSheetString(vivaStatusString),
            appPdfUrl = appPdfUrl,
            admitPdfUrl = admitPdfUrl,
            localAppPdfUri = localAppPdfUri,
            localAdmitPdfUri = localAdmitPdfUri,
            isCanceled = isCanceled,
            syncStatus = syncStatus,
            lastUpdated = lastUpdated
        )
    }

    companion object {
        fun fromDomain(app: JobApplication): JobApplicationEntity {
            return JobApplicationEntity(
                id = app.id,
                applyDate = app.applyDate,
                day = app.day,
                orgName = app.orgName,
                postName = app.postName,
                userId = app.userId,
                password = app.password,
                mcqStatusString = app.mcq.toSheetString(),
                writtenStatusString = app.written.toSheetString(),
                typingStatusString = app.typing.toSheetString(),
                vivaStatusString = app.viva.toSheetString(),
                appPdfUrl = appPdfUrl(app),
                admitPdfUrl = admitPdfUrl(app),
                localAppPdfUri = app.localAppPdfUri,
                localAdmitPdfUri = app.localAdmitPdfUri,
                isCanceled = app.isCanceled,
                syncStatus = app.syncStatus,
                lastUpdated = app.lastUpdated
            )
        }

        private fun appPdfUrl(app: JobApplication): String = app.appPdfUrl
        private fun admitPdfUrl(app: JobApplication): String = app.admitPdfUrl
    }
}
