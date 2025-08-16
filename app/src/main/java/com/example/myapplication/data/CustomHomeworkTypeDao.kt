package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CustomHomeworkTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customHomeworkType: CustomHomeworkType)

    @Update
    suspend fun update(customHomeworkType: CustomHomeworkType)

    @Delete
    suspend fun delete(customHomeworkType: CustomHomeworkType)

    @Query("SELECT * FROM custom_homework_types ORDER BY name ASC")
    fun getAllCustomHomeworkTypes(): LiveData<List<CustomHomeworkType>>
}
