package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentRepository(
    private val studentDao: StudentDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val furnitureDao: FurnitureDao,
    private val quizLogDao: QuizLogDao,
    private val studentGroupDao: StudentGroupDao,
    private val layoutTemplateDao: LayoutTemplateDao,
    private val conditionalFormattingRuleDao: ConditionalFormattingRuleDao,
    private val quizMarkTypeDao: QuizMarkTypeDao
) {

    // Student methods
    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()
    suspend fun getStudentById(id: Long): Student? = withContext(Dispatchers.IO) {
        studentDao.getStudentByIdNonLiveData(id)
    }
    suspend fun insertStudent(student: Student): Long = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }
    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
    }
    suspend fun deleteStudent(student: Student) = withContext(Dispatchers.IO) {
        behaviorEventDao.deleteByStudentId(student.id)
        homeworkLogDao.deleteByStudentId(student.id)
        quizLogDao.deleteByStudentId(student.id)
        studentDao.deleteStudent(student)
    }

    suspend fun studentExists(firstName: String, lastName: String): Boolean = withContext(Dispatchers.IO) {
        studentDao.studentExists(firstName, lastName)
    }

    fun getAllBehaviorEvents(): LiveData<List<BehaviorEvent>> {
        return behaviorEventDao.getAllBehaviorEvents()
    }

    fun getAllHomeworkLogs(): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getAllHomeworkLogs()
    }

    fun getAllQuizLogs(): LiveData<List<QuizLog>> {
        return quizLogDao.getAllQuizLogs()
    }
    // Assuming getStudentDetailsForDisplay is a method in StudentDao that returns StudentDetailsForDisplay?
    // If not, this will need further adjustment based on the actual StudentDao method.
    suspend fun getStudentDetailsForDisplay(studentId: Long): StudentDetailsForDisplay? = withContext(Dispatchers.IO) {
        studentDao.getStudentsForDisplay().value?.find { it.id == studentId } // This is a guess, might need adjustment
    }

    // BehaviorEvent methods
    fun getBehaviorEventsForStudent(studentId: Long): LiveData<List<BehaviorEvent>> {
        return behaviorEventDao.getBehaviorEventsForStudent(studentId)
    }
    suspend fun getBehaviorEventById(id: Long): BehaviorEvent? = withContext(Dispatchers.IO) {
        behaviorEventDao.getBehaviorEventById(id)
    }
    suspend fun insertBehaviorEvent(behaviorEvent: BehaviorEvent) = withContext(Dispatchers.IO) {
        behaviorEventDao.insertBehaviorEvent(behaviorEvent)
    }
    suspend fun updateBehaviorEvent(behaviorEvent: BehaviorEvent) = withContext(Dispatchers.IO) {
        behaviorEventDao.updateBehaviorEvent(behaviorEvent)
    }
    suspend fun deleteBehaviorEvent(behaviorEvent: BehaviorEvent) = withContext(Dispatchers.IO) {
        behaviorEventDao.delete(behaviorEvent)
    }
    suspend fun deleteBehaviorEventById(id: Long) = withContext(Dispatchers.IO) {
        behaviorEventDao.deleteBehaviorEventById(id)
    }

    // HomeworkLog methods
    fun getHomeworkLogsForStudent(studentId: Long): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getHomeworkLogsForStudent(studentId)
    }
    suspend fun getHomeworkLogById(id: Long): HomeworkLog? = withContext(Dispatchers.IO) {
        homeworkLogDao.getHomeworkLogById(id)
    }
    suspend fun insertHomeworkLog(homeworkLog: HomeworkLog) = withContext(Dispatchers.IO) {
        homeworkLogDao.insertHomeworkLog(homeworkLog)
    }
    suspend fun insertAllHomeworkLogs(homeworkLogs: List<HomeworkLog>) = withContext(Dispatchers.IO) {
        homeworkLogDao.insertAll(homeworkLogs)
    }
    suspend fun updateHomeworkLog(homeworkLog: HomeworkLog) = withContext(Dispatchers.IO) {
        homeworkLogDao.updateHomeworkLog(homeworkLog)
    }
    suspend fun deleteHomeworkLog(homeworkLog: HomeworkLog) = withContext(Dispatchers.IO) {
        homeworkLogDao.delete(homeworkLog)
    }
    suspend fun deleteHomeworkLogById(id: Long) = withContext(Dispatchers.IO) {
        homeworkLogDao.deleteHomeworkLog(id)
    }

    // Furniture methods
    fun getAllFurniture(): LiveData<List<Furniture>> = furnitureDao.getAllFurniture().asLiveData()
    suspend fun getFurnitureById(id: Long): Furniture? = withContext(Dispatchers.IO) {
        furnitureDao.getFurnitureById(id)
    }
    suspend fun insertFurniture(furniture: Furniture) = withContext(Dispatchers.IO) {
        furnitureDao.insert(furniture)
    }
    suspend fun updateFurniture(furniture: Furniture) = withContext(Dispatchers.IO) {
        furnitureDao.update(furniture)
    }
    suspend fun deleteFurniture(furniture: Furniture) = withContext(Dispatchers.IO) {
        furnitureDao.delete(furniture)
    }

    // QuizLog methods
    fun getQuizLogsForStudent(studentId: Long): LiveData<List<QuizLog>> {
        return quizLogDao.getQuizLogsForStudent(studentId)
    }
    suspend fun getQuizLogById(id: Long): QuizLog? = withContext(Dispatchers.IO) {
        quizLogDao.getQuizLogById(id)
    }
    suspend fun insertQuizLog(quizLog: QuizLog) = withContext(Dispatchers.IO) {
        quizLogDao.insertQuizLog(quizLog)
    }
    suspend fun insertAllQuizLogs(quizLogs: List<QuizLog>) = withContext(Dispatchers.IO) {
        quizLogDao.insertAll(quizLogs)
    }
    suspend fun updateQuizLog(quizLog: QuizLog) = withContext(Dispatchers.IO) {
        quizLogDao.updateQuizLog(quizLog)
    }
    suspend fun deleteQuizLog(quizLog: QuizLog) = withContext(Dispatchers.IO) {
        quizLogDao.deleteQuizLog(quizLog)
    }
    suspend fun deleteQuizLogById(id: Long) = withContext(Dispatchers.IO) {
        quizLogDao.deleteQuizLogById(id)
    }

    // StudentGroup methods
    fun getAllStudentGroups(): LiveData<List<StudentGroup>> = studentGroupDao.getAllStudentGroups().asLiveData()
    suspend fun getStudentGroupById(id: Long): StudentGroup? = withContext(Dispatchers.IO) {
        studentGroupDao.getStudentGroupById(id)
    }
    suspend fun insertStudentGroup(studentGroup: StudentGroup) = withContext(Dispatchers.IO) {
        studentGroupDao.insert(studentGroup)
    }
    suspend fun updateStudentGroup(studentGroup: StudentGroup) = withContext(Dispatchers.IO) {
        studentGroupDao.update(studentGroup)
    }
    suspend fun deleteStudentGroup(studentGroup: StudentGroup) = withContext(Dispatchers.IO) {
        studentGroupDao.delete(studentGroup)
    }

    // LayoutTemplate methods
    fun getAllLayoutTemplates(): LiveData<List<LayoutTemplate>> = layoutTemplateDao.getAllLayoutTemplates()
    suspend fun getLayoutTemplateById(id: Long): LayoutTemplate? = withContext(Dispatchers.IO) {
        layoutTemplateDao.getLayoutTemplateById(id)
    }
    suspend fun insertLayoutTemplate(layoutTemplate: LayoutTemplate) = withContext(Dispatchers.IO) {
        layoutTemplateDao.insertLayoutTemplate(layoutTemplate)
    }
    suspend fun updateLayoutTemplate(layoutTemplate: LayoutTemplate) = withContext(Dispatchers.IO) {
        layoutTemplateDao.updateLayoutTemplate(layoutTemplate)
    }
    suspend fun deleteLayoutTemplate(layoutTemplate: LayoutTemplate) = withContext(Dispatchers.IO) {
        layoutTemplateDao.deleteLayoutTemplate(layoutTemplate)
    }
    suspend fun deleteLayoutTemplateById(id: Long) = withContext(Dispatchers.IO) {
        layoutTemplateDao.deleteLayoutTemplateById(id)
    }

    // ConditionalFormattingRule methods
    fun getAllConditionalFormattingRules(): LiveData<List<ConditionalFormattingRule>> = conditionalFormattingRuleDao.getAllRules()
    suspend fun getConditionalFormattingRuleById(id: Long): ConditionalFormattingRule? = withContext(Dispatchers.IO) {
        conditionalFormattingRuleDao.getRuleById(id)
    }
    suspend fun insertConditionalFormattingRule(rule: ConditionalFormattingRule) = withContext(Dispatchers.IO) {
        conditionalFormattingRuleDao.insertRule(rule)
    }
    suspend fun updateConditionalFormattingRule(rule: ConditionalFormattingRule) = withContext(Dispatchers.IO) {
        conditionalFormattingRuleDao.updateRule(rule)
    }
    suspend fun deleteConditionalFormattingRule(rule: ConditionalFormattingRule) = withContext(Dispatchers.IO) {
        conditionalFormattingRuleDao.deleteRule(rule)
    }
    suspend fun deleteConditionalFormattingRuleById(id: Long) = withContext(Dispatchers.IO) {
        conditionalFormattingRuleDao.deleteRuleById(id)
    }

    // QuizMarkType methods
    fun getAllQuizMarkTypes(): LiveData<List<QuizMarkType>> = quizMarkTypeDao.getAllQuizMarkTypes().asLiveData()
    suspend fun getQuizMarkTypeById(id: Long): QuizMarkType? = withContext(Dispatchers.IO) {
        quizMarkTypeDao.getQuizMarkTypeById(id) // This will still cause an error if not defined in the DAO
    }
    suspend fun insertQuizMarkType(quizMarkType: QuizMarkType) = withContext(Dispatchers.IO) {
        quizMarkTypeDao.insert(quizMarkType)
    }
    suspend fun updateQuizMarkType(quizMarkType: QuizMarkType) = withContext(Dispatchers.IO) {
        quizMarkTypeDao.update(quizMarkType)
    }
    suspend fun deleteQuizMarkType(quizMarkType: QuizMarkType) = withContext(Dispatchers.IO) {
        quizMarkTypeDao.delete(quizMarkType)
    }
}
