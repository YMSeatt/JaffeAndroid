package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface HomeworkLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworkLog(homeworkLog: HomeworkLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(homeworkLogs: List<HomeworkLog>)

    @Update
    suspend fun updateHomeworkLog(homeworkLog: HomeworkLog)

    @Delete
    suspend fun delete(homeworkLog: HomeworkLog)

    @Query("SELECT * FROM homework_logs WHERE studentId = :studentId ORDER BY loggedAt DESC")
    fun getHomeworkLogsForStudent(studentId: Long): LiveData<List<HomeworkLog>>

    @Query("SELECT * FROM homework_logs ORDER BY loggedAt DESC")
    fun getAllHomeworkLogs(): LiveData<List<HomeworkLog>>

    @Query("SELECT * FROM homework_logs ORDER BY loggedAt DESC")
    suspend fun getAllHomeworkLogsList(): List<HomeworkLog>

    @Query("SELECT * FROM homework_logs WHERE loggedAt BETWEEN :startDate AND :endDate ORDER BY loggedAt ASC")
    suspend fun getFilteredHomeworkLogs(startDate: Long, endDate: Long): List<HomeworkLog>

    @Query("SELECT * FROM homework_logs WHERE loggedAt BETWEEN :startDate AND :endDate AND studentId IN (:studentIds) ORDER BY loggedAt ASC")
    suspend fun getFilteredHomeworkLogsWithStudents(startDate: Long, endDate: Long, studentIds: List<Long>): List<HomeworkLog>

    @Query("DELETE FROM homework_logs WHERE id = :logId")
    suspend fun deleteHomeworkLog(logId: Long)

    @Query("SELECT * FROM homework_logs WHERE studentId = :studentId ORDER BY loggedAt DESC LIMIT :limit")
    fun getRecentHomeworkLogsForStudent(studentId: Long, limit: Int): LiveData<List<HomeworkLog>>

    @Query("SELECT * FROM homework_logs WHERE studentId = :studentId ORDER BY loggedAt DESC LIMIT :limit")
    suspend fun getRecentHomeworkLogsForStudentList(studentId: Long, limit: Int): List<HomeworkLog>

    @Query("SELECT * FROM homework_logs WHERE id = :id")
    suspend fun getHomeworkLogById(id: Long): HomeworkLog?

    @Query("DELETE FROM homework_logs WHERE studentId = :studentId")
    suspend fun deleteByStudentId(studentId: Long)
}
