package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LayoutTemplateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: LayoutTemplate)

    @Delete
    suspend fun delete(template: LayoutTemplate)

    @Query("SELECT * FROM layout_templates ORDER BY name ASC")
    fun getAllTemplates(): LiveData<List<LayoutTemplate>>
}
