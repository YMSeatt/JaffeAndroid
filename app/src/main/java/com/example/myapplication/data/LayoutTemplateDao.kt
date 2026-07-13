package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for layout templates.
 */
@Dao
interface LayoutTemplateDao {
    /**
     * Saves a new layout template or updates an existing one.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLayoutTemplate(layoutTemplate: LayoutTemplate)

    @Update
    suspend fun updateLayoutTemplate(layoutTemplate: LayoutTemplate)

    @Delete
    suspend fun deleteLayoutTemplate(layoutTemplate: LayoutTemplate)

    /**
     * Retrieves all saved layout templates as a reactive [LiveData] stream.
     */
    @Query("SELECT * FROM layout_templates ORDER BY name ASC")
    fun getAllLayoutTemplates(): LiveData<List<LayoutTemplate>>

    /**
     * Fetches a specific template by its ID.
     */
    @Query("SELECT * FROM layout_templates WHERE id = :id")
    suspend fun getLayoutTemplateById(id: Long): LayoutTemplate?

    /**
     * Deletes a template by its primary key ID.
     */
    @Query("DELETE FROM layout_templates WHERE id = :id")
    suspend fun deleteLayoutTemplateById(id: Long)
}
