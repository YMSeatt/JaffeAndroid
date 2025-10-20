package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmailScheduleDao {
    @Query("SELECT * FROM email_schedules ORDER BY hour, minute ASC")
    fun getAllSchedules(): Flow<List<EmailSchedule>>

    @Query("SELECT * FROM email_schedules ORDER BY hour, minute ASC")
    suspend fun getAllSchedulesList(): List<EmailSchedule>

    @Insert
    suspend fun insert(schedule: EmailSchedule)

    @Update
    suspend fun update(schedule: EmailSchedule)

    @Delete
    suspend fun delete(schedule: EmailSchedule)
}
