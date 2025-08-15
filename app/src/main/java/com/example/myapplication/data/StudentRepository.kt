package com.example.myapplication.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentRepository(
    private val studentDao: StudentDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao, // Added HomeworkLogDao
    private val furnitureDao: FurnitureDao
) {

    val studentsForDisplay: LiveData<List<StudentDetailsForDisplay>> = studentDao.getStudentsForDisplay()
    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents() // Added for export
    val allFurniture: Flow<List<Furniture>> = furnitureDao.getAllFurniture()

    // Method to get a student by ID, non-LiveData for suspend contexts
    suspend fun getStudentByIdNonLiveData(studentId: Int): Student? {
        return withContext(Dispatchers.IO) {
            studentDao.getStudentByIdNonLiveData(studentId)
        }
    }

    fun getAllBehaviorEvents(): LiveData<List<BehaviorEvent>> = behaviorEventDao.getAllBehaviorEvents()

    fun getRecentBehaviorEventsForStudent(studentId: Long, limit: Int): LiveData<List<BehaviorEvent>> {
        return studentDao.getRecentBehaviorEventsForStudent(studentId, limit)
    }

    suspend fun insertStudent(student: Student) {
        withContext(Dispatchers.IO) {
            studentDao.insertStudent(student)
        }
    }

    suspend fun updateStudent(student: Student) {
        withContext(Dispatchers.IO) {
            studentDao.updateStudent(student)
        }
    }

    suspend fun deleteStudent(student: Student) {
        withContext(Dispatchers.IO) {
            studentDao.deleteStudent(student)
        }
    }

    suspend fun insertBehaviorEvent(event: BehaviorEvent) {
        withContext(Dispatchers.IO) {
            behaviorEventDao.insertBehaviorEvent(event)
        }
    }

    // Furniture methods
    suspend fun insertFurniture(furniture: Furniture) {
        withContext(Dispatchers.IO) {
            furnitureDao.insert(furniture)
        }
    }

    suspend fun updateFurniture(furniture: Furniture) {
        withContext(Dispatchers.IO) {
            furnitureDao.update(furniture)
        }
    }

    suspend fun deleteFurnitureById(id: Int) {
        withContext(Dispatchers.IO) {
            furnitureDao.deleteById(id)
        }
    }

    suspend fun getFurnitureById(id: Int): Furniture? {
        return withContext(Dispatchers.IO) {
            furnitureDao.getFurnitureById(id)
        }
    }

    // HomeworkLog methods
    suspend fun insertHomeworkLog(homeworkLog: HomeworkLog) {
        withContext(Dispatchers.IO) {
            homeworkLogDao.insertHomeworkLog(homeworkLog)
        }
    }

    fun getHomeworkLogsForStudent(studentId: Long): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getHomeworkLogsForStudent(studentId)
    }

    fun getAllHomeworkLogs(): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getAllHomeworkLogs()
    }

    suspend fun deleteHomeworkLog(logId: Long) {
        withContext(Dispatchers.IO) {
            homeworkLogDao.deleteHomeworkLog(logId)
        }
    }

    fun getRecentHomeworkLogsForStudent(studentId: Long, limit: Int): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getRecentHomeworkLogsForStudent(studentId, limit)
    }
}
