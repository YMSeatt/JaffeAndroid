package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for student quiz performance logs.
 *
 * This DAO handles both historical quiz records and active "In-Progress" session logs,
 * enabling real-time academic progress tracking.
 */
@Dao
interface QuizLogDao {
    /** Inserts or replaces a quiz log entry. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: QuizLog): Long

    /** Bulk inserts quiz logs, used for migrating session data to persistent storage. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<QuizLog>): List<Long>

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

    @Query("SELECT * FROM quiz_logs WHERE studentId = :studentId ORDER BY loggedAt DESC LIMIT :limit")
    suspend fun getRecentQuizLogsForStudentList(studentId: Long, limit: Int): List<QuizLog>

    /**
     * Retrieves quiz logs for UI display on student icons, specifically filtering for
     * active or recent relevant data.
     *
     * ### Visibility Logic:
     * 1. **User Clearance**: Excludes logs that occurred before the student's [lastCleared] timestamp.
     * 2. **Session Context**: Only selects logs where `isComplete = 0`. This ensures that only
     *    in-progress quiz sessions or recently unfinished logs appear on the chart icons,
     *    avoiding clutter from finished historical quizzes.
     * 3. **Temporal Decay**: Excludes logs older than [quizDisplayTimeout] hours.
     */
    @Query("""
        SELECT * FROM quiz_logs 
        WHERE studentId = :studentId 
        AND loggedAt > :lastCleared 
        AND isComplete = 0 
        AND (:quizDisplayTimeout = 0 OR :currentTime < loggedAt + (:quizDisplayTimeout * 3600000)) 
        ORDER BY loggedAt DESC 
        LIMIT :limit
    """)
    suspend fun getRecentQuizLogsForStudentListFiltered(
        studentId: Long,
        limit: Int,
        lastCleared: Long,
        quizDisplayTimeout: Int,
        currentTime: Long
    ): List<QuizLog>
}
