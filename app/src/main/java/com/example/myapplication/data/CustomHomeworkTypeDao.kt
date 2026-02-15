package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Query("SELECT * FROM custom_homework_types ORDER BY name ASC")
    suspend fun getAllCustomHomeworkTypesList(): List<CustomHomeworkType>

    @Query("DELETE FROM custom_homework_types")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(types: List<CustomHomeworkType>)

    @Transaction
    suspend fun replaceAll(types: List<CustomHomeworkType>) {
        deleteAll()
        insertAll(types)
    }
}
