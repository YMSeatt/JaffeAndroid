package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface HomeworkLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworkLog(homeworkLog: HomeworkLog)

    @Update
    suspend fun updateHomeworkLog(homeworkLog: HomeworkLog)

    @Query("SELECT * FROM homework_logs WHERE studentId = :studentId ORDER BY loggedAt DESC")
    fun getHomeworkLogsForStudent(studentId: Int): LiveData<List<HomeworkLog>>

    @Query("SELECT * FROM homework_logs ORDER BY loggedAt DESC")
    fun getAllHomeworkLogs(): LiveData<List<HomeworkLog>>

    @Query("DELETE FROM homework_logs WHERE id = :logId")
    suspend fun deleteHomeworkLog(logId: Long)

    @Query("SELECT * FROM homework_logs WHERE studentId = :studentId ORDER BY loggedAt DESC LIMIT :limit")
    fun getRecentHomeworkLogsForStudent(studentId: Int, limit: Int): LiveData<List<HomeworkLog>>
}
