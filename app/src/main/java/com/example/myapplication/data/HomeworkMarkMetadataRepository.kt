package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that coordinates access to [HomeworkMarkMetadata].
 *
 * This repository provides the business logic for managing the mapping of homework
 * status labels to numeric point values. It is utilized by ViewModels to manage
 * configurations and by the `HomeworkScoreEngine` to calculate longitudinal scores.
 */
@Singleton
class HomeworkMarkMetadataRepository @Inject constructor(
    private val homeworkMarkMetadataDao: HomeworkMarkMetadataDao
) {
    /**
     * Returns a reactive stream of all homework mark metadata.
     */
    fun getAll(): Flow<List<HomeworkMarkMetadata>> = homeworkMarkMetadataDao.getAllHomeworkMarkMetadata()

    /**
     * Returns a one-shot list of all homework mark metadata.
     * Essential for initializing the [com.example.myapplication.util.HomeworkScoreEngine.HomeworkScoringContext].
     */
    suspend fun getAllList(): List<HomeworkMarkMetadata> = homeworkMarkMetadataDao.getAllHomeworkMarkMetadataList()

    /**
     * Persists a new homework metadata entry.
     */
    suspend fun insert(metadata: HomeworkMarkMetadata) {
        homeworkMarkMetadataDao.insert(metadata)
    }

    /**
     * Updates an existing homework metadata entry.
     */
    suspend fun update(metadata: HomeworkMarkMetadata) {
        homeworkMarkMetadataDao.update(metadata)
    }

    /**
     * Deletes a specific metadata entry.
     */
    suspend fun delete(metadata: HomeworkMarkMetadata) {
        homeworkMarkMetadataDao.delete(metadata)
    }

    /**
     * Clears all homework metadata from the system.
     */
    suspend fun deleteAll() {
        homeworkMarkMetadataDao.deleteAll()
    }

    /**
     * Batch inserts a list of metadata entries.
     */
    suspend fun insertAll(metadataList: List<HomeworkMarkMetadata>) {
        homeworkMarkMetadataDao.insertAll(metadataList)
    }

    /**
     * Atomically replaces all existing metadata with a new list.
     */
    suspend fun replaceAll(metadataList: List<HomeworkMarkMetadata>) {
        homeworkMarkMetadataDao.replaceAll(metadataList)
    }
}
