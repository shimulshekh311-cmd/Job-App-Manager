package com.example

import com.example.data.model.ExamCategory
import com.example.data.model.JobApplication
import com.example.data.model.StageInfo
import com.example.data.model.StageStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class JobApplicationTest {

    @Test
    fun testStageInfoParsing() {
        val passedStage = StageInfo.fromSheetString("✅ 2024-05-18 (Very Good) [Marks: 75/100 (75.0%)]")
        assertEquals(StageStatus.PASSED, passedStage.status)
        assertEquals("2024-05-18", passedStage.date)
        assertEquals("Very Good", passedStage.rating)
        assertEquals("75", passedStage.obtainedMarks)
        assertEquals("100", passedStage.totalMarks)
        assertEquals(75.0, passedStage.percentage ?: 0.0, 0.01)

        val failedStage = StageInfo.fromSheetString("❌ Failed [Marks: 35/100 (35.0%)]")
        assertEquals(StageStatus.FAILED, failedStage.status)
        assertEquals("35", failedStage.obtainedMarks)

        val pendingStage = StageInfo.fromSheetString("⏳ No")
        assertEquals(StageStatus.NO, pendingStage.status)
    }

    @Test
    fun testPercentageCalculation() {
        val stage = StageInfo(
            status = StageStatus.PASSED,
            obtainedMarks = "80",
            totalMarks = "100"
        )
        assertEquals(80.0, stage.percentage ?: 0.0, 0.01)
        assertEquals("80.0%", stage.formattedPercentage)
    }

    @Test
    fun testStageInfoSerialization() {
        val stage = StageInfo(
            status = StageStatus.PASSED,
            date = "2024-06-01",
            rating = "খুব ভালো",
            obtainedMarks = "80",
            totalMarks = "100"
        )
        assertEquals("✅ 2024-06-01 (খুব ভালো) [Marks: 80/100 (80.0%)]", stage.toSheetString())

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

    @Test
    fun testDateDescendingSorting() {
        val app1 = JobApplication(id = 1, applyDate = "2024-08-01", orgName = "App 1")
        val app2 = JobApplication(id = 2, applyDate = "2023-11-15", orgName = "App 2")
        val app3 = JobApplication(id = 3, applyDate = "2024-04-12", orgName = "App 3")
        val app4 = JobApplication(id = 4, applyDate = "2024-01-10", orgName = "App 4")

        val list = listOf(app1, app2, app3, app4)
        val dateDescendingComparator = compareByDescending<JobApplication>(
            { it.applyDate.trim() }
        ).thenByDescending { it.id }

        val sorted = list.sortedWith(dateDescendingComparator)

        // Newest dates at the top, older dates at the bottom
        assertEquals("2024-08-01", sorted[0].applyDate)
        assertEquals("2024-04-12", sorted[1].applyDate)
        assertEquals("2024-01-10", sorted[2].applyDate)
        assertEquals("2023-11-15", sorted[3].applyDate)
    }

    @Test
    fun testPdfAttachmentHandling() {
        val appWithDrive = JobApplication(
            appPdfUrl = "https://drive.google.com/file/d/12345/view",
            admitPdfUrl = "https://drive.google.com/file/d/67890/view"
        )
        assertEquals(true, appWithDrive.appPdfUrl.isNotBlank())
        assertEquals(true, appWithDrive.admitPdfUrl.isNotBlank())

        val appWithLocal = JobApplication(
            localAppPdfUri = "content://com.example.fileprovider/files/pdfs/app.pdf",
            localAdmitPdfUri = "content://com.example.fileprovider/files/pdfs/admit.pdf"
        )
        assertEquals(true, appWithLocal.localAppPdfUri.isNotBlank())
        assertEquals(true, appWithLocal.localAdmitPdfUri.isNotBlank())
    }
}
