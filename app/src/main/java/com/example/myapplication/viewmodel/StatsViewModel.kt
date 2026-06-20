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
data class QuizSummary(
    val studentName: String,
    val quizName: String,
    val averageScore: Double,
    val averageScoreFormatted: String,
    val timesTaken: Int
)

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
data class AttendanceSummary(
    val studentName: String,
    val daysPresent: Int,
    val daysAbsent: Int,
    val attendancePercentage: Double,
    val attendancePercentageFormatted: String
)

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

    private var updateJob: kotlinx.coroutines.Job? = null

    /** Legacy LiveData for backward compatibility or simple observers. */
    val behaviorSummary: LiveData<List<BehaviorSummary>> = _statsData.map { it.behaviorSummary }
    val quizSummary: LiveData<List<QuizSummary>> = _statsData.map { it.quizSummary }
    val homeworkSummary: LiveData<List<HomeworkSummary>> = _statsData.map { it.homeworkSummary }

    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()
    val allCustomBehaviors: LiveData<List<CustomBehavior>> = customBehaviorDao.getAllCustomBehaviors()
    val allCustomHomeworkTypes: LiveData<List<CustomHomeworkType>> = customHomeworkTypeDao.getAllCustomHomeworkTypes()


    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Cache for pre-calculated points sum from homework marks data.
     * BOLT: Caching the Double sum directly avoids map iterations and string-to-double conversions in loops.
     */
    private val decodedHomeworkPointsCache = LruCache<String, Double>(1000)

    init {
        // Load initial data with default options
        updateStats(ExportOptions())
    }

    /**
     * Triggers a refresh of classroom statistics based on the provided [ExportOptions].
     *
     * BOLT: Optimized Consolidated Synthesis Pipeline.
     * Instead of performing up to 8 passes over log data (filtering, summary aggregation,
     * and attendance tracking), this pipeline uses a **Single-Pass Strategy** per log type.
     *
     * Performance Gains:
     * 1. **O(L) Complexity**: Reduces total iterations from multiple passes to exactly one pass per log.
     * 2. **Zero-Allocation Filtering**: Eliminates intermediate list allocations from .filter() calls.
     * 3. **LongSparseArray Accumulators**: Avoids boxed Long student IDs during aggregation.
     *
     * @param options Configuration for filtering (date range, selected students, log types).
     */
    fun updateStats(options: ExportOptions) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val startDate = options.startDate ?: 0L
                val endDate = options.endDate ?: Long.MAX_VALUE
                val studentIds = options.studentIds

                // Initial data fetching
                val students = if (studentIds != null && studentIds.isNotEmpty()) {
                    studentDao.getStudentsByIdsList(studentIds)
                } else {
                    studentDao.getAllStudentsNonLiveData()
                }
                val sortedStudents = students.sortedWith(compareBy({ it.lastName }, { it.firstName }))
                val studentNameMap = sortedStudents.associate { it.id to "${it.firstName} ${it.lastName}" }

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

                // Aggregator initialization
                val zoneId = ZoneId.systemDefault()
                var lastDayStartMs = Long.MIN_VALUE
                var lastDayEndMs = Long.MIN_VALUE
                var lastEpochDay = -1L

                /** BOLT: Optimized epoch-day calculation with localized caching. */
                fun getEpochDay(ms: Long): Long {
                    if (ms in lastDayStartMs..lastDayEndMs) return lastEpochDay
                    val localDate = Instant.ofEpochMilli(ms).atZone(zoneId).toLocalDate()
                    lastEpochDay = localDate.toEpochDay()
                    lastDayStartMs = localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
                    lastDayEndMs = localDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
                    return lastEpochDay
                }

                val startMillis = options.startDate ?: run {
                    val bStart = behaviorEvents.firstOrNull()?.timestamp ?: Long.MAX_VALUE
                    val hStart = homeworkLogs.firstOrNull()?.loggedAt ?: Long.MAX_VALUE
                    val qStart = quizLogs.firstOrNull()?.loggedAt ?: Long.MAX_VALUE
                    val min = minOf(bStart, minOf(hStart, qStart))
                    if (min == Long.MAX_VALUE) System.currentTimeMillis() else min
                }
                val endMillis = options.endDate ?: System.currentTimeMillis()

                val reportStartDay = getEpochDay(startMillis)
                val reportEndDay = getEpochDay(endMillis)

                if (reportEndDay < reportStartDay) {
                    _statsData.postValue(StatsData())
                    return@withContext
                }

                val totalDaysInRange = (reportEndDay - reportStartDay + 1).toInt().coerceAtMost(366)
                val lastReportDay = reportStartDay + totalDaysInRange - 1

                val studentActiveDays = android.util.LongSparseArray<java.util.BitSet>()
                val behaviorCounts = android.util.LongSparseArray<MutableMap<String, Int>>()
                val homeworkCountMap = android.util.LongSparseArray<MutableMap<String, Int>>()
                val homeworkPointsMap = android.util.LongSparseArray<MutableMap<String, Double>>()
                val quizScores = android.util.LongSparseArray<MutableMap<String, DoubleArray>>()

                val scoringContext = com.example.myapplication.util.QuizScoreEngine.getScoringContext(quizMarkTypes)
                val behaviorTypesSet = options.behaviorTypes?.toSet()
                val homeworkTypesSet = options.homeworkTypes?.toSet()

                // PASS 1: Consolidated Behavior Processing
                for (i in behaviorEvents.indices) {
                    val event = behaviorEvents[i]
                    val day = getEpochDay(event.timestamp)

                    // Attendance check
                    if (day in reportStartDay..lastReportDay) {
                        var bitSet = studentActiveDays.get(event.studentId)
                        if (bitSet == null) {
                            bitSet = java.util.BitSet(totalDaysInRange)
                            studentActiveDays.put(event.studentId, bitSet)
                        }
                        bitSet.set((day - reportStartDay).toInt())
                    }

                    // Behavior Summary aggregation with in-place filtering
                    if (behaviorTypesSet == null || behaviorTypesSet.contains(event.type)) {
                        var studentBehaviors = behaviorCounts.get(event.studentId)
                        if (studentBehaviors == null) {
                            studentBehaviors = mutableMapOf()
                            behaviorCounts.put(event.studentId, studentBehaviors)
                        }
                        studentBehaviors[event.type] = (studentBehaviors[event.type] ?: 0) + 1
                    }
                }

                // PASS 2: Consolidated Homework Processing
                for (i in homeworkLogs.indices) {
                    val log = homeworkLogs[i]
                    val day = getEpochDay(log.loggedAt)

                    // Attendance check
                    if (day in reportStartDay..lastReportDay) {
                        var bitSet = studentActiveDays.get(log.studentId)
                        if (bitSet == null) {
                            bitSet = java.util.BitSet(totalDaysInRange)
                            studentActiveDays.put(log.studentId, bitSet)
                        }
                        bitSet.set((day - reportStartDay).toInt())
                    }

                    // Homework Summary aggregation with in-place filtering
                    if (homeworkTypesSet == null || homeworkTypesSet.contains(log.assignmentName)) {
                        var studentCounts = homeworkCountMap.get(log.studentId)
                        if (studentCounts == null) {
                            studentCounts = mutableMapOf()
                            homeworkCountMap.put(log.studentId, studentCounts)
                        }
                        studentCounts[log.assignmentName] = (studentCounts[log.assignmentName] ?: 0) + 1

                        log.marksData?.let { jsonStr ->
                            val points = decodedHomeworkPointsCache.get(jsonStr) ?: run {
                                var sum = 0.0
                                try {
                                    val marks = json.decodeFromString<Map<String, String>>(jsonStr)
                                    for (value in marks.values) {
                                        value.toDoubleOrNull()?.let { sum += it }
                                    }
                                } catch (e: Exception) { }
                                decodedHomeworkPointsCache.put(jsonStr, sum)
                                sum
                            }
                            if (points != 0.0) {
                                var studentPoints = homeworkPointsMap.get(log.studentId)
                                if (studentPoints == null) {
                                    studentPoints = mutableMapOf()
                                    homeworkPointsMap.put(log.studentId, studentPoints)
                                }
                                studentPoints[log.assignmentName] = (studentPoints[log.assignmentName] ?: 0.0) + points
                            }
                        }
                    }
                }

                // PASS 3: Consolidated Quiz Processing
                for (i in quizLogs.indices) {
                    val log = quizLogs[i]
                    val day = getEpochDay(log.loggedAt)

                    // Attendance check
                    if (day in reportStartDay..lastReportDay) {
                        var bitSet = studentActiveDays.get(log.studentId)
                        if (bitSet == null) {
                            bitSet = java.util.BitSet(totalDaysInRange)
                            studentActiveDays.put(log.studentId, bitSet)
                        }
                        bitSet.set((day - reportStartDay).toInt())
                    }

                    // Quiz Summary aggregation
                    var studentScores = quizScores.get(log.studentId)
                    if (studentScores == null) {
                        studentScores = mutableMapOf()
                        quizScores.put(log.studentId, studentScores)
                    }
                    val stats = studentScores.getOrPut(log.quizName) { DoubleArray(2) } // [0] = sum, [1] = count
                    val scorePercent = com.example.myapplication.util.QuizScoreEngine.calculatePercentage(log, scoringContext) ?: 0.0
                    stats[0] += scorePercent
                    stats[1] += 1.0
                }

                // Final accumulation pass to build StatsData object
                // BOLT: Iterate over sorted students to maintain correct display order.
                val behaviorSummaryList = mutableListOf<BehaviorSummary>()
                val quizSummaryList = mutableListOf<QuizSummary>()
                val homeworkSummaryList = mutableListOf<HomeworkSummary>()
                val attendanceSummaryList = mutableListOf<AttendanceSummary>()

                for (student in sortedStudents) {
                    val studentName = studentNameMap[student.id] ?: ""

                    // 1. Finalize Behavior Summary
                    behaviorCounts.get(student.id)?.let { studentBehaviors ->
                        studentBehaviors.keys.sorted().forEach { behavior ->
                            behaviorSummaryList.add(BehaviorSummary(studentName, behavior, studentBehaviors[behavior] ?: 0))
                        }
                    }

                    // 2. Finalize Quiz Summary
                    quizScores.get(student.id)?.let { studentQuizzes ->
                        studentQuizzes.keys.sorted().forEach { quizName ->
                            val stats = studentQuizzes[quizName]!!
                            val avg = if (stats[1] > 0) stats[0] / stats[1] else 0.0
                            quizSummaryList.add(QuizSummary(
                                studentName = studentName,
                                quizName = quizName,
                                averageScore = avg,
                                averageScoreFormatted = "%.2f".format(avg),
                                timesTaken = stats[1].toInt()
                            ))
                        }
                    }

                    // 3. Finalize Homework Summary
                    homeworkCountMap.get(student.id)?.let { studentHomeworks ->
                        studentHomeworks.keys.sorted().forEach { assignmentName ->
                            homeworkSummaryList.add(HomeworkSummary(
                                studentName = studentName,
                                assignmentName = assignmentName,
                                count = studentHomeworks[assignmentName] ?: 0,
                                totalPoints = homeworkPointsMap.get(student.id)?.get(assignmentName) ?: 0.0
                            ))
                        }
                    }

                    // 4. Finalize Attendance Summary
                    val bitSet = studentActiveDays.get(student.id)
                    val presentCount = bitSet?.cardinality() ?: 0
                    val absentCount = totalDaysInRange - presentCount
                    val percentage = if (totalDaysInRange > 0) (presentCount.toDouble() / totalDaysInRange) * 100 else 0.0
                    attendanceSummaryList.add(AttendanceSummary(
                        studentName = studentName,
                        daysPresent = presentCount,
                        daysAbsent = absentCount,
                        attendancePercentage = percentage,
                        attendancePercentageFormatted = "%.1f".format(percentage)
                    ))
                }

                _statsData.postValue(
                    StatsData(
                        behaviorSummary = behaviorSummaryList,
                        quizSummary = quizSummaryList,
                        homeworkSummary = homeworkSummaryList,
                        attendanceSummary = attendanceSummaryList,
                        totalDaysInRange = totalDaysInRange
                    )
                )
            }
        }
    }

    /**
     * BOLT: Optimized single-pass implementation of behavior summary.
     * Note: This method is now legacy as its logic has been consolidated into [updateStats].
     * Retained as internal for unit test compatibility.
     */
    internal fun calculateBehaviorSummary(
        behaviorEvents: List<BehaviorEvent>,
        sortedStudents: List<Student>,
        studentNameMap: Map<Long, String>
    ): List<BehaviorSummary> {
        val behaviorCounts = android.util.LongSparseArray<MutableMap<String, Int>>()
        for (event in behaviorEvents) {
            var studentBehaviors = behaviorCounts.get(event.studentId)
            if (studentBehaviors == null) {
                studentBehaviors = mutableMapOf()
                behaviorCounts.put(event.studentId, studentBehaviors)
            }
            studentBehaviors[event.type] = (studentBehaviors[event.type] ?: 0) + 1
        }
        val summaryList = mutableListOf<BehaviorSummary>()
        for (student in sortedStudents) {
            val studentBehaviors = behaviorCounts.get(student.id) ?: continue
            val studentName = studentNameMap[student.id] ?: ""
            studentBehaviors.keys.sorted().forEach { behavior ->
                summaryList.add(BehaviorSummary(studentName, behavior, studentBehaviors[behavior] ?: 0))
            }
        }
        return summaryList
    }

    /**
     * BOLT: Optimized single-pass implementation of quiz summary.
     * Retained as internal for unit test compatibility.
     */
    internal fun calculateQuizSummary(
        quizLogs: List<QuizLog>,
        sortedStudents: List<Student>,
        quizMarkTypes: List<QuizMarkType>,
        studentNameMap: Map<Long, String>
    ): List<QuizSummary> {
        val scoringContext = com.example.myapplication.util.QuizScoreEngine.getScoringContext(quizMarkTypes)
        val quizScores = android.util.LongSparseArray<MutableMap<String, DoubleArray>>()
        for (log in quizLogs) {
            var studentScores = quizScores.get(log.studentId)
            if (studentScores == null) {
                studentScores = mutableMapOf()
                quizScores.put(log.studentId, studentScores)
            }
            val stats = studentScores.getOrPut(log.quizName) { DoubleArray(2) }
            val scorePercent = com.example.myapplication.util.QuizScoreEngine.calculatePercentage(log, scoringContext) ?: 0.0
            stats[0] += scorePercent
            stats[1] += 1.0
        }
        val summaryList = mutableListOf<QuizSummary>()
        for (student in sortedStudents) {
            val studentQuizzes = quizScores.get(student.id) ?: continue
            val studentName = studentNameMap[student.id] ?: ""
            studentQuizzes.keys.sorted().forEach { quizName ->
                val stats = studentQuizzes[quizName]!!
                val avg = if (stats[1] > 0) stats[0] / stats[1] else 0.0
                summaryList.add(QuizSummary(studentName, quizName, avg, "%.2f".format(avg), stats[1].toInt()))
            }
        }
        return summaryList
    }

    /**
     * BOLT: Optimized single-pass implementation of homework summary.
     * Retained as internal for unit test compatibility.
     */
    internal fun calculateHomeworkSummary(
        homeworkLogs: List<HomeworkLog>,
        sortedStudents: List<Student>,
        studentNameMap: Map<Long, String>
    ): List<HomeworkSummary> {
        val homeworkCountMap = android.util.LongSparseArray<MutableMap<String, Int>>()
        val homeworkPointsMap = android.util.LongSparseArray<MutableMap<String, Double>>()
        for (log in homeworkLogs) {
            var studentCounts = homeworkCountMap.get(log.studentId)
            if (studentCounts == null) {
                studentCounts = mutableMapOf()
                homeworkCountMap.put(log.studentId, studentCounts)
            }
            studentCounts[log.assignmentName] = (studentCounts[log.assignmentName] ?: 0) + 1
            log.marksData?.let { jsonStr ->
                val points = decodedHomeworkPointsCache.get(jsonStr) ?: run {
                    var sum = 0.0
                    try {
                        val marks = json.decodeFromString<Map<String, String>>(jsonStr)
                        for (value in marks.values) {
                            value.toDoubleOrNull()?.let { sum += it }
                        }
                    } catch (e: Exception) { }
                    decodedHomeworkPointsCache.put(jsonStr, sum)
                    sum
                }
                if (points != 0.0) {
                    var studentPoints = homeworkPointsMap.get(log.studentId)
                    if (studentPoints == null) {
                        studentPoints = mutableMapOf()
                        homeworkPointsMap.put(log.studentId, studentPoints)
                    }
                    studentPoints[log.assignmentName] = (studentPoints[log.assignmentName] ?: 0.0) + points
                }
            }
        }
        val summaryList = mutableListOf<HomeworkSummary>()
        for (student in sortedStudents) {
            val studentHomeworks = homeworkCountMap.get(student.id) ?: continue
            val studentName = studentNameMap[student.id] ?: ""
            studentHomeworks.keys.sorted().forEach { assignmentName ->
                summaryList.add(HomeworkSummary(studentName, assignmentName, studentHomeworks[assignmentName] ?: 0, homeworkPointsMap.get(student.id)?.get(assignmentName) ?: 0.0))
            }
        }
        return summaryList
    }

    /**
     * BOLT: Optimized single-pass implementation of attendance summary.
     * Retained as internal for unit test compatibility.
     */
    internal fun calculateAttendanceSummary(
        options: ExportOptions,
        students: List<Student>,
        behaviorEvents: List<BehaviorEvent>,
        homeworkLogs: List<HomeworkLog>,
        quizLogs: List<QuizLog>,
        studentNameMap: Map<Long, String>
    ): Pair<List<AttendanceSummary>, Int> {
        val zoneId = ZoneId.systemDefault()
        var lastDayStartMs = Long.MIN_VALUE
        var lastDayEndMs = Long.MIN_VALUE
        var lastEpochDay = -1L
        fun getEpochDay(ms: Long): Long {
            if (ms in lastDayStartMs..lastDayEndMs) return lastEpochDay
            val localDate = Instant.ofEpochMilli(ms).atZone(zoneId).toLocalDate()
            lastEpochDay = localDate.toEpochDay()
            lastDayStartMs = localDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
            lastDayEndMs = localDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
            return lastEpochDay
        }
        val startMillis = options.startDate ?: run {
            val bStart = behaviorEvents.firstOrNull()?.timestamp ?: Long.MAX_VALUE
            val hStart = homeworkLogs.firstOrNull()?.loggedAt ?: Long.MAX_VALUE
            val qStart = quizLogs.firstOrNull()?.loggedAt ?: Long.MAX_VALUE
            val min = minOf(bStart, minOf(hStart, qStart))
            if (min == Long.MAX_VALUE) System.currentTimeMillis() else min
        }
        val endMillis = options.endDate ?: System.currentTimeMillis()
        val reportStartDay = getEpochDay(startMillis)
        val reportEndDay = getEpochDay(endMillis)
        if (reportEndDay < reportStartDay) return Pair(emptyList(), 0)
        val totalDaysInRange = (reportEndDay - reportStartDay + 1).toInt().coerceAtMost(366)
        val lastReportDay = reportStartDay + totalDaysInRange - 1
        val studentActiveDays = android.util.LongSparseArray<java.util.BitSet>()
        // This is a simplified version of the logic used in Passive consolidation
        for (event in behaviorEvents) {
            val day = getEpochDay(event.timestamp)
            if (day in reportStartDay..lastReportDay) {
                var bitSet = studentActiveDays.get(event.studentId)
                if (bitSet == null) {
                    bitSet = java.util.BitSet(totalDaysInRange)
                    studentActiveDays.put(event.studentId, bitSet)
                }
                bitSet.set((day - reportStartDay).toInt())
            }
        }
        for (log in homeworkLogs) {
            val day = getEpochDay(log.loggedAt)
            if (day in reportStartDay..lastReportDay) {
                var bitSet = studentActiveDays.get(log.studentId)
                if (bitSet == null) {
                    bitSet = java.util.BitSet(totalDaysInRange)
                    studentActiveDays.put(log.studentId, bitSet)
                }
                bitSet.set((day - reportStartDay).toInt())
            }
        }
        for (log in quizLogs) {
            val day = getEpochDay(log.loggedAt)
            if (day in reportStartDay..lastReportDay) {
                var bitSet = studentActiveDays.get(log.studentId)
                if (bitSet == null) {
                    bitSet = java.util.BitSet(totalDaysInRange)
                    studentActiveDays.put(log.studentId, bitSet)
                }
                bitSet.set((day - reportStartDay).toInt())
            }
        }
        val summaryList = mutableListOf<AttendanceSummary>()
        for (student in students) {
            val bitSet = studentActiveDays.get(student.id)
            val presentCount = bitSet?.cardinality() ?: 0
            val absentCount = totalDaysInRange - presentCount
            val percentage = if (totalDaysInRange > 0) (presentCount.toDouble() / totalDaysInRange) * 100 else 0.0
            summaryList.add(AttendanceSummary(studentNameMap[student.id] ?: "", presentCount, absentCount, percentage, "%.1f".format(percentage)))
        }
        return Pair(summaryList, totalDaysInRange)
    }
}