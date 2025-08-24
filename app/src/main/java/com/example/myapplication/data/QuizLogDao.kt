package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface QuizLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: QuizLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<QuizLog>)

    @Update
    suspend fun updateQuizLog(log: QuizLog)

    @Delete
    suspend fun deleteQuizLog(log: QuizLog)

    @Query("SELECT * FROM quiz_logs WHERE studentId = :studentId ORDER BY loggedAt DESC")
    fun getQuizLogsForStudent(studentId: Long): LiveData<List<QuizLog>>

    @Query("SELECT * FROM quiz_logs ORDER BY loggedAt DESC")
    fun getAllQuizLogs(): LiveData<List<QuizLog>>

    @Query("SELECT * FROM quiz_logs ORDER BY loggedAt DESC")
    suspend fun getAllQuizLogsList(): List<QuizLog>

    @Query("SELECT * FROM quiz_logs WHERE loggedAt BETWEEN :startDate AND :endDate ORDER BY loggedAt ASC")
    suspend fun getFilteredQuizLogs(startDate: Long, endDate: Long): List<QuizLog>

    @Query("SELECT * FROM quiz_logs WHERE loggedAt BETWEEN :startDate AND :endDate AND studentId IN (:studentIds) ORDER BY loggedAt ASC")
    suspend fun getFilteredQuizLogsWithStudents(startDate: Long, endDate: Long, studentIds: List<Long>): List<QuizLog>

    @Query("SELECT * FROM quiz_logs WHERE id = :id")
    suspend fun getQuizLogById(id: Long): QuizLog?

    @Query("DELETE FROM quiz_logs WHERE id = :id")
    suspend fun deleteQuizLogById(id: Long)

    @Query("DELETE FROM quiz_logs WHERE studentId = :studentId")
    suspend fun deleteByStudentId(studentId: Long)
}
