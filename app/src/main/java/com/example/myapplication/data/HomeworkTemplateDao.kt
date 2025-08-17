package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface HomeworkTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(homeworkTemplate: HomeworkTemplate)

    @Update
    suspend fun update(homeworkTemplate: HomeworkTemplate)

    @Delete
    suspend fun delete(homeworkTemplate: HomeworkTemplate)

    @Query("SELECT * FROM homework_templates ORDER BY name ASC")
    fun getAllHomeworkTemplates(): kotlinx.coroutines.flow.Flow<List<HomeworkTemplate>>
}
