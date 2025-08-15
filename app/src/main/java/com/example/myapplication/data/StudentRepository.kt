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
    private val conditionalFormattingRuleDao: ConditionalFormattingRuleDao
) {

    val studentsForDisplay: LiveData<List<StudentDetailsForDisplay>> = studentDao.getStudentsForDisplay()
    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()
    val allFurniture: LiveData<List<Furniture>> = furnitureDao.getAllFurniture()

    // Student methods
    suspend fun getStudentByIdNonLiveData(studentId: Int): Student? {
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

    fun getStudentsByGroupId(groupId: Int): LiveData<List<Student>> {
        return studentDao.getStudentsByGroupId(groupId)
    }

    // BehaviorEvent methods
    fun getAllBehaviorEvents(): LiveData<List<BehaviorEvent>> = behaviorEventDao.getAllBehaviorEvents()

    fun getRecentBehaviorEventsForStudent(studentId: Int, limit: Int): LiveData<List<BehaviorEvent>> {
        return behaviorEventDao.getRecentBehaviorEventsForStudent(studentId, limit)
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

    fun getHomeworkLogsForStudent(studentId: Int): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getHomeworkLogsForStudent(studentId)
    }

    fun getAllHomeworkLogs(): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getAllHomeworkLogs()
    }

    suspend fun deleteHomeworkLog(logId: Int) {
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
            quizLogDao.insert(quizLog)
        }
    }

    fun getQuizLogsForStudent(studentId: Int): LiveData<List<QuizLog>> {
        return quizLogDao.getQuizLogsForStudent(studentId)
    }

    // StudentGroup methods
    val allGroups: LiveData<List<StudentGroup>> = studentGroupDao.getAllGroups()

    suspend fun insertGroup(group: StudentGroup) {
        withContext(Dispatchers.IO) {
            studentGroupDao.insert(group)
        }
    }

    suspend fun updateGroup(group: StudentGroup) {
        withContext(Dispatchers.IO) {
            studentGroupDao.update(group)
        }
    }

    suspend fun deleteGroup(group: StudentGroup) {
        withContext(Dispatchers.IO) {
            studentGroupDao.delete(group)
        }
    }

    // LayoutTemplate methods
    val allLayoutTemplates: LiveData<List<LayoutTemplate>> = layoutTemplateDao.getAllTemplates()

    suspend fun insertLayoutTemplate(template: LayoutTemplate) {
        withContext(Dispatchers.IO) {
            layoutTemplateDao.insert(template)
        }
    }

    suspend fun deleteLayoutTemplate(template: LayoutTemplate) {
        withContext(Dispatchers.IO) {
            layoutTemplateDao.delete(template)
        }
    }

    // ConditionalFormattingRule methods
    val allRules: LiveData<List<ConditionalFormattingRule>> = conditionalFormattingRuleDao.getAllRules()

    suspend fun insertRule(rule: ConditionalFormattingRule) {
        withContext(Dispatchers.IO) {
            conditionalFormattingRuleDao.insert(rule)
        }
    }

    suspend fun updateRule(rule: ConditionalFormattingRule) {
        withContext(Dispatchers.IO) {
            conditionalFormattingRuleDao.update(rule)
        }
    }

    suspend fun deleteRule(rule: ConditionalFormattingRule) {
        withContext(Dispatchers.IO) {
            conditionalFormattingRuleDao.delete(rule)
        }
    }
}
