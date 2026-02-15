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
interface CustomBehaviorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customBehavior: CustomBehavior)

    @Update
    suspend fun update(customBehavior: CustomBehavior)

    @Delete
    suspend fun delete(customBehavior: CustomBehavior)

    @Query("SELECT * FROM custom_behaviors ORDER BY name ASC")
    fun getAllCustomBehaviors(): LiveData<List<CustomBehavior>>

    @Query("DELETE FROM custom_behaviors")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(behaviors: List<CustomBehavior>)

    @Transaction
    suspend fun replaceAll(behaviors: List<CustomBehavior>) {
        deleteAll()
        insertAll(behaviors)
    }
}
