package com.example.data.model

import java.util.Locale

enum class StageStatus {
    NO,                     // Not reached or not appeared yet
    APPEARED_PENDING_RESULT,// Exam given, marks entered, awaiting pass/fail result
    PASSED,                 // Passed this stage
    FAILED,                 // Failed this stage
    CANCELED;               // Stage or application canceled

    fun displayName(): String = when (this) {
        NO -> "পরিক্ষা হয়নি / বাকি"
        APPEARED_PENDING_RESULT -> "ফলাফল অপেক্ষমাণ"
        PASSED -> "পাস"
        FAILED -> "ফেল"
        CANCELED -> "বাতিল"
    }
}

data class StageInfo(
    val status: StageStatus = StageStatus.NO,
    val date: String = "",
    val rating: String = "ভালো", // খুব ভালো, ভালো, মাঝারি, খারাপ, খুব খারাপ
    val obtainedMarks: String = "",
    val totalMarks: String = "",
    val isCanceled: Boolean = false
) {
    val percentage: Double?
        get() {
            val obt = obtainedMarks.toDoubleOrNull() ?: return null
            val tot = totalMarks.toDoubleOrNull() ?: return null
            if (tot <= 0.0) return null
            return (obt / tot) * 100.0
        }

    val formattedPercentage: String
        get() {
            val pct = percentage ?: return ""
            return String.format(Locale.US, "%.1f%%", pct)
        }

    fun toSheetString(): String {
        if (isCanceled || status == StageStatus.CANCELED) {
            return "⚠️ Canceled"
        }
        val marksPart = if (obtainedMarks.isNotBlank() && totalMarks.isNotBlank()) {
            " [Marks: $obtainedMarks/$totalMarks ($formattedPercentage)]"
        } else ""

        return when (status) {
            StageStatus.PASSED -> {
                val d = date.ifBlank { "" }
                val r = rating.ifBlank { "ভালো" }
                "✅ $d ($r)$marksPart"
            }
            StageStatus.FAILED -> "❌ Failed$marksPart"
            StageStatus.APPEARED_PENDING_RESULT -> {
                val d = date.ifBlank { "" }
                val r = rating.ifBlank { "ভালো" }
                "⏳ Exam: $d ($r)$marksPart (Pending Result)"
            }
            StageStatus.NO -> "⏳ No"
            StageStatus.CANCELED -> "⚠️ Canceled"
        }
    }

    companion object {
        fun fromSheetString(value: String?): StageInfo {
            if (value.isNullOrBlank() || value == "⏳ No" || value == "No") {
                return StageInfo(status = StageStatus.NO)
            }
            if (value.contains("Canceled", ignoreCase = true) || value.contains("বাতিল")) {
                return StageInfo(status = StageStatus.CANCELED, isCanceled = true)
            }

            // Extract Marks if present: "[Marks: 75/100 (75.0%)]"
            var obtained = ""
            var total = ""
            val marksMatch = Regex("""\[Marks:\s*([0-9.]+)/([0-9.]+)""").find(value)
            if (marksMatch != null) {
                obtained = marksMatch.groupValues[1]
                total = marksMatch.groupValues[2]
            }

            if (value.contains("❌") || value.contains("Failed", ignoreCase = true)) {
                return StageInfo(
                    status = StageStatus.FAILED,
                    obtainedMarks = obtained,
                    totalMarks = total
                )
            }

            var date = ""
            var rating = "ভালো"
            val ratingMatch = Regex("""\(([^)]+)\)""").find(value)
            if (ratingMatch != null) {
                rating = ratingMatch.groupValues[1].trim()
            }
            val dateMatch = Regex("""\d{4}-\d{2}-\d{2}""").find(value)
            if (dateMatch != null) {
                date = dateMatch.value
            }

            if (value.contains("Pending Result", ignoreCase = true)) {
                return StageInfo(
                    status = StageStatus.APPEARED_PENDING_RESULT,
                    date = date,
                    rating = rating,
                    obtainedMarks = obtained,
                    totalMarks = total
                )
            }

            if (value.contains("✅") || value.contains("Passed", ignoreCase = true)) {
                return StageInfo(
                    status = StageStatus.PASSED,
                    date = date,
                    rating = rating,
                    obtainedMarks = obtained,
                    totalMarks = total
                )
            }

            return StageInfo(status = StageStatus.NO)
        }
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
    val syncStatus: String = "Saved",
    val lastUpdated: String = ""
) {
    val category: ExamCategory
        get() {
            if (isCanceled || mcq.isCanceled || written.isCanceled || typing.isCanceled || viva.isCanceled) {
                return ExamCategory.CANCELED
            }
            val stages = listOf(mcq, written, typing, viva)
            if (stages.any { it.status == StageStatus.FAILED }) {
                return ExamCategory.FAILED
            }
            val passedCount = stages.count { it.status == StageStatus.PASSED }
            if (passedCount == 4) {
                return ExamCategory.COMPLETED
            }
            if (passedCount > 0 || mcq.status == StageStatus.APPEARED_PENDING_RESULT) {
                return ExamCategory.RUNNING
            }
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
