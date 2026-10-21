package com.example.data.model

data class StageInfo(
    val status: StageStatus = StageStatus.NO,
    val date: String = "",
    val rating: String = "Good"
) {
    fun toSheetString(): String {
        return when (status) {
            StageStatus.FAILED -> "❌ Failed"
            StageStatus.PASSED -> {
                val d = date.ifBlank { "" }
                val r = rating.ifBlank { "Good" }
                "✅ $d ($r)"
            }
            StageStatus.NO -> "⏳ No"
        }
    }

    companion object {
        fun fromSheetString(value: String?): StageInfo {
            if (value.isNullOrBlank() || value.contains("⏳") || value == "No") {
                return StageInfo(status = StageStatus.NO)
            }
            if (value.contains("❌") || value.contains("Failed", ignoreCase = true)) {
                return StageInfo(status = StageStatus.FAILED)
            }
            if (value.contains("✅") || value.contains("Passed", ignoreCase = true)) {
                // Format: "✅ YYYY-MM-DD (Rating)"
                val trimmed = value.replace("✅", "").trim()
                var date = ""
                var rating = "Good"
                val ratingMatch = Regex("""\(([^)]+)\)""").find(trimmed)
                if (ratingMatch != null) {
                    rating = ratingMatch.groupValues[1].trim()
                }
                val parts = trimmed.split(" ")
                if (parts.isNotEmpty()) {
                    date = parts[0].trim()
                }
                return StageInfo(
                    status = StageStatus.PASSED,
                    date = date,
                    rating = rating
                )
            }
            return StageInfo(status = StageStatus.NO)
        }
    }
}

enum class StageStatus {
    NO,
    PASSED,
    FAILED;

    fun displayName(): String = when (this) {
        NO -> "No / Pending"
        PASSED -> "Passed"
        FAILED -> "Failed"
    }
}

enum class ExamCategory {
    ALL,
    UPCOMING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELED;

    fun displayName(): String = when (this) {
        ALL -> "All"
        UPCOMING -> "Upcoming"
        RUNNING -> "Running"
        COMPLETED -> "Completed"
        FAILED -> "Failed"
        CANCELED -> "Canceled"
    }
}

data class JobApplication(
    val id: Long = 0,
    val applyDate: String = "",
    val day: String = "",
    val orgName: String = "",
    val postName: String = "",
    val userId: String = "",
    val password: String = "",
    val mcq: StageInfo = StageInfo(),
    val written: StageInfo = StageInfo(),
    val typing: StageInfo = StageInfo(),
    val viva: StageInfo = StageInfo(),
    val appPdfUrl: String = "",
    val admitPdfUrl: String = "",
    val localAppPdfUri: String = "",
    val localAdmitPdfUri: String = "",
    val isCanceled: Boolean = false,
    val syncStatus: String = "Saved", // "Saved", "Synced", "Pending"
    val lastUpdated: String = ""
) {
    val category: ExamCategory
        get() {
            if (isCanceled) return ExamCategory.CANCELED
            val stages = listOf(mcq, written, typing, viva)
            if (stages.any { it.status == StageStatus.FAILED }) return ExamCategory.FAILED
            val passedCount = stages.count { it.status == StageStatus.PASSED }
            if (passedCount == 4) return ExamCategory.COMPLETED
            if (passedCount > 0) return ExamCategory.RUNNING
            return ExamCategory.UPCOMING
        }

    val passedStagesCount: Int
        get() = listOf(mcq, written, typing, viva).count { it.status == StageStatus.PASSED }
}

data class DashboardStats(
    val totalApplications: Int = 0,
    val totalExams: Int = 0,
    val totalFailed: Int = 0,
    val totalCanceled: Int = 0,
    val mcqPassed: Int = 0,
    val mcqFailed: Int = 0,
    val mcqPending: Int = 0,
    val writtenPassed: Int = 0,
    val writtenFailed: Int = 0,
    val writtenPending: Int = 0,
    val typingPassed: Int = 0,
    val typingFailed: Int = 0,
    val typingPending: Int = 0,
    val vivaPassed: Int = 0,
    val vivaFailed: Int = 0,
    val vivaPending: Int = 0
)
