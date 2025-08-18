package com.example.myapplication.data

import androidx.lifecycle.LiveData
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
        studentDao.getStudentById(id)
    }
    suspend fun insertStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.insert(student)
    }
    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.update(student)
    }
    suspend fun deleteStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.delete(student)
    }
    suspend fun getStudentDetailsForDisplay(studentId: Long): StudentDetailsForDisplay? = withContext(Dispatchers.IO) {
        studentDao.getStudentDetailsForDisplay(studentId)
    }

    // BehaviorEvent methods
    fun getBehaviorEventsForStudent(studentId: Long): LiveData<List<BehaviorEvent>> {
        return behaviorEventDao.getBehaviorEventsForStudent(studentId)
    }
    suspend fun getBehaviorEventById(id: Long): BehaviorEvent? = withContext(Dispatchers.IO) {
        behaviorEventDao.getBehaviorEventById(id)
    }
    suspend fun insertBehaviorEvent(behaviorEvent: BehaviorEvent) = withContext(Dispatchers.IO) {
        behaviorEventDao.insert(behaviorEvent)
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
        homeworkLogDao.insert(homeworkLog)
    }
    suspend fun insertAllHomeworkLogs(homeworkLogs: List<HomeworkLog>) = withContext(Dispatchers.IO) {
        homeworkLogDao.insertAll(homeworkLogs)
    }
    suspend fun updateHomeworkLog(homeworkLog: HomeworkLog) = withContext(Dispatchers.IO) {
        homeworkLogDao.update(homeworkLog)
    }
    suspend fun deleteHomeworkLog(homeworkLog: HomeworkLog) = withContext(Dispatchers.IO) {
        homeworkLogDao.delete(homeworkLog)
    }
    suspend fun deleteHomeworkLogById(id: Long) = withContext(Dispatchers.IO) {
        homeworkLogDao.deleteById(id)
    }

    // Furniture methods
    fun getAllFurniture(): LiveData<List<Furniture>> = furnitureDao.getAllFurniture()
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
        quizLogDao.insert(quizLog)
    }
    suspend fun insertAllQuizLogs(quizLogs: List<QuizLog>) = withContext(Dispatchers.IO) {
        quizLogDao.insertAll(quizLogs)
    }
    suspend fun updateQuizLog(quizLog: QuizLog) = withContext(Dispatchers.IO) {
        quizLogDao.update(quizLog)
    }
    suspend fun deleteQuizLog(quizLog: QuizLog) = withContext(Dispatchers.IO) {
        quizLogDao.delete(quizLog)
    }
    suspend fun deleteQuizLogById(id: Long) = withContext(Dispatchers.IO) {
        quizLogDao.deleteQuizLogById(id)
    }

    // StudentGroup methods
    fun getAllStudentGroups(): LiveData<List<StudentGroup>> = studentGroupDao.getAllStudentGroups()
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
        layoutTemplateDao.insert(layoutTemplate)
    }
    suspend fun updateLayoutTemplate(layoutTemplate: LayoutTemplate) = with-context(Dispatchers.IO) {
        layoutTemplateDao.update(layoutTemplate)
    }
    suspend fun deleteLayoutTemplate(layoutTemplate: LayoutTemplate) = withContext(Dispatchers.IO) {
        layoutTemplateDao.delete(layoutTemplate)
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
        conditionalFormattingRuleDao.insert(rule)
    }
    suspend fun updateConditionalFormattingRule(rule: ConditionalFormattingRule) = withContext(Dispatchers.IO) {
        conditionalFormattingRuleDao.update(rule)
    }
    suspend fun deleteConditionalFormattingRule(rule: ConditionalFormattingRule) = withContext(Dispatchers.IO) {
        conditionalFormattingRuleDao.delete(rule)
    }
    suspend fun deleteConditionalFormattingRuleById(id: Long) = withContext(Dispatchers.IO) {
        conditionalFormattingRuleDao.deleteRuleById(id)
    }

    // QuizMarkType methods
    fun getAllQuizMarkTypes(): LiveData<List<QuizMarkType>> = quizMarkTypeDao.getAllQuizMarkTypes()
    suspend fun getQuizMarkTypeById(id: Long): QuizMarkType? = withContext(Dispatchers.IO) {
        quizMarkTypeDao.getQuizMarkTypeById(id)
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
