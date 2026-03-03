package com.example.myapplication.viewmodel

import android.util.LruCache
import androidx.lifecycle.*
import com.example.myapplication.data.*
import com.example.myapplication.data.exporter.ExportOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Summary of a specific behavior type for a student.
 * @property studentName Full name of the student.
 * @property behavior The name of the behavior category.
 * @property count Total occurrences within the specified timeframe.
 */
data class BehaviorSummary(val studentName: String, val behavior: String, val count: Int)

/**
 * Aggregated academic performance for a student on a specific quiz.
 * @property studentName Full name of the student.
 * @property quizName The name of the quiz assignment.
 * @property averageScore The mean percentage score across all attempts.
 * @property timesTaken The number of times the student has attempted this quiz.
 */
data class QuizSummary(val studentName: String, val quizName: String, val averageScore: Double, val timesTaken: Int)

/**
 * Summary of homework completion and achievement for a student.
 * @property studentName Full name of the student.
 * @property assignmentName The name of the homework assignment or category.
 * @property count The number of recorded entries for this assignment.
 * @property totalPoints The sum of points earned across all entries.
 */
data class HomeworkSummary(val studentName: String, val assignmentName: String, val count: Int, val totalPoints: Double)

/**
 * Presence and absence statistics for a student based on logged classroom activity.
 * @property studentName Full name of the student.
 * @property daysPresent Number of distinct days where any activity was logged.
 * @property daysAbsent Number of days in the reporting range where no activity was detected.
 * @property attendancePercentage Ratio of presence to the total reporting window.
 */
data class AttendanceSummary(val studentName: String, val daysPresent: Int, val daysAbsent: Int, val attendancePercentage: Double)

/**
 * Consolidated statistics data to reduce UI recomposition triggers.
 *
 * By aggregating multiple summaries into a single immutable data class, the UI layer
 * can observe a unified state, preventing fragmented recompositions as individual
 * metrics are calculated.
 *
 * @property behaviorSummary List of behavior metrics.
 * @property quizSummary List of academic quiz metrics.
 * @property homeworkSummary List of homework completion metrics.
 * @property attendanceSummary List of presence metrics.
 * @property totalDaysInRange The total number of school days covered by this report.
 */
data class StatsData(
    val behaviorSummary: List<BehaviorSummary> = emptyList(),
    val quizSummary: List<QuizSummary> = emptyList(),
    val homeworkSummary: List<HomeworkSummary> = emptyList(),
    val attendanceSummary: List<AttendanceSummary> = emptyList(),
    val totalDaysInRange: Int = 0
)

/**
 * StatsViewModel: The primary data aggregation hub for classroom analytics.
 *
 * This ViewModel coordinates the extraction and synthesis of raw log data (Behavior, Quiz, Homework)
 * into high-level summaries used by the "Stats" and "Data Viewer" screens. It is designed to
 * provide a unified, filtered view of classroom trends over custom timeframes.
 *
 * ### Performance Architecture:
 * To ensure smooth UI performance even with large historical datasets, this ViewModel
 * utilizes several "BOLT" optimizations:
 * 1. **Background Synthesis**: All complex calculations are performed on [Dispatchers.Default]
 *    to avoid blocking the main thread.
 * 2. **LruCache Memoization**: Uses [LruCache] to store the results of expensive JSON-based
 *    mark data parsing ([QuizLog.marksData]), avoiding redundant deserialization in loops.
 * 3. **Single-Pass Aggregation**: Calculation algorithms are optimized to minimize list
 *    allocations and O(N^2) operations by using single-pass grouping and primitive arrays.
 * 4. **Epoch-Day Indexing**: Attendance logic uses epoch-day calculations for O(1) presence
 *    checks instead of expensive Calendar object churn.
 */
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val studentDao: StudentDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val quizLogDao: QuizLogDao,
    private val quizMarkTypeDao: QuizMarkTypeDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val customHomeworkTypeDao: CustomHomeworkTypeDao
) : ViewModel() {

    private val _statsData = MutableLiveData<StatsData>(StatsData())
    val statsData: LiveData<StatsData> = _statsData

    /** Legacy LiveData for backward compatibility or simple observers. */
    val behaviorSummary: LiveData<List<BehaviorSummary>> = _statsData.map { it.behaviorSummary }
    val quizSummary: LiveData<List<QuizSummary>> = _statsData.map { it.quizSummary }
    val homeworkSummary: LiveData<List<HomeworkSummary>> = _statsData.map { it.homeworkSummary }

    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()
    val allCustomBehaviors: LiveData<List<CustomBehavior>> = customBehaviorDao.getAllCustomBehaviors()
    val allCustomHomeworkTypes: LiveData<List<CustomHomeworkType>> = customHomeworkTypeDao.getAllCustomHomeworkTypes()


    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Cache for decoded quiz marks data to avoid redundant JSON parsing during aggregation.
     * The key is the raw JSON string from [QuizLog.marksData].
     */
    private val decodedMarksCache = LruCache<String, Map<String, Int>>(1000)

    /**
     * Cache for decoded homework marks data.
     */
    private val decodedHomeworkMarksCache = LruCache<String, Map<String, String>>(1000)

    init {
        // Load initial data with default options
        updateStats(ExportOptions())
    }

    /**
     * Triggers a refresh of classroom statistics based on the provided [ExportOptions].
     *
     * This method orchestrates the full pipeline:
     * 1. Fetches raw data from DAOs (filtered by date and student IDs at the SQL level).
     * 2. Performs in-memory filtering for specific behavior/homework types.
     * 3. Dispatches parallel synthesis tasks for behavior, quiz, homework, and attendance.
     * 4. Posts a consolidated [StatsData] update to the UI.
     *
     * @param options Configuration for filtering (date range, selected students, log types).
     */
    fun updateStats(options: ExportOptions) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val startDate = options.startDate ?: 0L
                val endDate = options.endDate ?: Long.MAX_VALUE
                val studentIds = options.studentIds

                val students = if (studentIds != null && studentIds.isNotEmpty()) {
                    studentDao.getStudentsByIdsList(studentIds)
                } else {
                    studentDao.getAllStudentsNonLiveData()
                }

                val behaviorEvents = if (studentIds != null && studentIds.isNotEmpty()) {
                    behaviorEventDao.getFilteredBehaviorEventsWithStudents(startDate, endDate, studentIds)
                } else {
                    behaviorEventDao.getFilteredBehaviorEvents(startDate, endDate)
                }

                val homeworkLogs = if (studentIds != null && studentIds.isNotEmpty()) {
                    homeworkLogDao.getFilteredHomeworkLogsWithStudents(startDate, endDate, studentIds)
                } else {
                    homeworkLogDao.getFilteredHomeworkLogs(startDate, endDate)
                }

                val quizLogs = if (studentIds != null && studentIds.isNotEmpty()) {
                    quizLogDao.getFilteredQuizLogsWithStudents(startDate, endDate, studentIds)
                } else {
                    quizLogDao.getFilteredQuizLogs(startDate, endDate)
                }

                val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()

                // Filter data based on remaining options (types)
                val behaviorTypesSet = options.behaviorTypes?.toSet()
                val homeworkTypesSet = options.homeworkTypes?.toSet()

                val filteredBehaviorEvents = if (behaviorTypesSet == null) {
                    behaviorEvents
                } else {
                    behaviorEvents.filter { behaviorTypesSet.contains(it.type) }
                }

                val filteredHomeworkLogs = if (homeworkTypesSet == null) {
                    homeworkLogs
                } else {
                    homeworkLogs.filter { homeworkTypesSet.contains(it.assignmentName) }
                }

                val filteredQuizLogs = quizLogs

                val studentMap = students.associateBy { it.id }

                // Calculate summaries
                val behaviorSummaryList = calculateBehaviorSummary(filteredBehaviorEvents, studentMap)
                val quizSummaryList = calculateQuizSummary(filteredQuizLogs, studentMap, quizMarkTypes)
                val homeworkSummaryList = calculateHomeworkSummary(filteredHomeworkLogs, studentMap)
                val (attendanceSummaryList, totalDays) = calculateAttendanceSummary(options, students, filteredBehaviorEvents, filteredHomeworkLogs, filteredQuizLogs)

                _statsData.postValue(
                    StatsData(
                        behaviorSummary = behaviorSummaryList,
                        quizSummary = quizSummaryList,
                        homeworkSummary = homeworkSummaryList,
                        attendanceSummary = attendanceSummaryList,
                        totalDaysInRange = totalDays
                    )
                )
            }
        }
    }

    /**
     * Synthesizes attendance statistics based on student activity logs.
     *
     * **BOLT Optimization**: Instead of nested O(S*D) loops, this method uses an O(S + activity_count)
     * strategy. It maps active days to epoch-day Longs, allowing for O(1) presence verification
     * within the reporting window.
     *
     * @param options Current filtering options to determine the date range.
     * @param students List of students to include in the report.
     * @param behaviorEvents Contextual behavior logs.
     * @param homeworkLogs Contextual homework logs.
     * @param quizLogs Contextual quiz logs.
     * @return A pair containing the list of [AttendanceSummary] objects and the total days in the range.
     */
    internal fun calculateAttendanceSummary(
        options: ExportOptions,
        students: List<Student>,
        behaviorEvents: List<BehaviorEvent>,
        homeworkLogs: List<HomeworkLog>,
        quizLogs: List<QuizLog>
    ): Pair<List<AttendanceSummary>, Int> {
        val zoneId = ZoneId.systemDefault()

        // Determine date range from options or fallback to log boundaries
        val startMillis = options.startDate ?: listOfNotNull(
            behaviorEvents.minOfOrNull { it.timestamp },
            homeworkLogs.minOfOrNull { it.loggedAt },
            quizLogs.minOfOrNull { it.loggedAt }
        ).minOrNull() ?: System.currentTimeMillis()
        val endMillis = options.endDate ?: System.currentTimeMillis()

        val reportStartDay = Instant.ofEpochMilli(startMillis).atZone(zoneId).toLocalDate().toEpochDay()
        val reportEndDay = Instant.ofEpochMilli(endMillis).atZone(zoneId).toLocalDate().toEpochDay()

        if (reportEndDay < reportStartDay) return Pair(emptyList(), 0)

        // BOLT: Optimize by using epoch days for O(1) presence checks and avoiding Calendar
        val totalDaysInRange = (reportEndDay - reportStartDay + 1).toInt().coerceAtMost(366)
        val lastReportDay = reportStartDay + totalDaysInRange - 1

        // Track active days per student (optimized for O(1) presence checks using epoch day)
        val studentActiveDays = mutableMapOf<Long, MutableSet<Long>>()
        behaviorEvents.forEach { event ->
            studentActiveDays.getOrPut(event.studentId) { mutableSetOf() }.add(
                Instant.ofEpochMilli(event.timestamp).atZone(zoneId).toLocalDate().toEpochDay()
            )
        }
        homeworkLogs.forEach { log ->
            studentActiveDays.getOrPut(log.studentId) { mutableSetOf() }.add(
                Instant.ofEpochMilli(log.loggedAt).atZone(zoneId).toLocalDate().toEpochDay()
            )
        }
        quizLogs.forEach { log ->
            studentActiveDays.getOrPut(log.studentId) { mutableSetOf() }.add(
                Instant.ofEpochMilli(log.loggedAt).atZone(zoneId).toLocalDate().toEpochDay()
            )
        }

        // BOLT: Avoid string concatenation in sorting
        val filteredStudents = students.sortedWith(compareBy({ it.lastName }, { it.firstName }))

        val summaryList = mutableListOf<AttendanceSummary>()
        for (student in filteredStudents) {
            val presentDaysSet = studentActiveDays[student.id] ?: emptySet()

            // BOLT: Optimize nested loop by counting active days within the report range
            // This replaces the O(S*D) loop with O(S + active_days), which is much faster.
            val presentCount = presentDaysSet.count { it in reportStartDay..lastReportDay }

            val absentCount = totalDaysInRange - presentCount
            val percentage = if (totalDaysInRange > 0) (presentCount.toDouble() / totalDaysInRange) * 100 else 0.0
            summaryList.add(
                AttendanceSummary(
                    studentName = "${student.firstName} ${student.lastName}",
                    daysPresent = presentCount,
                    daysAbsent = absentCount,
                    attendancePercentage = percentage
                )
            )
        }

        return Pair(summaryList, totalDaysInRange)
    }

    /**
     * Aggregates behavior incident counts per student and type.
     *
     * **BOLT Optimization**: Performs a single-pass iteration over the [behaviorEvents] list
     * to populate a nested mapping, avoiding multiple filter/count cycles.
     *
     * @param behaviorEvents The filtered list of behavior incidents.
     * @param students Map for resolving student IDs to display names.
     * @return A sorted list of [BehaviorSummary] objects.
     */
    internal fun calculateBehaviorSummary(behaviorEvents: List<BehaviorEvent>, students: Map<Long, Student>): List<BehaviorSummary> {
        // Optimization: Single-pass iteration to count behaviors per student without creating intermediate lists
        val behaviorCounts = mutableMapOf<Long, MutableMap<String, Int>>()
        for (event in behaviorEvents) {
            val studentBehaviors = behaviorCounts.getOrPut(event.studentId) { mutableMapOf() }
            studentBehaviors[event.type] = (studentBehaviors[event.type] ?: 0) + 1
        }

        val summaryList = mutableListOf<BehaviorSummary>()
        behaviorCounts.keys.sortedWith(compareBy { students[it]?.lastName }).forEach { studentId ->
            val studentBehaviors = behaviorCounts[studentId]
            studentBehaviors?.keys?.sorted()?.forEach { behavior ->
                val student = students[studentId]
                summaryList.add(
                    BehaviorSummary(
                        studentName = if(student != null) "${student.firstName} ${student.lastName}" else "Unknown",
                        behavior = behavior,
                        count = studentBehaviors[behavior] ?: 0
                    )
                )
            }
        }
        return summaryList
    }

    /**
     * Aggregates quiz performance data, calculating average scores per student.
     *
     * **BOLT Optimization**: Uses [decodedMarksCache] to minimize JSON overhead and utilizes
     * [DoubleArray] for efficient in-memory accumulation (sum and count) during averaging.
     *
     * @param quizLogs The filtered list of quiz logs.
     * @param students Student lookup map.
     * @param quizMarkTypes Configuration for mark point values.
     * @return A sorted list of [QuizSummary] objects.
     */
    internal fun calculateQuizSummary(quizLogs: List<QuizLog>, students: Map<Long, Student>, quizMarkTypes: List<QuizMarkType>): List<QuizSummary> {
        // Optimization: Use a sum/count Pair (via custom class or primitive arrays to avoid boxing) for averaging
        val quizScores = mutableMapOf<Long, MutableMap<String, DoubleArray>>()

        // --- Optimization: Pre-calculate mark type lookups and points ---
        val markTypeMapByName = quizMarkTypes.associateBy { it.name }
        val markTypeMapById = quizMarkTypes.associateBy { it.id.toString() }
        val sumDefaultPointsContributing = quizMarkTypes.filter { it.contributesToTotal }.sumOf { it.defaultPoints }

        for (log in quizLogs) {
            val studentScores = quizScores.getOrPut(log.studentId) { mutableMapOf() }
            val stats = studentScores.getOrPut(log.quizName) { DoubleArray(2) } // [0] = sum, [1] = count

            // Optimization: LruCache for JSON parsing
            val marksData = decodedMarksCache.get(log.marksData) ?: try {
                json.decodeFromString<Map<String, Int>>(log.marksData).also {
                    decodedMarksCache.put(log.marksData, it)
                }
            } catch (e: Exception) { emptyMap() }

            var totalScore = 0.0
            val totalPossible = log.numQuestions * sumDefaultPointsContributing

            // Optimization: Iterate over recorded marks instead of all possible mark types (O(M_recorded) vs O(M_all))
            marksData.forEach { (key, markCount) ->
                val markType = markTypeMapByName[key] ?: markTypeMapById[key]
                if (markType != null) {
                    totalScore += markCount * markType.defaultPoints
                }
            }

            val scorePercent = if (totalPossible > 0) (totalScore / totalPossible) * 100 else 0.0
            stats[0] += scorePercent
            stats[1] += 1.0
        }

        val summaryList = mutableListOf<QuizSummary>()
        quizScores.keys.sortedWith(compareBy { students[it]?.lastName }).forEach { studentId ->
            val studentQuizzes = quizScores[studentId]
            studentQuizzes?.keys?.sorted()?.forEach { quizName ->
                val stats = studentQuizzes[quizName]!!
                val student = students[studentId]
                summaryList.add(
                    QuizSummary(
                        studentName = if(student != null) "${student.firstName} ${student.lastName}" else "Unknown",
                        quizName = quizName,
                        averageScore = if (stats[1] > 0) stats[0] / stats[1] else 0.0,
                        timesTaken = stats[1].toInt()
                    )
                )
            }
        }
        return summaryList
    }

    /**
     * Aggregates homework completion data and point totals.
     *
     * **BOLT Optimization**: Minimizes object churn by using a single-pass aggregation
     * over the log list and leveraging [decodedHomeworkMarksCache].
     *
     * @param homeworkLogs The filtered list of homework logs.
     * @param students Student lookup map.
     * @return A sorted list of [HomeworkSummary] objects.
     */
    internal fun calculateHomeworkSummary(homeworkLogs: List<HomeworkLog>, students: Map<Long, Student>): List<HomeworkSummary> {
        // Optimization: Single-pass iteration to calculate homework summary and avoid Pair object allocations in loop
        val homeworkCountMap = mutableMapOf<Long, MutableMap<String, Int>>()
        val homeworkPointsMap = mutableMapOf<Long, MutableMap<String, Double>>()

        for (log in homeworkLogs) {
            val studentCounts = homeworkCountMap.getOrPut(log.studentId) { mutableMapOf() }
            studentCounts[log.assignmentName] = (studentCounts[log.assignmentName] ?: 0) + 1

            var points = 0.0
            log.marksData?.let { jsonStr ->
                // Optimization: LruCache for JSON parsing
                val marks = decodedHomeworkMarksCache.get(jsonStr) ?: try {
                    json.decodeFromString<Map<String, String>>(jsonStr).also {
                        decodedHomeworkMarksCache.put(jsonStr, it)
                    }
                } catch (e: Exception) { emptyMap() }

                for (value in marks.values) {
                    value.toDoubleOrNull()?.let { points += it }
                }
            }
            if (points != 0.0) {
                val studentPoints = homeworkPointsMap.getOrPut(log.studentId) { mutableMapOf() }
                studentPoints[log.assignmentName] = (studentPoints[log.assignmentName] ?: 0.0) + points
            }
        }

        val summaryList = mutableListOf<HomeworkSummary>()
        homeworkCountMap.keys.sortedWith(compareBy { students[it]?.lastName }).forEach { studentId ->
            val studentHomeworks = homeworkCountMap[studentId]
            studentHomeworks?.keys?.sorted()?.forEach { assignmentName ->
                val count = studentHomeworks[assignmentName] ?: 0
                val totalPoints = homeworkPointsMap[studentId]?.get(assignmentName) ?: 0.0
                val student = students[studentId]
                summaryList.add(
                    HomeworkSummary(
                        studentName = if(student != null) "${student.firstName} ${student.lastName}" else "Unknown",
                        assignmentName = assignmentName,
                        count = count,
                        totalPoints = totalPoints
                    )
                )
            }
        }
        return summaryList
    }
}