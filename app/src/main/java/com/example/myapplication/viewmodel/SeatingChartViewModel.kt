package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.commands.AddFurnitureCommand
import com.example.myapplication.commands.AddStudentCommand
import com.example.myapplication.commands.Command
import com.example.myapplication.commands.DeleteStudentCommand
import com.example.myapplication.commands.LoadLayoutCommand
import com.example.myapplication.commands.LogBehaviorCommand
import com.example.myapplication.commands.LogHomeworkCommand
import com.example.myapplication.commands.LogQuizCommand
import com.example.myapplication.commands.MoveFurnitureCommand
import com.example.myapplication.commands.MoveStudentCommand
import com.example.myapplication.commands.UpdateFurnitureCommand
import com.example.myapplication.commands.UpdateStudentCommand
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.BehaviorEventDao
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.FurnitureDao
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkLogDao
import com.example.myapplication.data.HomeworkTemplate
import com.example.myapplication.data.HomeworkTemplateDao
import com.example.myapplication.data.LayoutTemplate
import com.example.myapplication.data.LayoutTemplateDao
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.QuizLogDao
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.QuizMarkTypeDao
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.data.QuizTemplateDao
import com.example.myapplication.data.Student
import com.example.myapplication.data.StudentDao
import com.example.myapplication.data.StudentGroupDao
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.data.importer.Importer
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.ui.model.FurnitureUiItem
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.model.toStudentUiItem
import com.example.myapplication.ui.model.toUiItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.data.exporter.Exporter
import com.example.myapplication.util.ConditionalFormattingEngine
import com.example.myapplication.util.CollisionDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Stack
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlin.math.abs

private const val MILLIS_IN_HOUR = 3_600_000L

@HiltViewModel
class SeatingChartViewModel @Inject constructor(
    private val repository: StudentRepository,
    private val studentDao: StudentDao,
    private val furnitureDao: FurnitureDao,
    private val layoutTemplateDao: LayoutTemplateDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val quizLogDao: QuizLogDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val studentGroupDao: StudentGroupDao,
    private val homeworkTemplateDao: HomeworkTemplateDao,
    private val quizTemplateDao: QuizTemplateDao,
    private val quizMarkTypeDao: QuizMarkTypeDao,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val exporter: Exporter,
    application: Application
) : AndroidViewModel(application) {

    val allStudents: LiveData<List<Student>>
    val allStudentsForDisplay: LiveData<List<com.example.myapplication.data.StudentDetailsForDisplay>>
    val allFurniture: LiveData<List<Furniture>>
    val allLayoutTemplates: LiveData<List<LayoutTemplate>>
    val allBehaviorEvents: LiveData<List<BehaviorEvent>>
    val allHomeworkLogs: LiveData<List<HomeworkLog>>
    val allQuizLogs: LiveData<List<QuizLog>>
    val allRules: LiveData<List<com.example.myapplication.data.ConditionalFormattingRule>>
    val studentsForDisplay = MediatorLiveData<List<StudentUiItem>>()
    val furnitureForDisplay = MediatorLiveData<List<FurnitureUiItem>>()

    val allHomeworkTemplates: LiveData<List<HomeworkTemplate>>
    val customHomeworkTypes: Flow<List<String>> =
        appPreferencesRepository.homeworkAssignmentTypesListFlow.map { it.toList() }
    val customHomeworkStatuses: Flow<List<String>> =
        appPreferencesRepository.homeworkStatusesListFlow.map { it.toList() }

    val allQuizTemplates: LiveData<List<QuizTemplate>> = quizTemplateDao.getAll().asLiveData()
    val quizMarkTypes: LiveData<List<QuizMarkType>>
    val allCustomBehaviors: LiveData<List<com.example.myapplication.data.CustomBehavior>>
    val allCustomHomeworkTypes: LiveData<List<com.example.myapplication.data.CustomHomeworkType>>
    val allSystemBehaviors: LiveData<List<com.example.myapplication.data.SystemBehavior>>


    private val commandUndoStack = Stack<Command>()
    private val commandRedoStack = Stack<Command>()
    private val pendingStudentPositions = ConcurrentHashMap<Int, Pair<Float, Float>>()
    private val pendingFurniturePositions = ConcurrentHashMap<Int, Pair<Float, Float>>()

    // In-memory session data
    private val sessionQuizLogs = MutableLiveData<List<QuizLog>>(emptyList())
    private val sessionHomeworkLogs = MutableLiveData<List<HomeworkLog>>(emptyList())
    val isSessionActive = MutableLiveData<Boolean>(false)
    val currentMode = MutableLiveData<String>("behavior")
    val liveQuizScores = MutableLiveData<Map<Long, Map<String, Any>>>(emptyMap())
    val liveHomeworkScores = MutableLiveData<Map<Long, Map<String, Any>>>(emptyMap())

    val selectedStudentIds = MutableLiveData<Set<Int>>(emptySet())
    var canvasHeight by mutableStateOf(0)
    var canvasWidth by mutableStateOf(0)


    fun clearSelection() {
        selectedStudentIds.value = emptySet()
    }

    init {
        allStudents = repository.allStudents
        allStudentsForDisplay = studentDao.getStudentsForDisplay()
        allFurniture = repository.getAllFurniture().asLiveData()
        allLayoutTemplates = repository.getAllLayoutTemplates().asLiveData()
        allBehaviorEvents = behaviorEventDao.getAllBehaviorEvents()
        allHomeworkLogs = homeworkLogDao.getAllHomeworkLogs()
        allQuizLogs = quizLogDao.getAllQuizLogs()
        allRules = AppDatabase.getDatabase(application).conditionalFormattingRuleDao().getAllRules()
        quizMarkTypes = repository.getAllQuizMarkTypes().asLiveData()
        allHomeworkTemplates = homeworkTemplateDao.getAllHomeworkTemplates().asLiveData()
        allCustomBehaviors = AppDatabase.getDatabase(application).customBehaviorDao().getAllCustomBehaviors()
    allCustomHomeworkTypes = AppDatabase.getDatabase(application).customHomeworkTypeDao().getAllCustomHomeworkTypes()
        allSystemBehaviors = AppDatabase.getDatabase(application).systemBehaviorDao().getAllSystemBehaviors().asLiveData()


        studentsForDisplay.addSource(allStudents) {
            updateStudentsForDisplay(it)
        }
        studentsForDisplay.addSource(allStudentsForDisplay) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
        studentsForDisplay.addSource(studentGroupDao.getAllStudentGroups().asLiveData()) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
        studentsForDisplay.addSource(sessionQuizLogs) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
        studentsForDisplay.addSource(sessionHomeworkLogs) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
        studentsForDisplay.addSource(isSessionActive) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
        studentsForDisplay.addSource(allBehaviorEvents) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
        studentsForDisplay.addSource(allHomeworkLogs) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
        studentsForDisplay.addSource(allQuizLogs) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
        studentsForDisplay.addSource(allRules) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }


        observePreferenceChanges()

        furnitureForDisplay.addSource(allFurniture) { furnitureList ->
            updateFurnitureForDisplay(furnitureList)
        }

        viewModelScope.launch {
            while (true) {
                delay(60000) // 1 minute
                updateStudentsForDisplay(allStudents.value ?: emptyList())
            }
        }
    }

    private fun observePreferenceChanges() {
        viewModelScope.launch {
            combine(
                appPreferencesRepository.recentBehaviorIncidentsLimitFlow,
                appPreferencesRepository.recentHomeworkLogsLimitFlow,
                appPreferencesRepository.recentLogsLimitFlow,
                appPreferencesRepository.maxRecentLogsToDisplayFlow,
                appPreferencesRepository.useInitialsForBehaviorFlow,
                appPreferencesRepository.useInitialsForHomeworkFlow,
                appPreferencesRepository.useInitialsForQuizFlow,
                appPreferencesRepository.behaviorInitialsMapFlow,
                appPreferencesRepository.homeworkInitialsMapFlow,
                appPreferencesRepository.quizInitialsMapFlow,
                appPreferencesRepository.defaultStudentBoxWidthFlow,
                appPreferencesRepository.defaultStudentBoxHeightFlow,
                appPreferencesRepository.defaultStudentBoxBackgroundColorFlow,
                appPreferencesRepository.defaultStudentBoxOutlineColorFlow,
                appPreferencesRepository.defaultStudentBoxTextColorFlow,
                appPreferencesRepository.defaultStudentBoxOutlineThicknessFlow,
                appPreferencesRepository.defaultStudentBoxCornerRadiusFlow,
                appPreferencesRepository.defaultStudentBoxPaddingFlow,
                appPreferencesRepository.defaultStudentFontFamilyFlow,
                appPreferencesRepository.defaultStudentFontSizeFlow,
                appPreferencesRepository.defaultStudentFontColorFlow,
                appPreferencesRepository.behaviorDisplayTimeoutFlow, // Added new timeout flow
                appPreferencesRepository.homeworkDisplayTimeoutFlow, // Added new timeout flow
                appPreferencesRepository.quizDisplayTimeoutFlow // Added new timeout flow
            ) { _ ->
                updateStudentsForDisplay(allStudents.value ?: emptyList())
            }.collect()
        }
    }

    private fun updateStudentsForDisplay(students: List<Student>) {
        viewModelScope.launch {
            val studentsForDisplayData = allStudentsForDisplay.value ?: return@launch
            val studentDetailsMap = studentsForDisplayData.associateBy { it.id }
            val groups = studentGroupDao.getAllStudentGroups().first()
            val behaviorLimit = appPreferencesRepository.recentBehaviorIncidentsLimitFlow.first()
            val homeworkLimit = appPreferencesRepository.recentHomeworkLogsLimitFlow.first()
            val quizLimit = appPreferencesRepository.recentLogsLimitFlow.first()
            val maxLogsToDisplay = appPreferencesRepository.maxRecentLogsToDisplayFlow.first()
            val useInitialsForBehavior = appPreferencesRepository.useInitialsForBehaviorFlow.first()
            val useInitialsForHomework = appPreferencesRepository.useInitialsForHomeworkFlow.first()
            val useInitialsForQuiz = appPreferencesRepository.useInitialsForQuizFlow.first()
            val behaviorInitialsMap = appPreferencesRepository.behaviorInitialsMapFlow.first()
                .split(",")
                .mapNotNull {
                    val parts = it.split(":", limit = 2)
                    if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                }
                .toMap()
            val homeworkInitialsMap = appPreferencesRepository.homeworkInitialsMapFlow.first()
                .split(",")
                .mapNotNull {
                    val parts = it.split(":", limit = 2)
                    if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                }
                .toMap()
            val quizInitialsMap = appPreferencesRepository.quizInitialsMapFlow.first()
                .split(",")
                .mapNotNull {
                    val parts = it.split(":", limit = 2)
                    if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
                }
                .toMap()
            val lastClearedTimestamps = appPreferencesRepository.studentLogsLastClearedFlow.first()

            // Retrieve specific display timeouts
            val behaviorDisplayTimeout = appPreferencesRepository.behaviorDisplayTimeoutFlow.first()
            val homeworkDisplayTimeout = appPreferencesRepository.homeworkDisplayTimeoutFlow.first()
            val quizDisplayTimeout = appPreferencesRepository.quizDisplayTimeoutFlow.first()
            val currentTime = System.currentTimeMillis()

            val defaultWidth = appPreferencesRepository.defaultStudentBoxWidthFlow.first()
            val defaultHeight = appPreferencesRepository.defaultStudentBoxHeightFlow.first()
            val defaultBgColor = appPreferencesRepository.defaultStudentBoxBackgroundColorFlow.first()
            val defaultOutlineColor = appPreferencesRepository.defaultStudentBoxOutlineColorFlow.first()
            val defaultTextColor = appPreferencesRepository.defaultStudentBoxTextColorFlow.first()
            val defaultOutlineThickness = appPreferencesRepository.defaultStudentBoxOutlineThicknessFlow.first()
            val defaultCornerRadius = appPreferencesRepository.defaultStudentBoxCornerRadiusFlow.first()
            val defaultPadding = appPreferencesRepository.defaultStudentBoxPaddingFlow.first()
            val defaultFontFamily = appPreferencesRepository.defaultStudentFontFamilyFlow.first()
            val defaultFontSize = appPreferencesRepository.defaultStudentFontSizeFlow.first()
            val defaultFontColor = appPreferencesRepository.defaultStudentFontColorFlow.first()

                        val studentsWithBehavior = students.map { student ->
                            val studentDetails = studentDetailsMap[student.id] ?: return@map student.toStudentUiItem(
                                recentBehaviorDescription = emptyList(),
                                recentHomeworkDescription = emptyList(),
                                recentQuizDescription = emptyList(),
                                sessionLogText = emptyList(),
                                groupColor = null,
                                conditionalFormattingResult = emptyList(),
                                defaultWidth = defaultWidth,
                                defaultHeight = defaultHeight,
                                defaultBackgroundColor = defaultBgColor,
                                defaultOutlineColor = defaultOutlineColor,
                                defaultTextColor = defaultTextColor,
                                defaultOutlineThickness = defaultOutlineThickness,
                                defaultCornerRadius = defaultCornerRadius,
                                defaultPadding = defaultPadding,
                                defaultFontFamily = defaultFontFamily,
                                defaultFontSize = defaultFontSize,
                                defaultFontColor = defaultFontColor
                            )
            
                            val lastCleared = lastClearedTimestamps[student.id] ?: 0L
                            val recentEvents = if (student.showLogs) {
                                behaviorEventDao.getRecentBehaviorEventsForStudentListFiltered(
                                    student.id, behaviorLimit, lastCleared, behaviorDisplayTimeout, currentTime      
                                )
                            } else {
                                emptyList()
                            }
                            val recentHomework = if (student.showLogs) {
                                homeworkLogDao.getRecentHomeworkLogsForStudentListFiltered(
                                    student.id, homeworkLimit, lastCleared, homeworkDisplayTimeout, currentTime      
                                )
                            } else {
                                emptyList()
                            }
                            val recentQuizzes = if (student.showLogs) {
                                quizLogDao.getRecentQuizLogsForStudentListFiltered(
                                    student.id, quizLimit, lastCleared, quizDisplayTimeout, currentTime
                                )
                            } else {
                                emptyList()
                            }
            
                            val behaviorDescription = recentEvents.map {
                                val description = if (useInitialsForBehavior) {
                                    behaviorInitialsMap[it.type] ?: it.type.first().toString()
                                } else {
                                    it.type
                                }
                                if (it.comment.isNullOrBlank()) {
                                    description
                                } else {
                                    "$description: ${it.comment}"
                                }
                            }
            
                            val homeworkDescription = recentHomework.map {
                                val status = if (it.isComplete) "Done" else "Not Done"
                                if (useInitialsForHomework) {
                                    (homeworkInitialsMap[it.assignmentName] ?: it.assignmentName.first().toString()) + ": $status"
                                } else {
                                    "${it.assignmentName}: $status"
                                }
                            }
                            val quizDescription = recentQuizzes.map {
                                if (useInitialsForQuiz) {
                                    quizInitialsMap[it.quizName] ?: it.quizName.first().toString()
                                } else {
                                    it.quizName
                                }
                            }
            
                            val sessionLogs = if (isSessionActive.value == true) {
                                val quizLogs = sessionQuizLogs.value?.filter { it.studentId == student.id }?.map { "Quiz: ${it.comment}" } ?: emptyList()
                                val homeworkLogs = sessionHomeworkLogs.value?.filter { it.studentId == student.id }?.map { "${it.assignmentName}: ${it.status}" } ?: emptyList()
                                (quizLogs + homeworkLogs).take(maxLogsToDisplay)
                            } else {
                                emptyList()
                            }
            
                            val conditionalFormattingResult = ConditionalFormattingEngine.applyConditionalFormatting(
                                student = studentDetails,
                                rules = allRules.value ?: emptyList(),
                                behaviorLog = allBehaviorEvents.value ?: emptyList(),
                                quizLog = allQuizLogs.value ?: emptyList(),
                                homeworkLog = allHomeworkLogs.value ?: emptyList(),
                                isLiveQuizActive = isSessionActive.value ?: false,
                                liveQuizScores = liveQuizScores.value ?: emptyMap(),
                                isLiveHomeworkActive = isSessionActive.value ?: false,
                                liveHomeworkScores = liveHomeworkScores.value ?: emptyMap(),
                                currentMode = currentMode.value ?: "behavior"
                            )

                            // Apply pending position if available
                            val pendingPos = pendingStudentPositions[student.id.toInt()]
                            var studentForUi = student
                            if (pendingPos != null) {
                                if (abs(student.xPosition - pendingPos.first) < 0.1f && abs(student.yPosition - pendingPos.second) < 0.1f) {
                                    pendingStudentPositions.remove(student.id.toInt())
                                } else {
                                    studentForUi = student.copy(xPosition = pendingPos.first, yPosition = pendingPos.second)
                                }
                            }
            
                            studentForUi.toStudentUiItem(
                                recentBehaviorDescription = behaviorDescription,
                                recentHomeworkDescription = homeworkDescription,
                                recentQuizDescription = quizDescription,
                                sessionLogText = sessionLogs,
                                groupColor = groups.find { group -> group.id == student.groupId }?.color,
                                conditionalFormattingResult = conditionalFormattingResult,
                                defaultWidth = defaultWidth,
                                defaultHeight = defaultHeight,
                                defaultBackgroundColor = defaultBgColor,
                                defaultOutlineColor = defaultOutlineColor,
                                defaultTextColor = defaultTextColor,
                                defaultOutlineThickness = defaultOutlineThickness,
                                defaultCornerRadius = defaultCornerRadius,
                                defaultPadding = defaultPadding,
                                defaultFontFamily = defaultFontFamily,
                                defaultFontSize = defaultFontSize,
                                defaultFontColor = defaultFontColor
                            )
                        }
                        studentsForDisplay.postValue(studentsWithBehavior)        }
    }

    fun clearRecentLogsForStudent(studentId: Long) {
        viewModelScope.launch {
            val student = getStudentForEditing(studentId) ?: return@launch
            val updatedStudent = student.copy(showLogs = false)
            updateStudent(student, updatedStudent)
        }
    }

    fun showRecentLogsForStudent(studentId: Long) {
        viewModelScope.launch {
            val student = getStudentForEditing(studentId) ?: return@launch
            val updatedStudent = student.copy(showLogs = true)
            updateStudent(student, updatedStudent)
        }
    }


    fun undo() {
        if (commandUndoStack.isNotEmpty()) {
            viewModelScope.launch {
                val command = commandUndoStack.pop()
                command.undo()
                commandRedoStack.push(command)
            }
        }
    }

    fun redo() {
        if (commandRedoStack.isNotEmpty()) {
            viewModelScope.launch {
                val command = commandRedoStack.pop()
                command.execute()
                commandUndoStack.push(command)
            }
        }
    }

    private suspend fun getFilteredBehaviorEvents(startDate: Long, endDate: Long, studentIds: List<Long>?): List<BehaviorEvent> {
        return if (studentIds.isNullOrEmpty()) {
            behaviorEventDao.getFilteredBehaviorEvents(startDate, endDate)
        } else {
            behaviorEventDao.getFilteredBehaviorEventsWithStudents(startDate, endDate, studentIds)
        }
    }

    private suspend fun getFilteredHomeworkLogs(startDate: Long, endDate: Long, studentIds: List<Long>?): List<HomeworkLog> {
        return if (studentIds.isNullOrEmpty()) {
            homeworkLogDao.getFilteredHomeworkLogs(startDate, endDate)
        } else {
            homeworkLogDao.getFilteredHomeworkLogsWithStudents(startDate, endDate, studentIds)
        }
    }

    private suspend fun getFilteredQuizLogs(startDate: Long, endDate: Long, studentIds: List<Long>?): List<QuizLog> {
        return if (studentIds.isNullOrEmpty()) {
            quizLogDao.getFilteredQuizLogs(startDate, endDate)
        } else {
            quizLogDao.getFilteredQuizLogsWithStudents(startDate, endDate, studentIds)
        }
    }

    suspend fun exportData(
        uri: Uri,
        options: com.example.myapplication.data.exporter.ExportOptions
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val allStudentsList = studentDao.getAllStudentsNonLiveData()
        val behaviorLogs = behaviorEventDao.getAllBehaviorEventsList()
        val homeworkLogs = homeworkLogDao.getAllHomeworkLogsList()
        val quizLogs = quizLogDao.getAllQuizLogsList()
        val studentGroups = studentGroupDao.getAllStudentGroupsList()
        val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()
        val customHomeworkTypes = AppDatabase.getDatabase(getApplication()).customHomeworkTypeDao().getAllCustomHomeworkTypesList()
        val customHomeworkStatuses = AppDatabase.getDatabase(getApplication()).customHomeworkStatusDao().getAllCustomHomeworkStatusesList()

        exporter.export(
            uri = uri,
            options = options,
            students = allStudentsList,
            behaviorEvents = behaviorLogs,
            homeworkLogs = homeworkLogs,
            quizLogs = quizLogs,
            studentGroups = studentGroups,
            quizMarkTypes = quizMarkTypes,
            customHomeworkTypes = customHomeworkTypes,
            customHomeworkStatuses = customHomeworkStatuses,
            encrypt = false,
            context = getApplication()
        )
        return@withContext Result.success(Unit)
    }

    suspend fun importStudentsFromExcel(uri: Uri): Result<Int> {
        return com.example.myapplication.util.ExcelImportUtil.importStudentsFromExcel(uri, getApplication(), repository)
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            Importer(
                getApplication(),
                AppDatabase.getDatabase(getApplication()),
                appPreferencesRepository.encryptDataFilesFlow
            ).importData(uri)
        }
    }

    fun importFromPythonAssets() {
        viewModelScope.launch {
            val importer = Importer(
                getApplication(),
                AppDatabase.getDatabase(getApplication()),
                appPreferencesRepository.encryptDataFilesFlow
            )
            importer.importFromAssets()
        }
    }


    // Student operations
    fun addStudent(student: Student) {
        viewModelScope.launch {
            val (resolvedX, resolvedY) = CollisionDetector.resolveCollisions(
                student,
                studentsForDisplay.value ?: emptyList(),
                canvasWidth,
                canvasHeight
            )
            val positionedStudent = student.copy(
                xPosition = resolvedX,
                yPosition = resolvedY
            )
            val command = AddStudentCommand(this@SeatingChartViewModel, positionedStudent)
            executeCommand(command)
        }
    }

    suspend fun internalAddStudent(student: Student) {
        withContext(Dispatchers.IO) {
            repository.insertStudent(student)
        }
    }

    fun updateStudent(oldStudent: Student, newStudent: Student) {
        viewModelScope.launch {
            val command =
                UpdateStudentCommand(this@SeatingChartViewModel, oldStudent, newStudent)
            executeCommand(command)
        }
    }

    suspend fun internalUpdateStudent(student: Student) {
        withContext(Dispatchers.IO) {
            repository.updateStudent(student)
        }
    }


    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            val command = DeleteStudentCommand(this@SeatingChartViewModel, student)
            executeCommand(command)
        }
    }

    suspend fun internalDeleteStudent(student: Student) {
        withContext(Dispatchers.IO) {
            repository.deleteStudent(student)
            withContext(Dispatchers.Main) {
                updateStudentsForDisplay(allStudents.value ?: emptyList())
            }
        }
    }

    fun deleteStudents(studentIds: Set<Int>) {
        viewModelScope.launch {
            val studentsToDelete = allStudents.value?.filter { studentIds.contains(it.id.toInt()) }
            studentsToDelete?.forEach {
                val command = DeleteStudentCommand(this@SeatingChartViewModel, it)
                executeCommand(command)
            }
        }
    }

    fun updateStudentPosition(
        studentId: Int,
        oldX: Float,
        oldY: Float,
        newX: Float,
        newY: Float
    ) {
        viewModelScope.launch {
            val student = getStudentForEditing(studentId.toLong())
            if (student != null) {
                val resolvedX = newX
                val resolvedY = newY

                // Optimistic update
                pendingStudentPositions[studentId] = resolvedX to resolvedY
                updateStudentsForDisplay(allStudents.value ?: emptyList())

                val command = MoveStudentCommand(
                    this@SeatingChartViewModel,
                    studentId,
                    oldX,
                    oldY,
                    resolvedX,
                    resolvedY
                )
                executeCommand(command)
            }
        }
    }

    suspend fun internalUpdateStudentPosition(studentId: Long, newX: Float, newY: Float) {
        withContext(Dispatchers.IO) {
            studentDao.updatePosition(studentId, newX, newY)
        }
    }


    suspend fun getStudentForEditing(studentId: Long): Student? = withContext(Dispatchers.IO) {
        return@withContext repository.getStudentById(studentId)
    }

    suspend fun studentExists(firstName: String, lastName: String): Boolean {
        return repository.studentExists(firstName, lastName)
    }

    fun updateStudentStyle(student: Student) {
        viewModelScope.launch {
            val oldStudent = getStudentForEditing(student.id) ?: return@launch
            val command = UpdateStudentCommand(this@SeatingChartViewModel, oldStudent, student)
            executeCommand(command)
        }
    }

    fun changeBoxSize(studentIds: Set<Int>, width: Int, height: Int) {
        viewModelScope.launch {
            val studentsToUpdate = allStudents.value?.filter { studentIds.contains(it.id.toInt()) }
            studentsToUpdate?.forEach {
                val updatedStudent = it.copy(customWidth = width, customHeight = height)
                val command = UpdateStudentCommand(this@SeatingChartViewModel, it, updatedStudent)
                executeCommand(command)
            }
        }
    }

    fun changeFurnitureSize(furnitureId: Int, width: Int, height: Int) {
        viewModelScope.launch {
            val furnitureToUpdate = allFurniture.value?.find { it.id.toLong() == furnitureId.toLong() }
            furnitureToUpdate?.let {
                val updatedFurniture = it.copy(width = width, height = height)
                val command = UpdateFurnitureCommand(this@SeatingChartViewModel, it, updatedFurniture)
                executeCommand(command)
            }
        }
    }

    fun assignStudentToGroup(studentId: Long, groupId: Long) {
        viewModelScope.launch {
            val student = getStudentForEditing(studentId) ?: return@launch
            val updatedStudent = student.copy(groupId = groupId)
            val command = UpdateStudentCommand(this@SeatingChartViewModel, student, updatedStudent)
            executeCommand(command)
        }
    }

    fun removeStudentFromGroup(studentId: Long) {
        viewModelScope.launch {
            val student = getStudentForEditing(studentId) ?: return@launch
            val updatedStudent = student.copy(groupId = null)
            val command = UpdateStudentCommand(this@SeatingChartViewModel, student, updatedStudent)
            executeCommand(command)
        }
    }

    fun alignSelectedItems(alignment: String) {
        viewModelScope.launch {
            val selectedUiItems = studentsForDisplay.value?.filter { selectedStudentIds.value?.contains(it.id) == true }
            if (selectedUiItems.isNullOrEmpty()) return@launch
            val selectedStudents = allStudents.value?.filter { selectedStudentIds.value?.contains(it.id.toInt()) == true } ?: return@launch

            when (alignment) {
                "top" -> {
                    val topY = selectedUiItems.minOf { it.yPosition.value }
                    selectedUiItems.forEach { it.yPosition.value = topY }
                    selectedStudents.forEach { student ->
                        val newStudent = student.copy(yPosition = topY)
                        executeCommand(UpdateStudentCommand(this@SeatingChartViewModel, student, newStudent))
                    }
                }
                "bottom" -> {
                    val bottomY = selectedUiItems.maxOf { it.yPosition.value + it.displayHeight.value.value }
                    selectedUiItems.forEach { it.yPosition.value = bottomY - it.displayHeight.value.value }
                    selectedStudents.forEach { student ->
                        val uiItem = selectedUiItems.find { it.id == student.id.toInt() } ?: return@forEach
                        val newStudent = student.copy(yPosition = bottomY - uiItem.displayHeight.value.value)
                        executeCommand(UpdateStudentCommand(this@SeatingChartViewModel, student, newStudent))
                    }
                }
                "left" -> {
                    val leftX = selectedUiItems.minOf { it.xPosition.value }
                    selectedUiItems.forEach { it.xPosition.value = leftX }
                    selectedStudents.forEach { student ->
                        val newStudent = student.copy(xPosition = leftX)
                        executeCommand(UpdateStudentCommand(this@SeatingChartViewModel, student, newStudent))
                    }
                }
                "right" -> {
                    val rightX = selectedUiItems.maxOf { it.xPosition.value + it.displayWidth.value.value }
                    selectedUiItems.forEach { it.xPosition.value = rightX - it.displayWidth.value.value }
                    selectedStudents.forEach { student ->
                        val uiItem = selectedUiItems.find { it.id == student.id.toInt() } ?: return@forEach
                        val newStudent = student.copy(xPosition = rightX - uiItem.displayWidth.value.value)
                        executeCommand(UpdateStudentCommand(this@SeatingChartViewModel, student, newStudent))
                    }
                }
            }
        }
    }

    fun distributeSelectedItems(distribution: String) {
        viewModelScope.launch {
            val selectedUiItems = studentsForDisplay.value?.filter { selectedStudentIds.value?.contains(it.id) == true }?.toMutableList()
            if (selectedUiItems.isNullOrEmpty() || selectedUiItems.size < 2) return@launch
            val selectedStudents = allStudents.value?.filter { selectedStudentIds.value?.contains(it.id.toInt()) == true } ?: return@launch

            when (distribution) {
                "horizontal" -> {
                    selectedUiItems.sortBy { it.xPosition.value }
                    val minX = selectedUiItems.first().xPosition.value
                    val maxX = selectedUiItems.last().xPosition.value + selectedUiItems.last().displayWidth.value.value
                    val totalWidth = selectedUiItems.sumOf { it.displayWidth.value.value.toDouble() }.toFloat()
                    val spacing = (maxX - minX - totalWidth) / (selectedUiItems.size - 1)
                    var currentX = minX
                    selectedUiItems.forEach { uiItem ->
                        uiItem.xPosition.value = currentX
                        val student = selectedStudents.find { it.id == uiItem.id.toLong() } ?: return@forEach
                        val newStudent = student.copy(xPosition = currentX)
                        executeCommand(UpdateStudentCommand(this@SeatingChartViewModel, student, newStudent))
                        currentX += uiItem.displayWidth.value.value + spacing
                    }
                }
                "vertical" -> {
                    selectedUiItems.sortBy { it.yPosition.value }
                    val minY = selectedUiItems.first().yPosition.value
                    val maxY = selectedUiItems.last().yPosition.value + selectedUiItems.last().displayHeight.value.value
                    val totalHeight = selectedUiItems.sumOf { it.displayHeight.value.value.toDouble() }.toFloat()
                    val spacing = (maxY - minY - totalHeight) / (selectedUiItems.size - 1)
                    var currentY = minY
                    selectedUiItems.forEach { uiItem ->
                        uiItem.yPosition.value = currentY
                        val student = selectedStudents.find { it.id == uiItem.id.toLong() } ?: return@forEach
                        val newStudent = student.copy(yPosition = currentY)
                        executeCommand(UpdateStudentCommand(this@SeatingChartViewModel, student, newStudent))
                        currentY += uiItem.displayHeight.value.value + spacing
                    }
                }
            }
        }
    }
    // Furniture operations
    fun addFurniture(furniture: Furniture) {
        viewModelScope.launch {
            val command = AddFurnitureCommand(this@SeatingChartViewModel, furniture)
            executeCommand(command)
        }
    }

    suspend fun internalAddFurniture(furniture: Furniture) {
        withContext(Dispatchers.IO) {
            repository.insertFurniture(furniture)
        }
    }

    fun updateFurniture(oldFurniture: Furniture, newFurniture: Furniture) {
        viewModelScope.launch {
            val command =
                UpdateFurnitureCommand(this@SeatingChartViewModel, oldFurniture, newFurniture)
            executeCommand(command)
        }
    }

    suspend fun internalUpdateFurniture(furniture: Furniture) {
        withContext(Dispatchers.IO) {
            repository.updateFurniture(furniture)
        }
    }

    fun internalDeleteFurniture(furniture: Furniture) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFurniture(furniture)
        }
    }

// In SeatingChartViewModel.kt

    fun updateFurniturePosition(furnitureId: Int, newX: Float, newY: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val furniture = repository.getFurnitureById(furnitureId.toLong())
            furniture?.let {
                // Optimistic update
                pendingFurniturePositions[furnitureId] = newX to newY
                withContext(Dispatchers.Main) {
                    updateFurnitureForDisplay(allFurniture.value ?: emptyList())
                }

                val command = MoveFurnitureCommand(
                    this@SeatingChartViewModel,
                    furnitureId,
                    it.xPosition,
                    it.yPosition,
                    newX,
                    newY
                )
                executeCommand(command)
            }
        }
    }

    private fun updateFurnitureForDisplay(furnitureList: List<Furniture>) {
        val mappedList = furnitureList.map { furniture ->
            val pending = pendingFurniturePositions[furniture.id]
            if (pending != null) {
                if (abs(furniture.xPosition - pending.first) < 0.1f && abs(furniture.yPosition - pending.second) < 0.1f) {
                    pendingFurniturePositions.remove(furniture.id)
                    furniture.toUiItem()
                } else {
                    furniture.copy(xPosition = pending.first, yPosition = pending.second).toUiItem()
                }
            } else {
                furniture.toUiItem()
            }
        }
        furnitureForDisplay.postValue(mappedList)
    }

    fun internalUpdateFurniturePosition(furnitureId: Long, newX: Float, newY: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            furnitureDao.updatePosition(furnitureId, newX, newY)
        }
    }


    suspend fun getFurnitureById(furnitureId: Int): Furniture? = withContext(Dispatchers.IO) {
        return@withContext repository.getFurnitureById(furnitureId.toLong())
    }

    // Layout operations
    fun saveLayout(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val studentsJson = JSONArray(allStudents.value?.map { student ->
            JSONObject().apply {
                put("id", student.id)
                put("x", student.xPosition)
                put("y", student.yPosition)
            }
        }).toString()

        val furnitureJson = JSONArray(allFurniture.value?.map { furniture ->
            JSONObject().apply {
                put("id", furniture.id)
                put("x", furniture.xPosition)
                put("y", furniture.yPosition)
            }
        }).toString()

        val layoutData = JSONObject().apply {
            put("students", studentsJson)
            put("furniture", furnitureJson)
        }.toString()

        val layout = LayoutTemplate(name = name, layoutDataJson = layoutData)
        repository.insertLayoutTemplate(layout)
    }

    fun loadLayout(layout: LayoutTemplate) {
        viewModelScope.launch {
            val oldStudents = allStudents.value ?: emptyList()
            val oldFurniture = allFurniture.value ?: emptyList()
            val command =
                LoadLayoutCommand(this@SeatingChartViewModel, layout, oldStudents, oldFurniture)
            executeCommand(command)
        }
    }

    fun internalLoadLayout(layout: LayoutTemplate) = viewModelScope.launch(Dispatchers.IO) {
        val layoutData = JSONObject(layout.layoutDataJson)
        val studentPositions = JSONArray(layoutData.getString("students"))
        for (i in 0 until studentPositions.length()) {
            val pos = studentPositions.getJSONObject(i)
            val x = pos.getDouble("x").toFloat()
            val y = pos.getDouble("y").toFloat()
            studentDao.updatePosition(pos.getLong("id"), x, y)
        }

        val furniturePositions = JSONArray(layoutData.getString("furniture"))
        for (i in 0 until furniturePositions.length()) {
            val pos = furniturePositions.getJSONObject(i)
            val x = pos.getDouble("x").toFloat()
            val y = pos.getDouble("y").toFloat()
            furnitureDao.updatePosition(pos.getLong("id"), x, y)
        }
    }

    fun internalUpdateAll(students: List<Student>, furniture: List<Furniture>) {
        viewModelScope.launch(Dispatchers.IO) {
            studentDao.updateAll(students)
            furnitureDao.updateAll(furniture)
        }
    }

    fun deleteLayoutTemplate(layout: LayoutTemplate) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteLayoutTemplate(layout)
    }

    fun addBehaviorEvent(event: BehaviorEvent) {
        viewModelScope.launch {
            val command = LogBehaviorCommand(this@SeatingChartViewModel, event)
            executeCommand(command)
        }
    }

    fun internalAddBehaviorEvent(event: BehaviorEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertBehaviorEvent(event)
            withContext(Dispatchers.Main) {
                updateStudentsForDisplay(allStudents.value ?: emptyList())
            }
        }
    }

    fun deleteBehaviorEvent(event: BehaviorEvent) = viewModelScope.launch(Dispatchers.IO) {
        behaviorEventDao.delete(event)
        withContext(Dispatchers.Main) {
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
    }

    fun updateBehaviorEvent(event: BehaviorEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            behaviorEventDao.updateBehaviorEvent(event)
            withContext(Dispatchers.Main) {
                updateStudentsForDisplay(allStudents.value ?: emptyList())
            }
        }
    }

    // QuizLog operations
    fun saveQuizLog(log: QuizLog) {
        viewModelScope.launch {
            val command = LogQuizCommand(this@SeatingChartViewModel, log)
            executeCommand(command)
        }
    }

    fun internalSaveQuizLog(log: QuizLog) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertQuizLog(log)
        }
    }

    fun deleteQuizLog(log: QuizLog) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteQuizLog(log)
    }

    // HomeworkLog operations
    fun addHomeworkLog(log: HomeworkLog) {
        viewModelScope.launch {
            val command = LogHomeworkCommand(this@SeatingChartViewModel, log)
            executeCommand(command)
        }
    }

    fun internalAddHomeworkLog(log: HomeworkLog) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertHomeworkLog(log)
        }
    }

    fun deleteHomeworkLog(log: HomeworkLog) = viewModelScope.launch(Dispatchers.IO) {
        homeworkLogDao.delete(log)
    }

    fun startSession() {
        isSessionActive.value = true
        sessionQuizLogs.value = emptyList()
        sessionHomeworkLogs.value = emptyList()
        Log.d("SeatingChartViewModel", "Session started.")
    }


    fun endSession() {
        if (isSessionActive.value == true) {
            viewModelScope.launch(Dispatchers.IO) {
                val quizLogsToSave = sessionQuizLogs.value
                if (!quizLogsToSave.isNullOrEmpty()) {
                    quizLogDao.insertAll(quizLogsToSave)
                }

                val homeworkLogsToSave = sessionHomeworkLogs.value
                if (!homeworkLogsToSave.isNullOrEmpty()) {
                    homeworkLogDao.insertAll(homeworkLogsToSave)
                }

                Log.d(
                    "SeatingChartViewModel",
                    "Session ended. Saved ${quizLogsToSave?.size ?: 0} quiz logs and ${homeworkLogsToSave?.size ?: 0} homework logs."
                )
                // Clear the session data
                sessionQuizLogs.postValue(emptyList())
                sessionHomeworkLogs.postValue(emptyList())
                isSessionActive.postValue(false)
            }
        }
    }


    fun addQuizLogToSession(quizLog: QuizLog) {
        if (isSessionActive.value == true) {
            val currentLogs = sessionQuizLogs.value.orEmpty().toMutableList()
            val existingLogIndex = currentLogs.indexOfFirst {
                it.studentId == quizLog.studentId && it.quizName == quizLog.quizName
            }

            if (existingLogIndex != -1) {
                currentLogs[existingLogIndex] = quizLog
            } else {
                currentLogs.add(quizLog)
            }
            sessionQuizLogs.postValue(currentLogs)

            // Update live scores for immediate UI feedback
            val studentScores = liveQuizScores.value?.get(quizLog.studentId)?.toMutableMap() ?: mutableMapOf()
            studentScores["last_response"] = quizLog.comment ?: ""
            studentScores["mark_value"] = quizLog.markValue ?: 0
            studentScores["max_mark_value"] = quizLog.maxMarkValue ?: 0
            studentScores["marks_data"] = quizLog.marksData

            val allScores = liveQuizScores.value?.toMutableMap() ?: mutableMapOf()
            allScores[quizLog.studentId] = studentScores
            liveQuizScores.postValue(allScores)

            Log.d(
                "SeatingChartViewModel",
                "Quiz log added/updated in session for student ${quizLog.studentId}."
            )
        } else {
            // If not in a session, save directly to the database
            saveQuizLog(quizLog)
        }
    }

    fun addHomeworkLogToSession(homeworkLog: HomeworkLog) {
        if (isSessionActive.value == true) {
            val currentLogs = sessionHomeworkLogs.value.orEmpty().toMutableList()
            val existingLogIndex = currentLogs.indexOfFirst {
                it.studentId == homeworkLog.studentId && it.assignmentName == homeworkLog.assignmentName
            }

            if (existingLogIndex != -1) {
                currentLogs[existingLogIndex] = homeworkLog
            } else {
                currentLogs.add(homeworkLog)
            }
            sessionHomeworkLogs.postValue(currentLogs)

            // Update live scores for immediate UI feedback
            val studentScores = liveHomeworkScores.value?.get(homeworkLog.studentId)?.toMutableMap() ?: mutableMapOf()
            studentScores[homeworkLog.assignmentName] = homeworkLog.status
            val allScores = liveHomeworkScores.value?.toMutableMap() ?: mutableMapOf()
            allScores[homeworkLog.studentId] = studentScores
            liveHomeworkScores.postValue(allScores)


            Log.d(
                "SeatingChartViewModel",
                "Homework log added/updated in session for student ${homeworkLog.studentId}."
            )
        } else {
            addHomeworkLog(homeworkLog)
        }
    }

    private suspend fun executeCommand(command: Command) {
        command.execute()
        commandUndoStack.push(command)
        commandRedoStack.clear()
    }

    fun assignTaskToStudent(studentId: Long, task: String) {
        viewModelScope.launch {
            val student = getStudentForEditing(studentId) ?: return@launch
            val updatedStudent = student.copy(temporaryTask = task)
            val command = UpdateStudentCommand(this@SeatingChartViewModel, student, updatedStudent)
            executeCommand(command)
        }
    }

    fun completeTaskForStudent(studentId: Long) {
        viewModelScope.launch {
            val student = getStudentForEditing(studentId) ?: return@launch
            val updatedStudent = student.copy(temporaryTask = null)
            val command = UpdateStudentCommand(this@SeatingChartViewModel, student, updatedStudent)
            executeCommand(command)
        }
    }
}
