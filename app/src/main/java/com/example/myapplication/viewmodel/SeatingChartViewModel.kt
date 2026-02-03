package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.myapplication.commands.AddFurnitureCommand
import com.example.myapplication.commands.AddGuideCommand
import com.example.myapplication.commands.AddStudentCommand
import com.example.myapplication.commands.Command
import com.example.myapplication.commands.DeleteGuideCommand
import com.example.myapplication.commands.DeleteStudentCommand
import com.example.myapplication.commands.LoadLayoutCommand
import com.example.myapplication.commands.LogBehaviorCommand
import com.example.myapplication.commands.LogHomeworkCommand
import com.example.myapplication.commands.LogQuizCommand
import com.example.myapplication.commands.MoveFurnitureCommand
import com.example.myapplication.commands.MoveGuideCommand
import com.example.myapplication.commands.MoveItemsCommand
import com.example.myapplication.commands.ItemMove
import com.example.myapplication.commands.ItemType
import com.example.myapplication.commands.MoveStudentCommand
import com.example.myapplication.commands.UpdateFurnitureCommand
import com.example.myapplication.commands.UpdateStudentCommand
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.BehaviorEventDao
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.data.ConditionalFormattingRuleDao
import com.example.myapplication.data.CustomBehaviorDao
import com.example.myapplication.data.CustomHomeworkTypeDao
import com.example.myapplication.data.Furniture
import com.example.myapplication.data.FurnitureDao
import com.example.myapplication.data.FurnitureLayout
import com.example.myapplication.data.Guide
import com.example.myapplication.data.GuideDao
import com.example.myapplication.data.GuideType
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.HomeworkLogDao
import com.example.myapplication.data.HomeworkTemplate
import com.example.myapplication.data.HomeworkTemplateDao
import com.example.myapplication.data.LayoutData
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
import com.example.myapplication.data.SystemBehaviorDao
import com.example.myapplication.data.StudentLayout
import com.example.myapplication.data.StudentRepository
import com.example.myapplication.data.importer.Importer
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.preferences.UserPreferences
import com.example.myapplication.ui.model.FurnitureUiItem
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.model.toStudentUiItem
import com.example.myapplication.ui.model.toUiItem
import com.example.myapplication.util.CollisionDetector
import com.example.myapplication.util.ConditionalFormattingEngine
import com.example.myapplication.util.EmailWorker
import com.example.myapplication.util.SecurityUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.util.Stack
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlin.math.abs

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
    private val conditionalFormattingRuleDao: ConditionalFormattingRuleDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val customHomeworkTypeDao: CustomHomeworkTypeDao,
    private val systemBehaviorDao: SystemBehaviorDao,
    private val guideDao: GuideDao,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val securityUtil: SecurityUtil,
    private val application: Application
) : ViewModel() {

    val allStudents: LiveData<List<Student>>
    val allStudentsForDisplay: LiveData<List<com.example.myapplication.data.StudentDetailsForDisplay>>
    val allFurniture: LiveData<List<Furniture>>
    val allLayoutTemplates: LiveData<List<LayoutTemplate>>
    val allBehaviorEvents: LiveData<List<BehaviorEvent>>
    val allHomeworkLogs: LiveData<List<HomeworkLog>>
    val allQuizLogs: LiveData<List<QuizLog>>
    val allRules: LiveData<List<com.example.myapplication.data.ConditionalFormattingRule>>
    val allGuides: StateFlow<List<Guide>> = guideDao.getAllGuides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
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
    private val _undoStackState = MutableStateFlow<List<Command>>(emptyList())
    val undoStackState: StateFlow<List<Command>> = _undoStackState.asStateFlow()

    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    private val updateTrigger = MutableSharedFlow<Unit>(replay = 1)
    private var updateJob: Job? = null

    private fun updateUndoStackState() {
        _undoStackState.value = commandUndoStack.toList()
    }
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
    var pendingExportOptions: com.example.myapplication.data.exporter.ExportOptions? by mutableStateOf(null)


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
        allRules = conditionalFormattingRuleDao.getAllRules()
        quizMarkTypes = repository.getAllQuizMarkTypes().asLiveData()
        allHomeworkTemplates = homeworkTemplateDao.getAllHomeworkTemplates().asLiveData()
        allCustomBehaviors = customBehaviorDao.getAllCustomBehaviors()
        allCustomHomeworkTypes = customHomeworkTypeDao.getAllCustomHomeworkTypes()
        allSystemBehaviors = systemBehaviorDao.getAllSystemBehaviors().asLiveData()


        studentsForDisplay.addSource(allStudents) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allStudentsForDisplay) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(studentGroupDao.getAllStudentGroups().asLiveData()) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(sessionQuizLogs) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(sessionHomeworkLogs) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(isSessionActive) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allBehaviorEvents) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allHomeworkLogs) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allQuizLogs) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allRules) { updateTrigger.tryEmit(Unit) }

        viewModelScope.launch {
            appPreferencesRepository.userPreferencesFlow.collect {
                _userPreferences.value = it
                updateTrigger.emit(Unit)
            }
        }

        viewModelScope.launch {
            updateTrigger.debounce(100).collect {
                updateStudentsForDisplay(allStudents.value ?: emptyList())
            }
        }

        furnitureForDisplay.addSource(allFurniture) { furnitureList ->
            updateFurnitureForDisplay(furnitureList)
        }

        viewModelScope.launch {
            while (true) {
                delay(60000) // 1 minute
                updateTrigger.emit(Unit)
            }
        }
    }

    private val allGroups = studentGroupDao.getAllStudentGroups().asLiveData()

    private fun updateStudentsForDisplay(students: List<Student>) {
        val prefs = _userPreferences.value ?: return
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val studentsForDisplayData = allStudentsForDisplay.value ?: return@withContext
                val studentDetailsMap = studentsForDisplayData.associateBy { it.id }

                val behaviorLogsByStudent = allBehaviorEvents.value?.groupBy { it.studentId } ?: emptyMap()
                val homeworkLogsByStudent = allHomeworkLogs.value?.groupBy { it.studentId } ?: emptyMap()
                val quizLogsByStudent = allQuizLogs.value?.groupBy { it.studentId } ?: emptyMap()

                val groups = allGroups.value ?: emptyList()
                val behaviorLimit = prefs.recentBehaviorIncidentsLimit
                val homeworkLimit = prefs.recentHomeworkLogsLimit
                val quizLimit = prefs.recentLogsLimit
                val maxLogsToDisplay = prefs.maxRecentLogsToDisplay
                val useInitialsForBehavior = prefs.useInitialsForBehavior
                val useInitialsForHomework = prefs.useInitialsForHomework
                val useInitialsForQuiz = prefs.useInitialsForQuiz
                val behaviorInitialsMap = parseKeyValueString(prefs.behaviorInitialsMap)
                val homeworkInitialsMap = parseKeyValueString(prefs.homeworkInitialsMap)
                val quizInitialsMap = parseKeyValueString(prefs.quizInitialsMap)
                val lastClearedTimestamps = prefs.studentLogsLastCleared

                val behaviorDisplayTimeout = prefs.behaviorDisplayTimeout
                val homeworkDisplayTimeout = prefs.homeworkDisplayTimeout
                val quizDisplayTimeout = prefs.quizDisplayTimeout
                val currentTime = System.currentTimeMillis()
                val calendar = java.util.Calendar.getInstance()

                val decodedRules = ConditionalFormattingEngine.decodeRules(allRules.value ?: emptyList())

                val defaultStyle = prefs.defaultStudentStyle

                val studentsWithBehavior = students.map { student ->
                    val studentDetails = studentDetailsMap[student.id] ?: return@map student.toStudentUiItem(
                        recentBehaviorDescription = emptyList(),
                        recentHomeworkDescription = emptyList(),
                        recentQuizDescription = emptyList(),
                        sessionLogText = emptyList(),
                        groupColor = null,
                        conditionalFormattingResult = emptyList(),
                        defaultWidth = defaultStyle.width,
                        defaultHeight = defaultStyle.height,
                        defaultBackgroundColor = defaultStyle.backgroundColor,
                        defaultOutlineColor = defaultStyle.outlineColor,
                        defaultTextColor = defaultStyle.textColor,
                        defaultOutlineThickness = defaultStyle.outlineThickness,
                        defaultCornerRadius = defaultStyle.cornerRadius,
                        defaultPadding = defaultStyle.padding,
                        defaultFontFamily = defaultStyle.fontFamily,
                        defaultFontSize = defaultStyle.fontSize,
                        defaultFontColor = defaultStyle.fontColor
                    )

                    val lastCleared = lastClearedTimestamps[student.id] ?: 0L
                    val recentEvents = if (student.showLogs) {
                        behaviorLogsByStudent[student.id]?.filter {
                            it.timestamp > lastCleared && (behaviorDisplayTimeout == 0 || currentTime < it.timestamp + (behaviorDisplayTimeout.toLong() * 3600000L))
                        }?.take(behaviorLimit) ?: emptyList()
                    } else {
                        emptyList()
                    }
                    val recentHomework = if (student.showLogs) {
                        homeworkLogsByStudent[student.id]?.filter {
                            it.loggedAt > lastCleared && (homeworkDisplayTimeout == 0 || currentTime < it.loggedAt + (homeworkDisplayTimeout.toLong() * 3600000L))
                        }?.take(homeworkLimit) ?: emptyList()
                    } else {
                        emptyList()
                    }
                    val recentQuizzes = if (student.showLogs) {
                        quizLogsByStudent[student.id]?.filter {
                            it.loggedAt > lastCleared && !it.isComplete && (quizDisplayTimeout == 0 || currentTime < it.loggedAt + (quizDisplayTimeout.toLong() * 3600000L))
                        }?.take(quizLimit) ?: emptyList()
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

                    val conditionalFormattingResult = ConditionalFormattingEngine.applyConditionalFormattingDecoded(
                        student = studentDetails,
                        rules = decodedRules,
                        behaviorLog = behaviorLogsByStudent[student.id] ?: emptyList(),
                        quizLog = quizLogsByStudent[student.id] ?: emptyList(),
                        homeworkLog = homeworkLogsByStudent[student.id] ?: emptyList(),
                        isLiveQuizActive = isSessionActive.value ?: false,
                        liveQuizScores = liveQuizScores.value ?: emptyMap(),
                        isLiveHomeworkActive = isSessionActive.value ?: false,
                        liveHomeworkScores = liveHomeworkScores.value ?: emptyMap(),
                        currentMode = currentMode.value ?: "behavior",
                        currentTimeMillis = currentTime,
                        calendar = calendar
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
                        defaultWidth = defaultStyle.width,
                        defaultHeight = defaultStyle.height,
                        defaultBackgroundColor = defaultStyle.backgroundColor,
                        defaultOutlineColor = defaultStyle.outlineColor,
                        defaultTextColor = defaultStyle.textColor,
                        defaultOutlineThickness = defaultStyle.outlineThickness,
                        defaultCornerRadius = defaultStyle.cornerRadius,
                        defaultPadding = defaultStyle.padding,
                        defaultFontFamily = defaultStyle.fontFamily,
                        defaultFontSize = defaultStyle.fontSize,
                        defaultFontColor = defaultStyle.fontColor
                    )
                }
                studentsForDisplay.postValue(studentsWithBehavior)
            }
        }
    }

    private fun parseKeyValueString(input: String): Map<String, String> {
        return input.split(",")
            .mapNotNull {
                val parts = it.split(":", limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
            }
            .toMap()
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
                updateUndoStackState()
            }
        }
    }

    fun redo() {
        if (commandRedoStack.isNotEmpty()) {
            viewModelScope.launch {
                val command = commandRedoStack.pop()
                command.execute()
                commandUndoStack.push(command)
                updateUndoStackState()
            }
        }
    }

    fun selectiveUndo(targetIndex: Int) {
        if (targetIndex < 0 || targetIndex >= commandUndoStack.size) return

        viewModelScope.launch {
            // 1. Undo actions that occurred after the target command
            val commandsToUndoCount = commandUndoStack.size - 1 - targetIndex
            for (i in 0 until commandsToUndoCount) {
                if (commandUndoStack.isEmpty()) break
                val commandToUndo = commandUndoStack.pop()
                try {
                    commandToUndo.undo()
                } catch (e: Exception) {
                    Log.e("SeatingChartViewModel", "Error undoing command during selective undo", e)
                    // Put it back if undo failed? Python does this.
                    commandUndoStack.push(commandToUndo)
                    updateUndoStackState()
                    return@launch
                }
            }

            // 2. The target command is now at the top. Pop it.
            val targetCommand = commandUndoStack.pop()

            // 3. Undo the target command itself
            try {
                targetCommand.undo()
            } catch (e: Exception) {
                Log.e("SeatingChartViewModel", "Error undoing target command during selective undo", e)
                commandUndoStack.push(targetCommand)
                updateUndoStackState()
                return@launch
            }

            // 4. Re-execute the target command
            try {
                targetCommand.execute()
                commandUndoStack.push(targetCommand)
            } catch (e: Exception) {
                Log.e("SeatingChartViewModel", "Error re-executing target command during selective undo", e)
                updateUndoStackState()
                return@launch
            }

            // 5. Invalidate subsequent history
            commandRedoStack.clear()
            updateUndoStackState()

            // Refresh UI
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
    }

    suspend fun exportData(
        context: Context,
        uri: Uri,
        options: com.example.myapplication.data.exporter.ExportOptions
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val allStudentsList = studentDao.getAllStudentsNonLiveData()
        val behaviorLogs = behaviorEventDao.getAllBehaviorEventsList()
        val homeworkLogs = homeworkLogDao.getAllHomeworkLogsList()
        val quizLogs = quizLogDao.getAllQuizLogsList()
        val studentGroups = studentGroupDao.getAllStudentGroupsList()
        val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()
        val customHomeworkTypes = AppDatabase.getDatabase(context).customHomeworkTypeDao().getAllCustomHomeworkTypesList()
        val customHomeworkStatuses = AppDatabase.getDatabase(context).customHomeworkStatusDao().getAllCustomHomeworkStatusesList()

        val exporter = com.example.myapplication.data.exporter.Exporter(context)
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
            encrypt = options.encrypt
        )
        return@withContext Result.success(Unit)
    }

    suspend fun importStudentsFromExcel(context: Context, uri: Uri): Result<Int> {
        return com.example.myapplication.util.ExcelImportUtil.importStudentsFromExcel(uri, context, repository)
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            Importer(
                context,
                AppDatabase.getDatabase(context),
                appPreferencesRepository.encryptDataFilesFlow
            ).importData(uri)
        }
    }

    fun importFromPythonAssets(context: Context) {
        viewModelScope.launch {
            val importer = Importer(
                context,
                AppDatabase.getDatabase(context),
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

            val moves = mutableListOf<ItemMove>()
            when (alignment) {
                "top" -> {
                    val topY = selectedUiItems.minOf { it.yPosition.value }
                    selectedStudents.forEach { student ->
                        if (student.yPosition != topY) {
                            selectedUiItems.find { it.id == student.id.toInt() }?.yPosition?.value = topY
                            moves.add(ItemMove(student.id, ItemType.STUDENT, student.xPosition, student.yPosition, student.xPosition, topY, student = student))
                        }
                    }
                }
                "bottom" -> {
                    val bottomY = selectedUiItems.maxOf { it.yPosition.value + it.displayHeight.value.value }
                    selectedStudents.forEach { student ->
                        val uiItem = selectedUiItems.find { it.id == student.id.toInt() } ?: return@forEach
                        val newY = bottomY - uiItem.displayHeight.value.value
                        if (student.yPosition != newY) {
                            uiItem.yPosition.value = newY
                            moves.add(ItemMove(student.id, ItemType.STUDENT, student.xPosition, student.yPosition, student.xPosition, newY, student = student))
                        }
                    }
                }
                "left" -> {
                    val leftX = selectedUiItems.minOf { it.xPosition.value }
                    selectedStudents.forEach { student ->
                        if (student.xPosition != leftX) {
                            selectedUiItems.find { it.id == student.id.toInt() }?.xPosition?.value = leftX
                            moves.add(ItemMove(student.id, ItemType.STUDENT, student.xPosition, student.yPosition, leftX, student.yPosition, student = student))
                        }
                    }
                }
                "right" -> {
                    val rightX = selectedUiItems.maxOf { it.xPosition.value + it.displayWidth.value.value }
                    selectedStudents.forEach { student ->
                        val uiItem = selectedUiItems.find { it.id == student.id.toInt() } ?: return@forEach
                        val newX = rightX - uiItem.displayWidth.value.value
                        if (student.xPosition != newX) {
                            uiItem.xPosition.value = newX
                            moves.add(ItemMove(student.id, ItemType.STUDENT, student.xPosition, student.yPosition, newX, student.yPosition, student = student))
                        }
                    }
                }
            }
            if (moves.isNotEmpty()) {
                executeCommand(MoveItemsCommand(this@SeatingChartViewModel, moves))
            }
        }
    }

    fun distributeSelectedItems(distribution: String) {
        viewModelScope.launch {
            val selectedUiItems = studentsForDisplay.value?.filter { selectedStudentIds.value?.contains(it.id) == true }?.toMutableList()
            if (selectedUiItems.isNullOrEmpty() || selectedUiItems.size < 2) return@launch
            val selectedStudents = allStudents.value?.filter { selectedStudentIds.value?.contains(it.id.toInt()) == true } ?: return@launch

            val moves = mutableListOf<ItemMove>()
            when (distribution) {
                "horizontal" -> {
                    selectedUiItems.sortBy { it.xPosition.value }
                    val minX = selectedUiItems.first().xPosition.value
                    val maxX = selectedUiItems.last().xPosition.value + selectedUiItems.last().displayWidth.value.value
                    val totalWidth = selectedUiItems.sumOf { it.displayWidth.value.value.toDouble() }.toFloat()
                    val spacing = if (selectedUiItems.size > 1) (maxX - minX - totalWidth) / (selectedUiItems.size - 1) else 0f
                    var currentX = minX
                    selectedUiItems.forEach { uiItem ->
                        val student = selectedStudents.find { it.id == uiItem.id.toLong() } ?: return@forEach
                        if (student.xPosition != currentX) {
                            uiItem.xPosition.value = currentX
                            moves.add(ItemMove(student.id, ItemType.STUDENT, student.xPosition, student.yPosition, currentX, student.yPosition, student = student))
                        }
                        currentX += uiItem.displayWidth.value.value + spacing
                    }
                }
                "vertical" -> {
                    selectedUiItems.sortBy { it.yPosition.value }
                    val minY = selectedUiItems.first().yPosition.value
                    val maxY = selectedUiItems.last().yPosition.value + selectedUiItems.last().displayHeight.value.value
                    val totalHeight = selectedUiItems.sumOf { it.displayHeight.value.value.toDouble() }.toFloat()
                    val spacing = if (selectedUiItems.size > 1) (maxY - minY - totalHeight) / (selectedUiItems.size - 1) else 0f
                    var currentY = minY
                    selectedUiItems.forEach { uiItem ->
                        val student = selectedStudents.find { it.id == uiItem.id.toLong() } ?: return@forEach
                        if (student.yPosition != currentY) {
                            uiItem.yPosition.value = currentY
                            moves.add(ItemMove(student.id, ItemType.STUDENT, student.xPosition, student.yPosition, student.xPosition, currentY, student = student))
                        }
                        currentY += uiItem.displayHeight.value.value + spacing
                    }
                }
            }
            if (moves.isNotEmpty()) {
                executeCommand(MoveItemsCommand(this@SeatingChartViewModel, moves))
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

    // Guide operations
    fun addGuide(type: GuideType) {
        viewModelScope.launch {
            val command = AddGuideCommand(this@SeatingChartViewModel, Guide(type = type, position = 0f))
            executeCommand(command)
        }
    }

    fun updateGuidePosition(guide: Guide, newPosition: Float) {
        viewModelScope.launch {
            val command = MoveGuideCommand(this@SeatingChartViewModel, guide, guide.position, newPosition)
            executeCommand(command)
        }
    }

    fun deleteGuide(guide: Guide) {
        viewModelScope.launch {
            val command = DeleteGuideCommand(this@SeatingChartViewModel, guide)
            executeCommand(command)
        }
    }

    suspend fun internalAddGuide(guide: Guide): Long = withContext(Dispatchers.IO) {
        guideDao.insert(guide)
    }

    suspend fun internalUpdateGuide(guide: Guide) = withContext(Dispatchers.IO) {
        guideDao.update(guide)
    }

    suspend fun internalDeleteGuide(guide: Guide) = withContext(Dispatchers.IO) {
        guideDao.delete(guide)
    }

    // Layout operations
    fun saveLayout(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val studentLayouts = allStudents.value?.map { student ->
            StudentLayout(id = student.id, x = student.xPosition, y = student.yPosition)
        } ?: emptyList()

        val furnitureLayouts = allFurniture.value?.map { furniture ->
            FurnitureLayout(id = furniture.id, x = furniture.xPosition, y = furniture.yPosition)
        } ?: emptyList()

        val layoutData = LayoutData(students = studentLayouts, furniture = furnitureLayouts)
        val layoutDataJson = Json.encodeToString(layoutData)

        val layout = LayoutTemplate(name = name, layoutDataJson = layoutDataJson)
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

    suspend fun internalLoadLayout(layout: LayoutTemplate) = withContext(Dispatchers.IO) {
        try {
            val layoutData = Json.decodeFromString<LayoutData>(layout.layoutDataJson)

            layoutData.students.forEach { studentLayout ->
                studentDao.updatePosition(studentLayout.id, studentLayout.x, studentLayout.y)
            }

            layoutData.furniture.forEach { furnitureLayout ->
                furnitureDao.updatePosition(furnitureLayout.id.toLong(), furnitureLayout.x, furnitureLayout.y)
            }
        } catch (e: Exception) {
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
    }

    suspend fun internalUpdateAll(students: List<Student>, furniture: List<Furniture>) = withContext(Dispatchers.IO) {
        if (students.isNotEmpty()) studentDao.updateAll(students)
        if (furniture.isNotEmpty()) furnitureDao.updateAll(furniture)
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
        updateUndoStackState()
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

    fun handleOnStop(context: Context) {
        viewModelScope.launch {
            val autoSendOnClose: Boolean = appPreferencesRepository.autoSendEmailOnCloseFlow.first()
            if (autoSendOnClose) {
                val email: String = appPreferencesRepository.defaultEmailAddressFlow.first()
                if (email.isNotBlank()) {
                    val exportOptions = pendingExportOptions ?: com.example.myapplication.data.exporter.ExportOptions()
                    val exportOptionsJson = Json.encodeToString(exportOptions)
                    val workRequest = OneTimeWorkRequestBuilder<EmailWorker>()
                        .setInputData(
                            workDataOf(
                                "request_type" to "on_stop_export",
                                "email_address" to securityUtil.encrypt(email),
                                "export_options" to securityUtil.encrypt(exportOptionsJson)
                            )
                        )
                        .build()
                    WorkManager.getInstance(context).enqueue(workRequest)
                }
            }
        }
    }
}
