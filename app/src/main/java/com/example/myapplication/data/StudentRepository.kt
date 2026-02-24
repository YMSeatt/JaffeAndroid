package com.example.myapplication.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.Flow

class StudentRepository(
    private val studentDao: StudentDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val quizLogDao: QuizLogDao,
    private val furnitureDao: FurnitureDao,
    private val layoutTemplateDao: LayoutTemplateDao,
    private val quizMarkTypeDao: QuizMarkTypeDao,
    private val context: Context
) {
    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()

    fun getHomeworkLogsForStudent(studentId: Long): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getHomeworkLogsForStudent(studentId)
    }

    suspend fun insertHomeworkLog(homeworkLog: HomeworkLog): Long {
        return homeworkLogDao.insert(homeworkLog)
    }

    suspend fun deleteHomeworkLogById(logId: Long) {
        homeworkLogDao.deleteHomeworkLog(logId)
    }

    fun getAllFurniture(): Flow<List<Furniture>> {
        return furnitureDao.getAllFurniture()
    }

    fun getAllLayoutTemplates(): Flow<List<LayoutTemplate>> {
        return layoutTemplateDao.getAllLayoutTemplates().asFlow()
    }

    fun getAllQuizMarkTypes(): Flow<List<QuizMarkType>> {
        return quizMarkTypeDao.getAllQuizMarkTypes()
    }

    suspend fun insertStudent(student: Student): Long {
        return studentDao.insert(student)
    }

    suspend fun insertStudents(students: List<Student>): List<Long> {
        return studentDao.insertAll(students)
    }

    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    suspend fun deleteStudents(students: List<Student>) {
        studentDao.deleteAll(students)
    }

    suspend fun getStudentById(studentId: Long): Student? {
        return studentDao.getStudentByIdNonLiveData(studentId)
    }

    suspend fun studentExists(firstName: String, lastName: String): Boolean {
        return studentDao.studentExists(firstName, lastName)
    }

    suspend fun insertFurniture(furniture: Furniture): Long {
        return furnitureDao.insert(furniture)
    }

    suspend fun updateFurniture(furniture: Furniture) {
        furnitureDao.update(furniture)
    }

    suspend fun getFurnitureById(furnitureId: Long): Furniture? {
        return furnitureDao.getFurnitureById(furnitureId)
    }

    suspend fun deleteFurniture(furniture: Furniture) {
        furnitureDao.delete(furniture)
    }

    suspend fun insertLayoutTemplate(layout: LayoutTemplate) {
        layoutTemplateDao.insertLayoutTemplate(layout)
    }

    suspend fun deleteLayoutTemplate(layout: LayoutTemplate) {
        layoutTemplateDao.deleteLayoutTemplate(layout)
    }

    suspend fun insertBehaviorEvent(event: BehaviorEvent): Long {
        return behaviorEventDao.insert(event)
    }

    /**
     * BOLT: Centralized filtered fetch logic to handle the distinction between
     * null studentIds (all students) and empty studentIds (no students).
     */
    suspend fun getFilteredStudents(studentIds: List<Long>?): List<Student> {
        return when {
            studentIds == null -> studentDao.getAllStudentsNonLiveData()
            studentIds.isEmpty() -> emptyList()
            else -> studentDao.getStudentsByIdsList(studentIds)
        }
    }

    suspend fun getFilteredBehaviorEvents(startDate: Long, endDate: Long, studentIds: List<Long>? = null): List<BehaviorEvent> {
        return when {
            studentIds == null -> behaviorEventDao.getFilteredBehaviorEvents(startDate, endDate)
            studentIds.isEmpty() -> emptyList()
            else -> behaviorEventDao.getFilteredBehaviorEventsWithStudents(startDate, endDate, studentIds)
        }
    }

    suspend fun getFilteredHomeworkLogs(startDate: Long, endDate: Long, studentIds: List<Long>? = null): List<HomeworkLog> {
        return when {
            studentIds == null -> homeworkLogDao.getFilteredHomeworkLogs(startDate, endDate)
            studentIds.isEmpty() -> emptyList()
            else -> homeworkLogDao.getFilteredHomeworkLogsWithStudents(startDate, endDate, studentIds)
        }
    }

    suspend fun getFilteredQuizLogs(startDate: Long, endDate: Long, studentIds: List<Long>? = null): List<QuizLog> {
        return when {
            studentIds == null -> quizLogDao.getFilteredQuizLogs(startDate, endDate)
            studentIds.isEmpty() -> emptyList()
            else -> quizLogDao.getFilteredQuizLogsWithStudents(startDate, endDate, studentIds)
        }
    }

    suspend fun insertQuizLog(log: QuizLog): Long {
        return quizLogDao.insert(log)
    }

    suspend fun deleteQuizLog(log: QuizLog) {
        quizLogDao.deleteQuizLog(log)
    }

    suspend fun getStudentsByIdsList(studentIds: List<Long>): List<Student> {
        return studentDao.getStudentsByIdsList(studentIds)
    }

    suspend fun getAllStudentsNonLiveData(): List<Student> {
        return studentDao.getAllStudentsNonLiveData()
    }
}
