package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface LayoutTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayoutTemplate(layoutTemplate: LayoutTemplate)

    @Update
    suspend fun updateLayoutTemplate(layoutTemplate: LayoutTemplate)

    @Delete
    suspend fun deleteLayoutTemplate(layoutTemplate: LayoutTemplate)

    @Query("SELECT * FROM layout_templates ORDER BY name ASC")
    fun getAllLayoutTemplates(): LiveData<List<LayoutTemplate>>
}
