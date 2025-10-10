package com.example.myapplication.viewmodel

import androidx.lifecycle.*
import com.example.myapplication.data.*
import com.example.myapplication.data.exporter.ExportOptions
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// Define data classes to hold the summary information
data class BehaviorSummary(val studentName: String, val behavior: String, val count: Int)
data class QuizSummary(val studentName: String, val quizName: String, val averageScore: Double, val timesTaken: Int)
data class HomeworkSummary(val studentName: String, val assignmentName: String, val count: Int, val totalPoints: Double)

class StatsViewModel(
    private val studentDao: StudentDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val quizLogDao: QuizLogDao,
    private val quizMarkTypeDao: QuizMarkTypeDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val customHomeworkTypeDao: CustomHomeworkTypeDao
) : ViewModel() {

    private val _behaviorSummary = MutableLiveData<List<BehaviorSummary>>(emptyList())
    val behaviorSummary: LiveData<List<BehaviorSummary>> = _behaviorSummary

    private val _quizSummary = MutableLiveData<List<QuizSummary>>(emptyList())
    val quizSummary: LiveData<List<QuizSummary>> = _quizSummary

    private val _homeworkSummary = MutableLiveData<List<HomeworkSummary>>(emptyList())
    val homeworkSummary: LiveData<List<HomeworkSummary>> = _homeworkSummary

    val allStudents: LiveData<List<Student>> = studentDao.getAllStudents()
    val allCustomBehaviors: LiveData<List<CustomBehavior>> = customBehaviorDao.getAllCustomBehaviors()
    val allCustomHomeworkTypes: LiveData<List<CustomHomeworkType>> = customHomeworkTypeDao.getAllCustomHomeworkTypes()


    init {
        // Load initial data with default options
        updateStats(ExportOptions())
    }

    fun updateStats(options: ExportOptions) {
        viewModelScope.launch {
            val students = studentDao.getAllStudentsNonLiveData()
            val behaviorEvents = behaviorEventDao.getAllBehaviorEventsList()
            val homeworkLogs = homeworkLogDao.getAllHomeworkLogsList()
            val quizLogs = quizLogDao.getAllQuizLogsList()
            val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()

            // Filter data based on options
            val filteredBehaviorEvents = behaviorEvents.filter { event ->
                (options.startDate == null || event.timestamp >= options.startDate) &&
                        (options.endDate == null || event.timestamp <= options.endDate) &&
                        (options.studentIds == null || options.studentIds.contains(event.studentId)) &&
                        (options.behaviorTypes == null || options.behaviorTypes.contains(event.type))
            }

            val filteredHomeworkLogs = homeworkLogs.filter { log ->
                (options.startDate == null || log.loggedAt >= options.startDate) &&
                        (options.endDate == null || log.loggedAt <= options.endDate) &&
                        (options.studentIds == null || options.studentIds.contains(log.studentId)) &&
                        (options.homeworkTypes == null || options.homeworkTypes.contains(log.assignmentName))
            }

            val filteredQuizLogs = quizLogs.filter { log ->
                (options.startDate == null || log.loggedAt >= options.startDate) &&
                        (options.endDate == null || log.loggedAt <= options.endDate) &&
                        (options.studentIds == null || options.studentIds.contains(log.studentId))
            }

            val studentMap = students.associateBy { it.id }

            // Calculate summaries
            calculateBehaviorSummary(filteredBehaviorEvents, studentMap)
            calculateQuizSummary(filteredQuizLogs, studentMap, quizMarkTypes)
            calculateHomeworkSummary(filteredHomeworkLogs, studentMap)
        }
    }

    private fun calculateBehaviorSummary(behaviorEvents: List<BehaviorEvent>, students: Map<Long, Student>) {
        val behaviorCounts = behaviorEvents.groupBy { it.studentId }
            .mapValues { entry -> entry.value.groupingBy { it.type }.eachCount() }

        val summaryList = mutableListOf<BehaviorSummary>()
        behaviorCounts.keys.sortedBy { students[it]?.lastName }.forEach { studentId ->
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
        _behaviorSummary.postValue(summaryList)
    }

    private fun calculateQuizSummary(quizLogs: List<QuizLog>, students: Map<Long, Student>, quizMarkTypes: List<QuizMarkType>) {
        val quizScores = mutableMapOf<Long, MutableMap<String, MutableList<Double>>>()
        quizLogs.forEach { log ->
            val studentScores = quizScores.getOrPut(log.studentId) { mutableMapOf() }
            val quizScoresList = studentScores.getOrPut(log.quizName) { mutableListOf() }
            val marksData = try { Json.decodeFromString<Map<String, Int>>(log.marksData) } catch (e: Exception) { emptyMap() }
            var totalScore = 0.0
            var totalPossible = 0.0
            quizMarkTypes.forEach { markType ->
                val markCount = marksData[markType.id.toString()] ?: 0
                if (markType.contributesToTotal) {
                    totalPossible += log.numQuestions * markType.defaultPoints
                }
                totalScore += markCount * markType.defaultPoints
            }
            val scorePercent = if (totalPossible > 0) (totalScore / totalPossible) * 100 else 0.0
            quizScoresList.add(scorePercent)
        }

        val summaryList = mutableListOf<QuizSummary>()
        quizScores.keys.sortedBy { students[it]?.lastName }.forEach { studentId ->
            val studentQuizzes = quizScores[studentId]
            studentQuizzes?.keys?.sorted()?.forEach { quizName ->
                val scores = studentQuizzes[quizName]!!
                val student = students[studentId]
                summaryList.add(
                    QuizSummary(
                        studentName = if(student != null) "${student.firstName} ${student.lastName}" else "Unknown",
                        quizName = quizName,
                        averageScore = scores.average(),
                        timesTaken = scores.size
                    )
                )
            }
        }
        _quizSummary.postValue(summaryList)
    }

    private fun calculateHomeworkSummary(homeworkLogs: List<HomeworkLog>, students: Map<Long, Student>) {
        val homeworkSummaryMap = mutableMapOf<Long, MutableMap<String, Pair<Int, Double>>>()
        homeworkLogs.forEach { log ->
            val studentSummary = homeworkSummaryMap.getOrPut(log.studentId) { mutableMapOf() }
            val assignmentSummary = studentSummary.getOrPut(log.assignmentName) { Pair(0, 0.0) }
            var points = 0.0
            log.marksData?.let {
                val marks = try { Json.decodeFromString<Map<String, String>>(it) } catch (e: Exception) { emptyMap() }
                marks.values.forEach { value ->
                    value.toDoubleOrNull()?.let { points += it }
                }
            }
            studentSummary[log.assignmentName] = Pair(assignmentSummary.first + 1, assignmentSummary.second + points)
        }

        val summaryList = mutableListOf<HomeworkSummary>()
        homeworkSummaryMap.keys.sortedBy { students[it]?.lastName }.forEach { studentId ->
            val studentHomeworks = homeworkSummaryMap[studentId]
            studentHomeworks?.keys?.sorted()?.forEach { assignmentName ->
                val summary = studentHomeworks[assignmentName]!!
                val student = students[studentId]
                summaryList.add(
                    HomeworkSummary(
                        studentName = if(student != null) "${student.firstName} ${student.lastName}" else "Unknown",
                        assignmentName = assignmentName,
                        count = summary.first,
                        totalPoints = summary.second
                    )
                )
            }
        }
        _homeworkSummary.postValue(summaryList)
    }
}

class StatsViewModelFactory(
    private val studentDao: StudentDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val quizLogDao: QuizLogDao,
    private val quizMarkTypeDao: QuizMarkTypeDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val customHomeworkTypeDao: CustomHomeworkTypeDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatsViewModel(studentDao, behaviorEventDao, homeworkLogDao, quizLogDao, quizMarkTypeDao, customBehaviorDao, customHomeworkTypeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}