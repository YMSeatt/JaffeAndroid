package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizTemplateDao {
    @Query("SELECT * FROM quiz_templates ORDER BY name ASC")
    fun getAll(): Flow<List<QuizTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quizTemplate: QuizTemplate)

    @Update
    suspend fun update(quizTemplate: QuizTemplate)

    @Delete
    suspend fun delete(quizTemplate: QuizTemplate)
}
