package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkMarkMetadataDao {
    @Query("SELECT * FROM homework_mark_metadata ORDER BY name ASC")
    fun getAllHomeworkMarkMetadata(): Flow<List<HomeworkMarkMetadata>>

    @Query("SELECT * FROM homework_mark_metadata ORDER BY name ASC")
    suspend fun getAllHomeworkMarkMetadataList(): List<HomeworkMarkMetadata>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: HomeworkMarkMetadata)

    @Update
    suspend fun update(metadata: HomeworkMarkMetadata)

    @Delete
    suspend fun delete(metadata: HomeworkMarkMetadata)

    @Query("DELETE FROM homework_mark_metadata")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(metadataList: List<HomeworkMarkMetadata>)

    @Transaction
    suspend fun replaceAll(metadataList: List<HomeworkMarkMetadata>) {
        deleteAll()
        insertAll(metadataList)
    }
}
