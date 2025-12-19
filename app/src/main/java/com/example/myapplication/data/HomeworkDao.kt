package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    // HomeworkTemplate operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomeworkTemplate(template: HomeworkTemplate)

    @Update
    suspend fun updateHomeworkTemplate(template: HomeworkTemplate)

    @Delete
    suspend fun deleteHomeworkTemplate(template: HomeworkTemplate)

    @Query("SELECT * FROM homework_templates")
    fun getAllHomeworkTemplates(): Flow<List<HomeworkTemplate>>

    // Homework operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: Homework)

    @Update
    suspend fun updateHomework(homework: Homework)

    @Delete
    suspend fun deleteHomework(homework: Homework)

    @Query("SELECT * FROM homework WHERE student_id = :studentId")
    fun getHomeworkForStudent(studentId: Long): Flow<List<Homework>>
}
