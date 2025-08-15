package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface QuizLogDao {
    @Insert
    suspend fun insert(quizLog: QuizLog)

    @Update
    suspend fun update(quizLog: QuizLog)

    @Query("DELETE FROM quiz_logs WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM quiz_logs WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getQuizLogsForStudent(studentId: Int): LiveData<List<QuizLog>>

    @Query("SELECT * FROM quiz_logs ORDER BY timestamp DESC")
    fun getAllQuizLogs(): LiveData<List<QuizLog>>
}
