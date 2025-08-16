package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface QuizMarkTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(quizMarkType: QuizMarkType)

    @Update
    suspend fun update(quizMarkType: QuizMarkType)

    @Delete
    suspend fun delete(quizMarkType: QuizMarkType)

    @Query("SELECT * FROM quiz_mark_types ORDER BY name ASC")
    fun getAllQuizMarkTypes(): LiveData<List<QuizMarkType>>
}
