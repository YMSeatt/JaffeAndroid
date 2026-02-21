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

// Define data classes to hold the summary information
data class BehaviorSummary(val studentName: String, val behavior: String, val count: Int)
data class QuizSummary(val studentName: String, val quizName: String, val averageScore: Double, val timesTaken: Int)
data class HomeworkSummary(val studentName: String, val assignmentName: String, val count: Int, val totalPoints: Double)
data class AttendanceSummary(val studentName: String, val daysPresent: Int, val daysAbsent: Int, val attendancePercentage: Double)

/**
 * Consolidated statistics data to reduce UI recomposition triggers.
 */
data class StatsData(
    val behaviorSummary: List<BehaviorSummary> = emptyList(),
    val quizSummary: List<QuizSummary> = emptyList(),
    val homeworkSummary: List<HomeworkSummary> = emptyList(),
    val attendanceSummary: List<AttendanceSummary> = emptyList(),
    val totalDaysInRange: Int = 0
)

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
    private val decodedMarksCache = LruCache<String, Map<String, Int>>(1000)
    private val decodedHomeworkMarksCache = LruCache<String, Map<String, String>>(1000)

    init {
        // Load initial data with default options
        updateStats(ExportOptions())
    }

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