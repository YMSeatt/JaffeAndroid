package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [HomeworkMarkMetadata] entities.
 *
 * This DAO manages the persistence of metadata used to map homework status labels
 * (e.g., "Complete", "Submitted Late") to numeric point values.
 */
@Dao
interface HomeworkMarkMetadataDao {
    /**
     * Observes all homework mark metadata, sorted alphabetically.
     */
    @Query("SELECT * FROM homework_mark_metadata ORDER BY name ASC")
    fun getAllHomeworkMarkMetadata(): Flow<List<HomeworkMarkMetadata>>

    /**
     * Retrieves a one-shot list of all homework mark metadata.
     */
    @Query("SELECT * FROM homework_mark_metadata ORDER BY name ASC")
    suspend fun getAllHomeworkMarkMetadataList(): List<HomeworkMarkMetadata>

    /**
     * Inserts a single metadata entry. Replaces on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: HomeworkMarkMetadata)

    /**
     * Updates an existing metadata entry's name or point value.
     */
    @Update
    suspend fun update(metadata: HomeworkMarkMetadata)

    /**
     * Deletes a metadata entry.
     */
    @Delete
    suspend fun delete(metadata: HomeworkMarkMetadata)

    /**
     * Clears all homework scoring metadata.
     */
    @Query("DELETE FROM homework_mark_metadata")
    suspend fun deleteAll()

    /**
     * Inserts multiple metadata entries in a single operation.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(metadataList: List<HomeworkMarkMetadata>)

    /**
     * Atomically replaces the entire homework scoring metadata library.
     * Often used during data sync or when restoring default scoring rules.
     */
    @Transaction
    suspend fun replaceAll(metadataList: List<HomeworkMarkMetadata>) {
        deleteAll()
        insertAll(metadataList)
    }
}
