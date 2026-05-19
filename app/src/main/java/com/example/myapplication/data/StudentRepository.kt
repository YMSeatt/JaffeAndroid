package com.example.myapplication.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
     *
     * @param studentId The unique ID of the student.
     * @return A LiveData list of [HomeworkLog] entities, ordered by time.
     */
    fun getHomeworkLogsForStudent(studentId: Long): LiveData<List<HomeworkLog>> {
        return homeworkLogDao.getHomeworkLogsForStudent(studentId)
    }

    /**
     * Persists a new homework log entry.
     *
     * @param homeworkLog The log to insert.
     * @return The row ID of the newly inserted log.
     */
    suspend fun insertHomeworkLog(homeworkLog: HomeworkLog): Long {
        return homeworkLogDao.insert(homeworkLog)
    }

    /**
     * Removes a homework log entry by its primary key.
     *
     * @param logId The ID of the log to delete.
     */
    suspend fun deleteHomeworkLogById(logId: Long) {
        homeworkLogDao.deleteHomeworkLog(logId)
    }

    /**
     * Returns a reactive stream of all furniture items currently on the seating chart.
     */
    fun getAllFurniture(): Flow<List<Furniture>> {
        return furnitureDao.getAllFurniture()
    }

    /**
     * Returns a reactive stream of all saved layout templates.
     *
     * Templates allow users to store and restore student and furniture positions.
     */
    fun getAllLayoutTemplates(): Flow<List<LayoutTemplate>> {
        return layoutTemplateDao.getAllLayoutTemplates().asFlow()
    }

    /**
     * Returns a reactive stream of all available quiz mark types.
     *
     * Mark types define how quiz scores are calculated (e.g., "Correct", "Extra Credit").
     */
    fun getAllQuizMarkTypes(): Flow<List<QuizMarkType>> {
        return quizMarkTypeDao.getAllQuizMarkTypes()
    }

    /**
     * Inserts a single [Student] record into the database.
     *
     * @param student The student to persist.
     * @return The row ID of the new student.
     */
    suspend fun insertStudent(student: Student): Long {
        return studentDao.insert(student)
    }

    /**
     * Bulk inserts multiple [Student] records.
     *
     * @param students The list of students to insert.
     * @return A list of the new row IDs.
     */
    suspend fun insertStudents(students: List<Student>): List<Long> {
        return studentDao.insertAll(students)
    }

    /**
     * Updates an existing student's attributes.
     *
     * @param student The student entity with updated fields.
     */
    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    /**
     * Removes a student and all their associated history from the database.
     *
     * @param student The student to delete.
     */
    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    /**
     * Bulk deletes multiple student records.
     *
     * @param students The list of students to delete.
     */
    suspend fun deleteStudents(students: List<Student>) {
        studentDao.deleteAll(students)
    }

    /**
     * Fetches a single student entity by their unique ID.
     *
     * @param studentId The primary key ID of the student.
     * @return The student object, or null if not found.
     */
    suspend fun getStudentById(studentId: Long): Student? {
        return studentDao.getStudentByIdNonLiveData(studentId)
    }

    /**
     * Checks if a student with the given names already exists (case-insensitive).
     *
     * @param firstName The student's first name.
     * @param lastName The student's last name.
     * @return True if a match is found, false otherwise.
     */
    suspend fun studentExists(firstName: String, lastName: String): Boolean {
        return studentDao.studentExists(firstName, lastName)
    }

    /**
     * Persists a new furniture item.
     *
     * @param furniture The furniture entity to insert.
     * @return The row ID of the new item.
     */
    suspend fun insertFurniture(furniture: Furniture): Long {
        return furnitureDao.insert(furniture)
    }

    /**
     * Updates existing furniture attributes (e.g., position, name).
     *
     * @param furniture The updated furniture entity.
     */
    suspend fun updateFurniture(furniture: Furniture) {
        furnitureDao.update(furniture)
    }

    /**
     * Fetches a furniture item by its primary key ID.
     *
     * @param furnitureId The ID of the item.
     * @return The furniture object, or null if not found.
     */
    suspend fun getFurnitureById(furnitureId: Long): Furniture? {
        return furnitureDao.getFurnitureById(furnitureId)
    }

    /**
     * Removes a furniture item from the seating chart.
     *
     * @param furniture The item to delete.
     */
    suspend fun deleteFurniture(furniture: Furniture) {
        furnitureDao.delete(furniture)
    }

    /**
     * Saves a snapshot of the current seating chart as a named template.
     *
     * @param layout The template to save.
     */
    suspend fun insertLayoutTemplate(layout: LayoutTemplate) {
        layoutTemplateDao.insertLayoutTemplate(layout)
    }

    /**
     * Deletes a saved layout template.
     *
     * @param layout The template to remove.
     */
    suspend fun deleteLayoutTemplate(layout: LayoutTemplate) {
        layoutTemplateDao.deleteLayoutTemplate(layout)
    }

    /**
     * Persists a behavioral event (Positive, Negative, or Neutral).
     *
     * @param event The event to log.
     * @return The row ID of the new log.
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
     *
     * @param log The quiz log to insert.
     * @return The row ID of the new log entry.
     */
    suspend fun insertQuizLog(log: QuizLog): Long {
        return quizLogDao.insert(log)
    }

    /**
     * Deletes a quiz result entry.
     *
     * @param log The log entry to remove.
     */
    suspend fun deleteQuizLog(log: QuizLog) {
        quizLogDao.deleteQuizLog(log)
    }

    /**
     * Fetches students for a given set of IDs.
     *
     * @param studentIds The list of IDs to retrieve.
     * @return A list of matching student entities.
     */
    suspend fun getStudentsByIdsList(studentIds: List<Long>): List<Student> {
        return studentDao.getStudentsByIdsList(studentIds)
    }

    /**
     * Fetches all students as a simple list for background processing.
     *
     * Use this when reactive [LiveData] or [Flow] streams are not required.
     */
    suspend fun getAllStudentsNonLiveData(): List<Student> {
        return studentDao.getAllStudentsNonLiveData()
    }

    /**
     * BOLT: Identifies the most recently active student across the entire classroom.
     *
     * This is used by the Ghost Quick Settings tiles to provide rapid access to the
     * last student the teacher interacted with.
     *
     * @return The ID of the last active student, or null if no logs exist.
     */
    suspend fun getLastActiveStudentId(): Long? {
        return behaviorEventDao.getLastBehaviorEvent()?.studentId
    }

    /**
     * BOLT: Returns a reactive stream of the most recently active student ID.
     */
    fun getLastActiveStudentIdFlow(): Flow<Long?> {
        return behaviorEventDao.getLastBehaviorEventFlow().map { it?.studentId }
    }

    /**
     * BOLT: Returns a reactive stream of a single student by their ID.
     */
    fun getStudentByIdFlow(studentId: Long): Flow<Student?> {
        return studentDao.getStudentByIdFlow(studentId)
    }
}
