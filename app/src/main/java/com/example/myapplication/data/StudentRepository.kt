package com.example.myapplication.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import com.example.myapplication.data.exporter.ExportOptions
import com.example.myapplication.data.exporter.Exporter
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

    suspend fun exportToExcel(uri: Uri, options: ExportOptions) {
        val students = studentDao.getAllStudentsNonLiveData()
        val behaviorLogs = behaviorEventDao.getAllBehaviorEventsList()
        val homeworkLogs = homeworkLogDao.getAllHomeworkLogsList()
        val quizLogs = quizLogDao.getAllQuizLogsList()

        val exporter = Exporter(context)
        exporter.exportToXlsx(
            uri = uri,
            students = students,
            behaviorLogs = behaviorLogs,
            homeworkLogs = homeworkLogs,
            quizLogs = quizLogs,
            options = options
        )
    }

    fun getHomeworkLogsForStudent(studentId: Long): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getHomeworkLogsForStudent(studentId)
    }

    suspend fun insertHomeworkLog(homeworkLog: HomeworkLog) {
        homeworkLogDao.insert(homeworkLog)
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

    suspend fun insertStudent(student: Student) {
        studentDao.insert(student)
    }

    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    suspend fun getStudentById(studentId: Long): Student? {
        return studentDao.getStudentByIdNonLiveData(studentId)
    }

    suspend fun studentExists(firstName: String, lastName: String): Boolean {
        return studentDao.studentExists(firstName, lastName)
    }

    suspend fun insertFurniture(furniture: Furniture) {
        furnitureDao.insert(furniture)
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

    suspend fun insertBehaviorEvent(event: BehaviorEvent) {
        behaviorEventDao.insert(event)
    }

    suspend fun insertQuizLog(log: QuizLog) {
        quizLogDao.insert(log)
    }

    suspend fun deleteQuizLog(log: QuizLog) {
        quizLogDao.deleteQuizLog(log)
    }
}
