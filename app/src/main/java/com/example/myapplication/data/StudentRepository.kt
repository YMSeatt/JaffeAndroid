package com.example.myapplication.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class StudentRepository(
    private val studentDao: StudentDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val furnitureDao: FurnitureDao,
    private val quizLogDao: QuizLogDao, // Added QuizLogDao
    private val studentGroupDao: StudentGroupDao, // Added StudentGroupDao
    private val layoutTemplateDao: LayoutTemplateDao, // Added LayoutTemplateDao
    private val conditionalFormattingRuleDao: ConditionalFormattingRuleDao // Added ConditionalFormattingRuleDao
) {

    val studentsForDisplay: LiveData<List<StudentDetailsForDisplay>> = studentDao.getStudentsForDisplay()
    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()
    val allFurniture: Flow<List<Furniture>> = furnitureDao.getAllFurniture()

    // Student methods
    suspend fun getStudentByIdNonLiveData(studentId: Int): Student? { // Changed from Long to Int
        return withContext(Dispatchers.IO) {
            studentDao.getStudentByIdNonLiveData(studentId)
        }
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

    // BehaviorEvent methods
    fun getAllBehaviorEvents(): LiveData<List<BehaviorEvent>> = behaviorEventDao.getAllBehaviorEvents()

    fun getRecentBehaviorEventsForStudent(studentId: Int, limit: Int): LiveData<List<BehaviorEvent>> {
        return studentDao.getRecentBehaviorEventsForStudent(studentId, limit)
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
 
    suspend fun updateHomeworkLog(homeworkLog: HomeworkLog) { // Added update method
        withContext(Dispatchers.IO) {
            homeworkLogDao.updateHomeworkLog(homeworkLog)
        }
    }

    fun getHomeworkLogsForStudent(studentId: Int): LiveData<List<HomeworkLog>> {
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

    fun getRecentHomeworkLogsForStudent(studentId: Int, limit: Int): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getRecentHomeworkLogsForStudent(studentId, limit)
    }

    // QuizLog methods
    suspend fun insertQuizLog(quizLog: QuizLog) {
        withContext(Dispatchers.IO) {
            quizLogDao.insertQuizLog(quizLog)
        }
    }

    suspend fun updateQuizLog(quizLog: QuizLog) {
        withContext(Dispatchers.IO) {
            quizLogDao.updateQuizLog(quizLog)
        }
    }

    suspend fun deleteQuizLog(quizLog: QuizLog) {
        withContext(Dispatchers.IO) {
            quizLogDao.deleteQuizLog(quizLog)
        }
    }

    fun getQuizLogsForStudent(studentId: Int): LiveData<List<QuizLog>> {
        return quizLogDao.getQuizLogsForStudent(studentId)
    }

    fun getAllQuizLogs(): LiveData<List<QuizLog>> {
        return quizLogDao.getAllQuizLogs()
    }

    // StudentGroup methods
    suspend fun insertStudentGroup(studentGroup: StudentGroup) {
        withContext(Dispatchers.IO) {
            studentGroupDao.insertStudentGroup(studentGroup)
        }
    }

    suspend fun updateStudentGroup(studentGroup: StudentGroup) {
        withContext(Dispatchers.IO) {
            studentGroupDao.updateStudentGroup(studentGroup)
        }
    }

    suspend fun deleteStudentGroup(studentGroup: StudentGroup) {
        withContext(Dispatchers.IO) {
            studentGroupDao.deleteStudentGroup(studentGroup)
        }
    }

    fun getAllStudentGroups(): LiveData<List<StudentGroup>> {
        return studentGroupDao.getAllStudentGroups()
    }

    suspend fun getStudentGroupById(id: Long): StudentGroup? {
        return studentGroupDao.getStudentGroupById(id)
    }

    // LayoutTemplate methods
    suspend fun insertLayoutTemplate(layoutTemplate: LayoutTemplate) {
        withContext(Dispatchers.IO) {
            layoutTemplateDao.insertLayoutTemplate(layoutTemplate)
        }
    }

    suspend fun updateLayoutTemplate(layoutTemplate: LayoutTemplate) {
        withContext(Dispatchers.IO) {
            layoutTemplateDao.updateLayoutTemplate(layoutTemplate)
        }
    }

    suspend fun deleteLayoutTemplate(layoutTemplate: LayoutTemplate) {
        withContext(Dispatchers.IO) {
            layoutTemplateDao.deleteLayoutTemplate(layoutTemplate)
        }
    }

    fun getAllLayoutTemplates(): LiveData<List<LayoutTemplate>> {
        return layoutTemplateDao.getAllLayoutTemplates()
    }

    // ConditionalFormattingRule methods
    suspend fun insertConditionalFormattingRule(rule: ConditionalFormattingRule) {
        withContext(Dispatchers.IO) {
            conditionalFormattingRuleDao.insertRule(rule)
        }
    }

    suspend fun updateConditionalFormattingRule(rule: ConditionalFormattingRule) {
        withContext(Dispatchers.IO) {
            conditionalFormattingRuleDao.updateRule(rule)
        }
    }

    suspend fun deleteConditionalFormattingRule(rule: ConditionalFormattingRule) {
        withContext(Dispatchers.IO) {
            conditionalFormattingRuleDao.deleteRule(rule)
        }
    }

    fun getAllConditionalFormattingRules(): LiveData<List<ConditionalFormattingRule>> {
        return conditionalFormattingRuleDao.getAllRules()
    }
}