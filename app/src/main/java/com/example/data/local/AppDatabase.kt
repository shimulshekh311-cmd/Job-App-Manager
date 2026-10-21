package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [JobApplicationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun jobDao(): JobDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "job_app_manager_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.jobDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: JobDao) {
                val samples = listOf(
                    // 1. Running Exam (MCQ & Written passed, Typing pending)
                    JobApplicationEntity(
                        applyDate = "2024-04-12",
                        day = "Friday",
                        orgName = "Bangladesh Bank",
                        postName = "Assistant Director (General)",
                        userId = "BB_AD_8832",
                        password = "Pass#2024Bank",
                        mcqStatusString = "✅ 2024-05-18 (Very Good)",
                        writtenStatusString = "✅ 2024-07-20 (Good)",
                        typingStatusString = "⏳ No",
                        vivaStatusString = "⏳ No",
                        appPdfUrl = "https://drive.google.com/file/d/1sampleBBAppPdfUrl/view",
                        admitPdfUrl = "https://drive.google.com/file/d/1sampleBBAdmitPdfUrl/view",
                        localAppPdfUri = "",
                        localAdmitPdfUri = "",
                        isCanceled = false,
                        syncStatus = "Saved",
                        lastUpdated = "2024-07-22"
                    ),
                    // 2. Upcoming Exam (Applied, no exams taken yet)
                    JobApplicationEntity(
                        applyDate = "2024-08-01",
                        day = "Thursday",
                        orgName = "Biman Bangladesh Airlines",
                        postName = "Junior Officer (Administration)",
                        userId = "BIMAN_JO_4519",
                        password = "FlyHigh@2024",
                        mcqStatusString = "⏳ No",
                        writtenStatusString = "⏳ No",
                        typingStatusString = "⏳ No",
                        vivaStatusString = "⏳ No",
                        appPdfUrl = "https://drive.google.com/file/d/1sampleBimanApp/view",
                        admitPdfUrl = "",
                        localAppPdfUri = "",
                        localAdmitPdfUri = "",
                        isCanceled = false,
                        syncStatus = "Saved",
                        lastUpdated = "2024-08-01"
                    ),
                    // 3. Fully Completed (All 4 stages passed!)
                    JobApplicationEntity(
                        applyDate = "2023-11-15",
                        day = "Wednesday",
                        orgName = "Sonali Bank PLC",
                        postName = "Officer (Cash)",
                        userId = "SBL_CASH_9021",
                        password = "SonaliSecure9!",
                        mcqStatusString = "✅ 2023-12-10 (Very Good)",
                        writtenStatusString = "✅ 2024-02-14 (Good)",
                        typingStatusString = "✅ 2024-03-25 (Very Good)",
                        vivaStatusString = "✅ 2024-05-02 (Very Good)",
                        appPdfUrl = "https://drive.google.com/file/d/1sampleSonaliApp/view",
                        admitPdfUrl = "https://drive.google.com/file/d/1sampleSonaliAdmit/view",
                        localAppPdfUri = "",
                        localAdmitPdfUri = "",
                        isCanceled = false,
                        syncStatus = "Saved",
                        lastUpdated = "2024-05-05"
                    ),
                    // 4. Failed Application (Failed in Written)
                    JobApplicationEntity(
                        applyDate = "2024-01-10",
                        day = "Wednesday",
                        orgName = "Rupali Bank Limited",
                        postName = "Senior Officer (IT)",
                        userId = "RBL_IT_1109",
                        password = "TechPass#77",
                        mcqStatusString = "✅ 2024-02-17 (Good)",
                        writtenStatusString = "❌ Failed",
                        typingStatusString = "⏳ No",
                        vivaStatusString = "⏳ No",
                        appPdfUrl = "",
                        admitPdfUrl = "",
                        localAppPdfUri = "",
                        localAdmitPdfUri = "",
                        isCanceled = false,
                        syncStatus = "Saved",
                        lastUpdated = "2024-03-01"
                    ),
                    // 5. Canceled Application
                    JobApplicationEntity(
                        applyDate = "2024-03-05",
                        day = "Tuesday",
                        orgName = "Agrani Bank PLC",
                        postName = "Officer General",
                        userId = "AGR_GEN_5521",
                        password = "AgraniKey$8",
                        mcqStatusString = "⏳ No",
                        writtenStatusString = "⏳ No",
                        typingStatusString = "⏳ No",
                        vivaStatusString = "⏳ No",
                        appPdfUrl = "",
                        admitPdfUrl = "",
                        localAppPdfUri = "",
                        localAdmitPdfUri = "",
                        isCanceled = true,
                        syncStatus = "Saved",
                        lastUpdated = "2024-03-10"
                    )
                )
                dao.insertAll(samples)
            }
        }
    }
}
