package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM job_applications ORDER BY id DESC")
    fun getAllApplications(): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE orgName = :org AND postName = :post LIMIT 1")
    suspend fun getByOrgAndPost(org: String, post: String): JobApplicationEntity?

    @Query("SELECT * FROM job_applications WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): JobApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: JobApplicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<JobApplicationEntity>)

    @Update
    suspend fun update(entity: JobApplicationEntity)

    @Query("DELETE FROM job_applications WHERE orgName = :org AND postName = :post")
    suspend fun deleteByOrgAndPost(org: String, post: String): Int

    @Query("DELETE FROM job_applications WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query("UPDATE job_applications SET appPdfUrl = '', admitPdfUrl = '' WHERE orgName = :org AND postName = :post")
    suspend fun clearDriveFiles(org: String, post: String): Int

    @Query("DELETE FROM job_applications")
    suspend fun clearAll()
}
