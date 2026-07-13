package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for managing automated email reporting schedules.
 *
 * This DAO provides the primary interface for persisting user-defined reporting
 * configurations. The results of these queries drive the periodic evaluation
 * logic in the [com.example.myapplication.worker.EmailSchedulerWorker].
 */
@Dao
interface EmailScheduleDao {
    /**
     * Returns a reactive stream of all reporting schedules, sorted chronologically.
     */
    @Query("SELECT * FROM email_schedules ORDER BY hour, minute ASC")
    fun getAllSchedules(): Flow<List<EmailSchedule>>

    /**
     * Retrieves a static list of all schedules for background processing.
     */
    @Query("SELECT * FROM email_schedules ORDER BY hour, minute ASC")
    suspend fun getAllSchedulesList(): List<EmailSchedule>

    @Insert
    suspend fun insert(schedule: EmailSchedule)

    @Update
    suspend fun update(schedule: EmailSchedule)

    @Delete
    suspend fun delete(schedule: EmailSchedule)
}
