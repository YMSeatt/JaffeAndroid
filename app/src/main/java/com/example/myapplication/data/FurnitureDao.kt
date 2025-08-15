package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FurnitureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(furniture: Furniture)

    @Update
    suspend fun update(furniture: Furniture)

    @Query("DELETE FROM furniture WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM furniture")
    fun getAllFurniture(): Flow<List<Furniture>>

    @Query("SELECT * FROM furniture WHERE id = :id")
    suspend fun getFurnitureById(id: Int): Furniture?
}
