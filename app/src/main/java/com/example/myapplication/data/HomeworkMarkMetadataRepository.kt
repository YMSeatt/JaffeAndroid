package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeworkMarkMetadataRepository @Inject constructor(
    private val homeworkMarkMetadataDao: HomeworkMarkMetadataDao
) {
    fun getAll(): Flow<List<HomeworkMarkMetadata>> = homeworkMarkMetadataDao.getAllHomeworkMarkMetadata()

    suspend fun getAllList(): List<HomeworkMarkMetadata> = homeworkMarkMetadataDao.getAllHomeworkMarkMetadataList()

    suspend fun insert(metadata: HomeworkMarkMetadata) {
        homeworkMarkMetadataDao.insert(metadata)
    }

    suspend fun update(metadata: HomeworkMarkMetadata) {
        homeworkMarkMetadataDao.update(metadata)
    }

    suspend fun delete(metadata: HomeworkMarkMetadata) {
        homeworkMarkMetadataDao.delete(metadata)
    }

    suspend fun deleteAll() {
        homeworkMarkMetadataDao.deleteAll()
    }
}
