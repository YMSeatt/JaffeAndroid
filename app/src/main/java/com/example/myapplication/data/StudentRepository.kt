package com.example.myapplication.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.Flow

/**
 * StudentRepository: The primary data interface for the application.
 *
 * This repository coordinates multiple Data Access Objects (DAOs) to provide a unified
 * API for managing classroom data. It serves as the single source of truth for the ViewModels,
 * abstracting away the complexity of whether data is retrieved via reactive streams (LiveData/Flow)
 * or one-shot asynchronous operations (suspend functions).
 *
 * ### Responsibilities:
 * 1. **Data Coordination**: Orchestrates operations across [StudentDao], [BehaviorEventDao],
 *    [HomeworkLogDao], [QuizLogDao], [FurnitureDao], and [LayoutTemplateDao].
 * 2. **Performance Optimization**: Implements "BOLT" optimizations by pushing filtering logic
 *    (e.g., date ranges and student ID subsets) directly to the SQLite layer, significantly
 *    reducing in-memory processing and GC pressure.
 * 3. **Reactive state**: Provides LiveData and Flow streams for UI components to observe
 *    real-time database changes.
 */
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
    /** Reactive stream of all students currently in the database. */
    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()

    /**
     * Retrieves all homework logs for a specific student as a reactive [LiveData] stream.
     * @param studentId The unique ID of the student.
     */
    fun getHomeworkLogsForStudent(studentId: Long): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getHomeworkLogsForStudent(studentId)
    }

    /**
     * Persists a new homework log entry.
     * @return The row ID of the newly inserted log.
     */
    suspend fun insertHomeworkLog(homeworkLog: HomeworkLog): Long {
        return homeworkLogDao.insert(homeworkLog)
    }

    /**
     * Removes a homework log entry by its primary key.
     */
    suspend fun deleteHomeworkLogById(logId: Long) {
        homeworkLogDao.deleteHomeworkLog(logId)
    }

    /**
     * Returns a reactive stream of all furniture items on the seating chart.
     */
    fun getAllFurniture(): Flow<List<Furniture>> {
        return furnitureDao.getAllFurniture()
    }

    /**
     * Returns a reactive stream of all saved layout templates.
     */
    fun getAllLayoutTemplates(): Flow<List<LayoutTemplate>> {
        return layoutTemplateDao.getAllLayoutTemplates().asFlow()
    }

    /**
     * Returns a reactive stream of all available quiz mark types.
     */
    fun getAllQuizMarkTypes(): Flow<List<QuizMarkType>> {
        return quizMarkTypeDao.getAllQuizMarkTypes()
    }

    /**
     * Inserts a single [Student] record into the database.
     */
    suspend fun insertStudent(student: Student): Long {
        return studentDao.insert(student)
    }

    /**
     * Bulk inserts multiple [Student] records.
     * @return A list of the new row IDs.
     */
    suspend fun insertStudents(students: List<Student>): List<Long> {
        return studentDao.insertAll(students)
    }

    /**
     * Updates an existing student's data.
     */
    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    /**
     * Removes a student from the database.
     */
    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    /**
     * Bulk deletes multiple student records.
     */
    suspend fun deleteStudents(students: List<Student>) {
        studentDao.deleteAll(students)
    }

    /**
     * Fetches a single student entity by their unique ID.
     * @return The student object, or null if not found.
     */
    suspend fun getStudentById(studentId: Long): Student? {
        return studentDao.getStudentByIdNonLiveData(studentId)
    }

    /**
     * Checks if a student with the given names already exists (case-insensitive).
     */
    suspend fun studentExists(firstName: String, lastName: String): Boolean {
        return studentDao.studentExists(firstName, lastName)
    }

    /**
     * Persists a new furniture item.
     */
    suspend fun insertFurniture(furniture: Furniture): Long {
        return furnitureDao.insert(furniture)
    }

    /**
     * Updates existing furniture attributes.
     */
    suspend fun updateFurniture(furniture: Furniture) {
        furnitureDao.update(furniture)
    }

    /**
     * Fetches a furniture item by its ID.
     */
    suspend fun getFurnitureById(furnitureId: Long): Furniture? {
        return furnitureDao.getFurnitureById(furnitureId)
    }

    /**
     * Removes a furniture item from the seating chart.
     */
    suspend fun deleteFurniture(furniture: Furniture) {
        furnitureDao.delete(furniture)
    }

    /**
     * Saves a snapshot of the current seating chart as a template.
     */
    suspend fun insertLayoutTemplate(layout: LayoutTemplate) {
        layoutTemplateDao.insertLayoutTemplate(layout)
    }

    /**
     * Deletes a saved layout template.
     */
    suspend fun deleteLayoutTemplate(layout: LayoutTemplate) {
        layoutTemplateDao.deleteLayoutTemplate(layout)
    }

    /**
     * Persists a behavioral event (Positive/Negative/Neutral).
     */
    suspend fun insertBehaviorEvent(event: BehaviorEvent): Long {
        return behaviorEventDao.insert(event)
    }

    /**
     * BOLT: Centralized filtered fetch logic to handle the distinction between
     * null studentIds (all students) and empty studentIds (no students).
     *
     * This method pushes the filtering logic to the SQLite layer to avoid
     * loading the entire student table into memory for a partial subset.
     *
     * @param studentIds List of IDs to include. If null, returns all students.
     * @return List of matching students.
     */
    suspend fun getFilteredStudents(studentIds: List<Long>?): List<Student> {
        return when {
            studentIds == null -> studentDao.getAllStudentsNonLiveData()
            studentIds.isEmpty() -> emptyList()
            else -> studentDao.getStudentsByIdsList(studentIds)
        }
    }

    /**
     * BOLT: Optimized fetch for behavior events within a specific timeframe.
     *
     * @param startDate Start timestamp in epoch milliseconds.
     * @param endDate End timestamp in epoch milliseconds.
     * @param studentIds Optional filter for specific students. If null, returns events for all students.
     * @return Sorted list of matching behavior events.
     */
    suspend fun getFilteredBehaviorEvents(startDate: Long, endDate: Long, studentIds: List<Long>? = null): List<BehaviorEvent> {
        return when {
            studentIds == null -> behaviorEventDao.getFilteredBehaviorEvents(startDate, endDate)
            studentIds.isEmpty() -> emptyList()
            else -> behaviorEventDao.getFilteredBehaviorEventsWithStudents(startDate, endDate, studentIds)
        }
    }

    /**
     * BOLT: Optimized fetch for homework logs within a specific timeframe.
     *
     * @param startDate Start timestamp.
     * @param endDate End timestamp.
     * @param studentIds Optional filter for specific students.
     * @return List of matching homework logs.
     */
    suspend fun getFilteredHomeworkLogs(startDate: Long, endDate: Long, studentIds: List<Long>? = null): List<HomeworkLog> {
        return when {
            studentIds == null -> homeworkLogDao.getFilteredHomeworkLogs(startDate, endDate)
            studentIds.isEmpty() -> emptyList()
            else -> homeworkLogDao.getFilteredHomeworkLogsWithStudents(startDate, endDate, studentIds)
        }
    }

    /**
     * BOLT: Optimized fetch for quiz logs within a specific timeframe.
     *
     * @param startDate Start timestamp.
     * @param endDate End timestamp.
     * @param studentIds Optional filter for specific students.
     * @return List of matching quiz logs.
     */
    suspend fun getFilteredQuizLogs(startDate: Long, endDate: Long, studentIds: List<Long>? = null): List<QuizLog> {
        return when {
            studentIds == null -> quizLogDao.getFilteredQuizLogs(startDate, endDate)
            studentIds.isEmpty() -> emptyList()
            else -> quizLogDao.getFilteredQuizLogsWithStudents(startDate, endDate, studentIds)
        }
    }

    /**
     * Persists a quiz result.
     */
    suspend fun insertQuizLog(log: QuizLog): Long {
        return quizLogDao.insert(log)
    }

    /**
     * Deletes a quiz result entry.
     */
    suspend fun deleteQuizLog(log: QuizLog) {
        quizLogDao.deleteQuizLog(log)
    }

    /**
     * Fetches students for a given set of IDs.
     */
    suspend fun getStudentsByIdsList(studentIds: List<Long>): List<Student> {
        return studentDao.getStudentsByIdsList(studentIds)
    }

    /**
     * Fetches all students as a simple list for background processing.
     */
    suspend fun getAllStudentsNonLiveData(): List<Student> {
        return studentDao.getAllStudentsNonLiveData()
    }
}
