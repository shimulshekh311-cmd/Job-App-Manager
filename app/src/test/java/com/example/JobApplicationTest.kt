package com.example

import com.example.data.model.ExamCategory
import com.example.data.model.JobApplication
import com.example.data.model.StageInfo
import com.example.data.model.StageStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JobApplicationTest {

    @Test
    fun testStageInfoParsing() {
        val passedStage = StageInfo.fromSheetString("✅ 2024-05-18 (Very Good)")
        assertEquals(StageStatus.PASSED, passedStage.status)
        assertEquals("2024-05-18", passedStage.date)
        assertEquals("Very Good", passedStage.rating)

        val failedStage = StageInfo.fromSheetString("❌ Failed")
        assertEquals(StageStatus.FAILED, failedStage.status)

        val pendingStage = StageInfo.fromSheetString("⏳ No")
        assertEquals(StageStatus.NO, pendingStage.status)
    }

    @Test
    fun testStageInfoSerialization() {
        val stage = StageInfo(StageStatus.PASSED, "2024-06-01", "Good")
        assertEquals("✅ 2024-06-01 (Good)", stage.toSheetString())

        val failed = StageInfo(StageStatus.FAILED)
        assertEquals("❌ Failed", failed.toSheetString())

        val pending = StageInfo(StageStatus.NO)
        assertEquals("⏳ No", pending.toSheetString())
    }

    @Test
    fun testCategoryCalculation() {
        // Canceled
        val canceled = JobApplication(isCanceled = true)
        assertEquals(ExamCategory.CANCELED, canceled.category)

        // Failed
        val failed = JobApplication(
            mcq = StageInfo(StageStatus.PASSED),
            written = StageInfo(StageStatus.FAILED)
        )
        assertEquals(ExamCategory.FAILED, failed.category)

        // Running
        val running = JobApplication(
            mcq = StageInfo(StageStatus.PASSED),
            written = StageInfo(StageStatus.NO)
        )
        assertEquals(ExamCategory.RUNNING, running.category)

        // Completed
        val completed = JobApplication(
            mcq = StageInfo(StageStatus.PASSED),
            written = StageInfo(StageStatus.PASSED),
            typing = StageInfo(StageStatus.PASSED),
            viva = StageInfo(StageStatus.PASSED)
        )
        assertEquals(ExamCategory.COMPLETED, completed.category)

        // Upcoming
        val upcoming = JobApplication()
        assertEquals(ExamCategory.UPCOMING, upcoming.category)
    }
}
