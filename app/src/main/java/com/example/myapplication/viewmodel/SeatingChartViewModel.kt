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
import com.example.myapplication.commands.CompositeCommand
import com.example.myapplication.commands.Command
import com.example.myapplication.commands.DeleteFurnitureCommand
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
import com.example.myapplication.ui.model.ChartItemId
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.ui.model.toStudentUiItem
import com.example.myapplication.ui.model.updateStudentUiItem
import com.example.myapplication.ui.model.toUiItem
import com.example.myapplication.util.CollisionDetector
import com.example.myapplication.util.ConditionalFormattingEngine
import com.example.myapplication.util.DecodedConditionalFormattingRule
import com.example.myapplication.util.FormattingTimeContext
import com.example.myapplication.util.EmailWorker
import com.example.myapplication.util.StringSimilarity
import com.example.myapplication.labs.ghost.GhostCognitiveEngine
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

/**
 * SeatingChartViewModel: The central architectural hub for the Seating Chart application.
 *
 * This ViewModel coordinates several complex subsystems:
 * 1. **Reactive Data Pipeline**: Synchronizes state between the Room database (via DAOs),
 *    User Preferences (via [AppPreferencesRepository] and DataStore), and in-memory session data.
 * 2. **Undo/Redo System**: Implements the Command pattern to provide a robust, multi-step
 *    history for all layout and logging actions.
 * 3. **Ghost Lab Experiments**: Integrates advanced R&D features like the [GhostCognitiveEngine]
 *    for automated layout optimization and Predictive HUD data generation.
 * 4. **Display Optimization**: Manages a high-performance transformation of raw database entities
 *    into [StudentUiItem]s, utilizing memoization and item-level caching to ensure a fluid 60fps
 *    experience during seating chart interactions.
 */
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

    /** Live stream of all students in the database. */
    val allStudents: LiveData<List<Student>>

    /** Live stream of student details optimized for display (includes initials and aggregate counts). */
    val allStudentsForDisplay: LiveData<List<com.example.myapplication.data.StudentDetailsForDisplay>>

    /** Live stream of all furniture items in the current layout. */
    val allFurniture: LiveData<List<Furniture>>

    /** Live stream of all saved layout templates. */
    val allLayoutTemplates: LiveData<List<LayoutTemplate>>

    /** Live stream of all historical behavior events. */
    val allBehaviorEvents: LiveData<List<BehaviorEvent>>

    /** Live stream of all historical homework logs. */
    val allHomeworkLogs: LiveData<List<HomeworkLog>>

    /** Live stream of all historical quiz logs. */
    val allQuizLogs: LiveData<List<QuizLog>>

    /** Live stream of all active conditional formatting rules. */
    val allRules: LiveData<List<com.example.myapplication.data.ConditionalFormattingRule>>

    /** Reactive stream of visual guides (horizontal/vertical) currently on the canvas. */
    val allGuides: StateFlow<List<Guide>> = guideDao.getAllGuides()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * The primary observable state for the Seating Chart UI.
     * This [MediatorLiveData] aggregates multiple data sources (students, logs, rules, sessions)
     * and triggers a high-performance transformation via [updateTrigger].
     */
    val studentsForDisplay = MediatorLiveData<List<StudentUiItem>>()

    /**
     * Observable state for furniture items, used to render desks and other classroom objects.
     */
    val furnitureForDisplay = MediatorLiveData<List<FurnitureUiItem>>()

    /** Live stream of all available homework templates. */
    val allHomeworkTemplates: LiveData<List<HomeworkTemplate>>

    /** Stream of user-defined homework assignment types. */
    val customHomeworkTypes: Flow<List<String>> =
        appPreferencesRepository.homeworkAssignmentTypesListFlow.map { it.toList() }

    /** Stream of user-defined homework status labels. */
    val customHomeworkStatuses: Flow<List<String>> =
        appPreferencesRepository.homeworkStatusesListFlow.map { it.toList() }

    /** Live stream of all available quiz templates. */
    val allQuizTemplates: LiveData<List<QuizTemplate>> = quizTemplateDao.getAll().asLiveData()

    /** Live stream of quiz mark types (e.g., "Correct", "Half Credit"). */
    val quizMarkTypes: LiveData<List<QuizMarkType>>

    /** Live stream of user-defined behavior categories. */
    val allCustomBehaviors: LiveData<List<com.example.myapplication.data.CustomBehavior>>

    /** Live stream of user-defined homework categories. */
    val allCustomHomeworkTypes: LiveData<List<com.example.myapplication.data.CustomHomeworkType>>

    /** Live stream of built-in system behaviors. */
    val allSystemBehaviors: LiveData<List<com.example.myapplication.data.SystemBehavior>>


    /**
     * The history of executed commands.
     * New commands are pushed onto this stack. [undo] pops from here.
     */
    private val commandUndoStack = Stack<Command>()

    /**
     * The history of undone commands.
     * Commands are pushed here during [undo]. [redo] pops from here and reapplies them.
     */
    private val commandRedoStack = Stack<Command>()

    private val _undoStackState = MutableStateFlow<List<Command>>(emptyList())

    /**
     * Exposed state of the undo history, used by the UI to render the history list.
     */
    val undoStackState: StateFlow<List<Command>> = _undoStackState.asStateFlow()

    private val _userPreferences = MutableStateFlow<UserPreferences?>(null)
    val userPreferences: StateFlow<UserPreferences?> = _userPreferences.asStateFlow()

    /**
     * A coordination trigger used to synchronize UI updates.
     * Multiple data sources (Room DAOs, DataStore preferences, and in-memory session changes)
     * emit to this flow. It uses [debounce] to prevent redundant, expensive recalculations
     * during rapid state changes.
     */
    private val updateTrigger = MutableSharedFlow<Unit>(replay = 1)
    private var updateJob: Job? = null

    private fun updateUndoStackState() {
        _undoStackState.value = commandUndoStack.toList()
    }
    /**
     * Stores optimistic student positions during drag operations.
     *
     * This cache is a critical part of the fluid interaction model. When a user drags
     * a student icon, the UI updates its local state immediately. However, the database
     * update is asynchronous. This cache ensures that if a global state update occurs
     * *before* the database write finishes, the icon doesn't "snap back" to its old
     * position.
     *
     * The reconciliation happens in [updateStudentsForDisplay], where student positions
     * from the database are overwritten by these "in-flight" positions if they exist.
     */
    private val pendingStudentPositions = ConcurrentHashMap<Int, Pair<Float, Float>>()

    /**
     * Stores optimistic furniture positions.
     */
    private val pendingFurniturePositions = ConcurrentHashMap<Int, Pair<Float, Float>>()

    // --- Memoization Caches ---
    // These caches prevent redundant O(N log N) or O(N^2) operations during high-frequency
    // UI updates (like dragging). They are invalidated only when their respective
    // source data (LiveData/Flow) actually changes.

    private var memoizedBehaviorEvents: List<BehaviorEvent>? = null
    /** Caches behavior logs grouped by student ID to avoid O(B) filtering inside student loops. */
    private var behaviorLogsByStudentCache: Map<Long, List<BehaviorEvent>> = emptyMap()

    private var memoizedHomeworkLogs: List<HomeworkLog>? = null
    /** Caches homework logs grouped by student ID. */
    private var homeworkLogsByStudentCache: Map<Long, List<HomeworkLog>> = emptyMap()

    private var memoizedQuizLogs: List<QuizLog>? = null
    /** Caches quiz logs grouped by student ID. */
    private var quizLogsByStudentCache: Map<Long, List<QuizLog>> = emptyMap()

    private var memoizedRules: List<ConditionalFormattingRule>? = null
    /** Stores prioritized, decoded rule objects to avoid repetitive JSON parsing. */
    private var decodedRulesCache: List<DecodedConditionalFormattingRule> = emptyList()

    private var memoizedBehaviorInitials: String? = null
    /** Caches the parsed initials mapping for behavior types. */
    private var behaviorInitialsMapCache: Map<String, String> = emptyMap()

    private var memoizedHomeworkInitials: String? = null
    /** Caches the parsed initials mapping for homework assignments. */
    private var homeworkInitialsMapCache: Map<String, String> = emptyMap()

    private var memoizedQuizInitials: String? = null
    /** Caches the parsed initials mapping for quizzes. */
    private var quizInitialsMapCache: Map<String, String> = emptyMap()

    private data class StudentCacheKey(
        val studentDataHash: Int,
        val behaviorLogsIdentity: Int,
        val homeworkLogsIdentity: Int,
        val quizLogsIdentity: Int,
        val rulesIdentity: Int,
        val prefsHash: Int,
        val sessionActive: Boolean,
        val currentMode: String,
        val timeKey: String,
        val lastCleared: Long
    )

    private data class StudentDerivedData(
        val behaviorDescription: List<String>,
        val homeworkDescription: List<String>,
        val quizDescription: List<String>,
        val sessionLogs: List<String>,
        val conditionalFormattingResult: List<Pair<String?, String?>>
    )

    private val studentDerivedDataCache = ConcurrentHashMap<Long, Pair<StudentCacheKey, StudentDerivedData>>()

    /**
     * A persistent cache of [StudentUiItem] instances.
     * Reusing these instances is CRITICAL for Compose performance, as it allows
     * fine-grained updates to internal MutableState fields without triggering
     * full-box recompositions or object allocations during scroll/drag.
     */
    private val studentUiItemCache = ConcurrentHashMap<Int, StudentUiItem>()

    // In-memory session data
    private val sessionQuizLogs = MutableLiveData<List<QuizLog>>(emptyList())
    private val sessionHomeworkLogs = MutableLiveData<List<HomeworkLog>>(emptyList())
    val isSessionActive = MutableLiveData<Boolean>(false)
    val currentMode = MutableLiveData<String>("behavior")
    val liveQuizScores = MutableLiveData<Map<Long, Map<String, Any>>>(emptyMap())
    val liveHomeworkScores = MutableLiveData<Map<Long, Map<String, Any>>>(emptyMap())

    val selectedItemIds = MutableLiveData<Set<ChartItemId>>(emptySet())
    var canvasHeight by mutableStateOf(0)
    var canvasWidth by mutableStateOf(0)
    var pendingExportOptions: com.example.myapplication.data.exporter.ExportOptions? by mutableStateOf(null)


    fun clearSelection() {
        selectedItemIds.value = emptySet()
    }

    private val allGroups: StateFlow<List<com.example.myapplication.data.StudentGroup>> = studentGroupDao.getAllStudentGroups()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Initialize basic LiveData streams from repositories
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


        // Wire up MediatorLiveData sources.
        // Any change in the underlying database or session state triggers the unified update pipeline.
        studentsForDisplay.addSource(allStudents) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allStudentsForDisplay) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allGroups.asLiveData()) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(sessionQuizLogs) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(sessionHomeworkLogs) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(isSessionActive) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allBehaviorEvents) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allHomeworkLogs) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allQuizLogs) { updateTrigger.tryEmit(Unit) }
        studentsForDisplay.addSource(allRules) { updateTrigger.tryEmit(Unit) }

        // Sync user preferences from DataStore.
        viewModelScope.launch {
            appPreferencesRepository.userPreferencesFlow.collect {
                _userPreferences.value = it
                updateTrigger.emit(Unit)
            }
        }

        // The unified update loop.
        // debounced to 100ms to ensure fluid UI even when multiple sources update simultaneously.
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

    /**
     * The core transformation engine for student UI data, optimized for 60fps performance.
     *
     * This method performs a multi-stage process to convert raw [Student] database entities
     * into enriched [StudentUiItem]s. It runs on [Dispatchers.Default] to avoid blocking
     * the main thread during complex calculations.
     *
     * ### Pipeline Stages:
     *
     * #### Stage 1: Data Pre-processing (Memoized)
     * Groups flat log lists by student ID and decodes conditional formatting rules.
     * These operations are O(N) or O(N log N) but are performed only once per update cycle
     * and are bypassed if the underlying data identities haven't changed.
     *
     * #### Stage 2: Per-Student Transformation (Memoized)
     * For each student, this stage:
     * - Filters logs based on "last cleared" timestamps and configurable display timeouts.
     * - Generates human-readable descriptions (Behavior/Homework/Quiz), optionally using initials.
     * - Evaluates the prioritized set of Conditional Formatting rules.
     * - **Optimistic Reconciliation**: Reconciles positions with [pendingStudentPositions] to
     *   ensure drag operations remain smooth.
     *
     * This entire stage is memoized using [studentDerivedDataCache] and a [StudentCacheKey].
     * The cache key tracks student data hashes (excluding volatile positions) and log
     * identities, ensuring that we only re-calculate descriptions or formatting when
     * something "meaningful" has changed.
     *
     * #### Stage 3: State Sync & Identity Preservation
     * Instead of creating new UI objects, the pipeline retrieves existing [StudentUiItem]s
     * from [studentUiItemCache] and updates their internal [androidx.compose.runtime.MutableState]
     * fields via [com.example.myapplication.ui.model.updateStudentUiItem].
     *
     * By preserving object identity, we allow Jetpack Compose to perform highly efficient
     * "diff-and-patch" updates at the property level, minimizing recomposition scope.
     *
     * @param students The list of raw Student entities to transform.
     */
    private fun updateStudentsForDisplay(students: List<Student>) {
        val prefs = _userPreferences.value ?: return
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val studentsForDisplayData = allStudentsForDisplay.value ?: return@withContext
                val studentDetailsMap = studentsForDisplayData.associateBy { it.id }

                // --- Stage 1: Data Pre-processing (Memoized) ---

                // Group flat behavior logs by student ID to optimize O(1) lookup in the student loop.
                val behaviorEvents = allBehaviorEvents.value ?: emptyList()
                if (behaviorEvents !== memoizedBehaviorEvents) {
                    behaviorLogsByStudentCache = behaviorEvents.groupBy { it.studentId }
                    memoizedBehaviorEvents = behaviorEvents
                }
                val behaviorLogsByStudent = behaviorLogsByStudentCache

                val homeworkLogs = allHomeworkLogs.value ?: emptyList()
                if (homeworkLogs !== memoizedHomeworkLogs) {
                    homeworkLogsByStudentCache = homeworkLogs.groupBy { it.studentId }
                    memoizedHomeworkLogs = homeworkLogs
                }
                val homeworkLogsByStudent = homeworkLogsByStudentCache

                val quizLogs = allQuizLogs.value ?: emptyList()
                if (quizLogs !== memoizedQuizLogs) {
                    quizLogsByStudentCache = quizLogs.groupBy { it.studentId }
                    memoizedQuizLogs = quizLogs
                }
                val quizLogsByStudent = quizLogsByStudentCache

                val sessionActive = isSessionActive.value == true
                val sessionQuizLogsGrouped = if (sessionActive) {
                    sessionQuizLogs.value?.groupBy { it.studentId } ?: emptyMap()
                } else {
                    emptyMap()
                }
                val sessionHomeworkLogsGrouped = if (sessionActive) {
                    sessionHomeworkLogs.value?.groupBy { it.studentId } ?: emptyMap()
                } else {
                    emptyMap()
                }

                val groups = allGroups.value ?: emptyList()
                val groupColorMap = groups.associate { it.id to it.color }

                val behaviorLimit = prefs.recentBehaviorIncidentsLimit
                val homeworkLimit = prefs.recentHomeworkLogsLimit
                val quizLimit = prefs.recentLogsLimit
                val maxLogsToDisplay = prefs.maxRecentLogsToDisplay
                val useInitialsForBehavior = prefs.useInitialsForBehavior
                val useInitialsForHomework = prefs.useInitialsForHomework
                val useInitialsForQuiz = prefs.useInitialsForQuiz

                // Memoized preference parsing
                if (prefs.behaviorInitialsMap != memoizedBehaviorInitials) {
                    behaviorInitialsMapCache = parseKeyValueString(prefs.behaviorInitialsMap)
                    memoizedBehaviorInitials = prefs.behaviorInitialsMap
                }
                val behaviorInitialsMap = behaviorInitialsMapCache

                if (prefs.homeworkInitialsMap != memoizedHomeworkInitials) {
                    homeworkInitialsMapCache = parseKeyValueString(prefs.homeworkInitialsMap)
                    memoizedHomeworkInitials = prefs.homeworkInitialsMap
                }
                val homeworkInitialsMap = homeworkInitialsMapCache

                if (prefs.quizInitialsMap != memoizedQuizInitials) {
                    quizInitialsMapCache = parseKeyValueString(prefs.quizInitialsMap)
                    memoizedQuizInitials = prefs.quizInitialsMap
                }
                val quizInitialsMap = quizInitialsMapCache

                val lastClearedTimestamps = prefs.studentLogsLastCleared

                val behaviorDisplayTimeout = prefs.behaviorDisplayTimeout
                val homeworkDisplayTimeout = prefs.homeworkDisplayTimeout
                val quizDisplayTimeout = prefs.quizDisplayTimeout
                val currentTime = System.currentTimeMillis()
                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = currentTime }
                val dayOfWeek = when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
                    java.util.Calendar.MONDAY -> 0
                    java.util.Calendar.TUESDAY -> 1
                    java.util.Calendar.WEDNESDAY -> 2
                    java.util.Calendar.THURSDAY -> 3
                    java.util.Calendar.FRIDAY -> 4
                    java.util.Calendar.SATURDAY -> 5
                    java.util.Calendar.SUNDAY -> 6
                    else -> -1
                }
                val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                val minute = calendar.get(java.util.Calendar.MINUTE)
                val currentTimeString = "${if (hour < 10) "0$hour" else hour}:${if (minute < 10) "0$minute" else minute}"
                val timeContext = FormattingTimeContext(dayOfWeek, currentTimeString)

                // Memoized rule decoding
                val rules = allRules.value ?: emptyList()
                if (rules !== memoizedRules) {
                    decodedRulesCache = ConditionalFormattingEngine.decodeRules(rules)
                    memoizedRules = rules
                }
                val decodedRules = decodedRulesCache

                val defaultStyle = prefs.defaultStudentStyle

                // Clean up caches for deleted students.
                val currentStudentIds = students.mapTo(HashSet(students.size)) { it.id }
                studentUiItemCache.keys.retainAll { it.toLong() in currentStudentIds }
                studentDerivedDataCache.keys.retainAll { it in currentStudentIds }

                // --- Stage 2: Per-Student Transformation ---
                val studentsWithBehavior = students.map { student ->
                    val lastCleared = lastClearedTimestamps[student.id] ?: 0L
                    val studentDataHash = getStudentDataHash(student)
                    val behaviorList = behaviorLogsByStudent[student.id]
                    val homeworkList = homeworkLogsByStudent[student.id]
                    val quizList = quizLogsByStudent[student.id]
                    val currentModeValue = currentMode.value ?: "behavior"

                    val cacheKey = StudentCacheKey(
                        studentDataHash = studentDataHash,
                        behaviorLogsIdentity = System.identityHashCode(behaviorList),
                        homeworkLogsIdentity = System.identityHashCode(homeworkList),
                        quizLogsIdentity = System.identityHashCode(quizList),
                        rulesIdentity = System.identityHashCode(decodedRules),
                        prefsHash = prefs.hashCode(),
                        sessionActive = sessionActive,
                        currentMode = currentModeValue,
                        timeKey = currentTimeString,
                        lastCleared = lastCleared
                    )

                    val cached = studentDerivedDataCache[student.id]
                    val derivedData = if (cached != null && cached.first == cacheKey) {
                        cached.second
                    } else {
                        // 2a. Log Filtering:
                        // Only show logs that haven't been "cleared" by the user and haven't exceeded
                        // the display timeout defined in preferences.
                        val recentEvents = if (student.showLogs) {
                            behaviorList?.filter {
                                it.timestamp > lastCleared && (behaviorDisplayTimeout == 0 || currentTime < it.timestamp + (behaviorDisplayTimeout.toLong() * 3600000L))
                            }?.take(behaviorLimit) ?: emptyList()
                        } else {
                            emptyList()
                        }
                        val recentHomework = if (student.showLogs) {
                            homeworkList?.filter {
                                it.loggedAt > lastCleared && (homeworkDisplayTimeout == 0 || currentTime < it.loggedAt + (homeworkDisplayTimeout.toLong() * 3600000L))
                            }?.take(homeworkLimit) ?: emptyList()
                        } else {
                            emptyList()
                        }
                        val recentQuizzes = if (student.showLogs) {
                            quizList?.filter {
                                it.loggedAt > lastCleared && !it.isComplete && (quizDisplayTimeout == 0 || currentTime < it.loggedAt + (quizDisplayTimeout.toLong() * 3600000L))
                            }?.take(quizLimit) ?: emptyList()
                        } else {
                            emptyList()
                        }

                        // 2b. Description Generation:
                        // Convert raw log entries into display strings, optionally using initials.
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

                        val sessionLogs = if (sessionActive) {
                            val quizLogsSess = sessionQuizLogsGrouped[student.id]?.map { "Quiz: ${it.comment}" } ?: emptyList()
                            val homeworkLogsSess = sessionHomeworkLogsGrouped[student.id]?.map { "${it.assignmentName}: ${it.status}" } ?: emptyList()
                            (quizLogsSess + homeworkLogsSess).take(maxLogsToDisplay)
                        } else {
                            emptyList()
                        }

                        // 2c. Conditional Formatting:
                        // Evaluate the prioritized rule set against the student's current state and history.
                        val studentDetails = studentDetailsMap[student.id]
                        val conditionalFormattingResult = if (studentDetails != null) {
                            ConditionalFormattingEngine.applyConditionalFormattingDecoded(
                                student = studentDetails,
                                rules = decodedRules,
                                behaviorLog = behaviorList ?: emptyList(),
                                quizLog = quizList ?: emptyList(),
                                homeworkLog = homeworkList ?: emptyList(),
                                isLiveQuizActive = sessionActive,
                                liveQuizScores = liveQuizScores.value ?: emptyMap(),
                                isLiveHomeworkActive = sessionActive,
                                liveHomeworkScores = liveHomeworkScores.value ?: emptyMap(),
                                currentMode = currentModeValue,
                                currentTimeMillis = currentTime,
                                timeContext = timeContext
                            )
                        } else {
                            emptyList()
                        }

                        StudentDerivedData(
                            behaviorDescription,
                            homeworkDescription,
                            quizDescription,
                            sessionLogs,
                            conditionalFormattingResult
                        ).also {
                            studentDerivedDataCache[student.id] = cacheKey to it
                        }
                    }

                    val behaviorDescription = derivedData.behaviorDescription
                    val homeworkDescription = derivedData.homeworkDescription
                    val quizDescription = derivedData.quizDescription
                    val sessionLogs = derivedData.sessionLogs
                    val conditionalFormattingResult = derivedData.conditionalFormattingResult

                    // 2d. Optimistic Reconciliation:
                    // If the student is currently being dragged, use the local "pending" position
                    // to ensure immediate UI feedback before the database update completes.
                    val pendingPos = pendingStudentPositions[student.id.toInt()]
                    var studentForUi = student
                    if (pendingPos != null) {
                        if (abs(student.xPosition - pendingPos.first) < 0.1f && abs(student.yPosition - pendingPos.second) < 0.1f) {
                            pendingStudentPositions.remove(student.id.toInt())
                        } else {
                            studentForUi = student.copy(xPosition = pendingPos.first, yPosition = pendingPos.second)
                        }
                    }

                    // --- Stage 3: State Sync ---

                    // Reuse or create UI item instances.
                    // Reusing instances allows Compose to perform optimized "diff-and-patch"
                    // updates to individual fields.
                    val existingItem = studentUiItemCache[student.id.toInt()]
                    if (existingItem != null) {
                        studentForUi.updateStudentUiItem(
                            item = existingItem,
                            recentBehaviorDescription = behaviorDescription,
                            recentHomeworkDescription = homeworkDescription,
                            recentQuizDescription = quizDescription,
                            sessionLogText = sessionLogs,
                            groupColor = groupColorMap[student.groupId],
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
                        existingItem
                    } else {
                        val newItem = studentForUi.toStudentUiItem(
                            recentBehaviorDescription = behaviorDescription,
                            recentHomeworkDescription = homeworkDescription,
                            recentQuizDescription = quizDescription,
                            sessionLogText = sessionLogs,
                            groupColor = groupColorMap[student.groupId],
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
                        studentUiItemCache[student.id.toInt()] = newItem
                        newItem
                    }
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


    /**
     * Reverses the most recent action in the command history.
     * The command is moved from [commandUndoStack] to [commandRedoStack].
     */
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

    /**
     * Re-applies the most recently undone action.
     * The command is moved from [commandRedoStack] back to [commandUndoStack].
     */
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

    /**
     * Performs a non-linear history manipulation to "re-execute" or "undo" a specific
     * action from the middle of the history stack.
     *
     * Unlike a standard linear undo (which only affects the top of the stack), Selective Undo
     * allows a user to "toggle" the effect of a historical action without losing their
     * entire recent work. This is particularly useful for correcting a log entry made
     * minutes ago without undoing subsequent layout changes.
     *
     * ### Selective Undo Algorithm:
     *
     * 1. **Rollback**: Temporarily pops and undos all commands from the top of the
     *    [commandUndoStack] down to (but not including) the target index. This reverts
     *    the application state to exactly how it was immediately after the target
     *    command was first executed.
     * 2. **Isolate**: Pops and undos the target command itself.
     * 3. **Re-branch**: Re-executes the target command. This "refreshes" its effect
     *    in the database. In future iterations, this step could be expanded to support
     *    true "toggling" or "editing" of historical actions.
     * 4. **Invalidate Future**: Clears the [commandRedoStack] because the historical
     *    "future" (the commands we rolled back in step 1) may have depended on the
     *    state we just mutated.
     *
     * **Note:** To maintain data integrity, all actions that occurred AFTER the target
     * index are permanently discarded. This "re-branches" history from the point of
     * the target action.
     *
     * @param targetIndex The index of the command in [commandUndoStack] to manipulate.
     */
    fun selectiveUndo(targetIndex: Int) {
        if (targetIndex < 0 || targetIndex >= commandUndoStack.size) return

        viewModelScope.launch {
            // 1. Undo all actions that occurred AFTER the target command.
            // This brings the system back to the state immediately following the target action.
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

            // 2. The target command is now at the top of the stack.
            val targetCommand = commandUndoStack.pop()

            // 3. Undo the target command itself.
            try {
                targetCommand.undo()
            } catch (e: Exception) {
                Log.e("SeatingChartViewModel", "Error undoing target command during selective undo", e)
                commandUndoStack.push(targetCommand)
                updateUndoStackState()
                return@launch
            }

            // 4. Re-execute the target command.
            // In some contexts, this might be a "toggle" or "edit", but standard implementation
            // just reapplies it.
            try {
                targetCommand.execute()
                commandUndoStack.push(targetCommand)
            } catch (e: Exception) {
                Log.e("SeatingChartViewModel", "Error re-executing target command during selective undo", e)
                updateUndoStackState()
                return@launch
            }

            // 5. Invalidate all subsequent history.
            // We cannot reliably "redo" actions that depended on the state we just modified.
            commandRedoStack.clear()
            updateUndoStackState()

            // Refresh UI
            updateStudentsForDisplay(allStudents.value ?: emptyList())
        }
    }

    /**
     * Exports the current application data (students, logs, groups) to an Excel file.
     *
     * This method gathers data from all relevant Room DAOs and delegates the actual
     * Excel generation to the [com.example.myapplication.data.exporter.Exporter] utility.
     *
     * @param context Application context.
     * @param uri The URI where the generated file should be saved.
     * @param options Configuration for what data to include (e.g., summary sheets, encryption).
     * @return A [Result] indicating success or failure of the export operation.
     */
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
        return com.example.myapplication.util.ExcelImportUtil.importStudentsFromExcel(uri, context, repository, studentGroupDao)
    }

    /**
     * Imports classroom data from a JSON backup file.
     *
     * @param context Application context.
     * @param uri The URI of the JSON backup file.
     */
    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            Importer(
                context,
                AppDatabase.getDatabase(context),
                appPreferencesRepository.encryptDataFilesFlow
            ).importData(uri)
        }
    }

    /**
     * Special import task that loads initial classroom data from bundled assets.
     * Primarily used to provide sample data or sync with the Python desktop application.
     *
     * @param context Application context.
     */
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

    suspend fun internalAddStudent(student: Student): Long {
        return withContext(Dispatchers.IO) {
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

    fun deleteSelectedItems(itemIds: Set<ChartItemId>) {
        viewModelScope.launch {
            val commands = mutableListOf<Command>()

            val studentIds = itemIds.filter { it.type == ItemType.STUDENT }.map { it.id }
            val studentsToDelete = allStudents.value?.filter { studentIds.contains(it.id.toInt()) } ?: emptyList()
            commands.addAll(studentsToDelete.map { DeleteStudentCommand(this@SeatingChartViewModel, it) })

            val furnitureIds = itemIds.filter { it.type == ItemType.FURNITURE }.map { it.id }
            val furnitureToDelete = allFurniture.value?.filter { furnitureIds.contains(it.id.toInt()) } ?: emptyList()
            commands.addAll(furnitureToDelete.map { DeleteFurnitureCommand(this@SeatingChartViewModel, it) })

            if (commands.isEmpty()) return@launch

            val compositeCommand = CompositeCommand(commands, "Delete ${commands.size} item(s)")
            executeCommand(compositeCommand)
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

    fun changeBoxSize(itemIds: Set<ChartItemId>, width: Int, height: Int) {
        viewModelScope.launch {
            itemIds.forEach { itemId ->
                if (itemId.type == ItemType.STUDENT) {
                    val student = allStudents.value?.find { it.id.toInt() == itemId.id }
                    student?.let {
                        val updatedStudent = it.copy(customWidth = width, customHeight = height)
                        val command = UpdateStudentCommand(this@SeatingChartViewModel, it, updatedStudent)
                        executeCommand(command)
                    }
                } else {
                    val furniture = allFurniture.value?.find { it.id == itemId.id }
                    furniture?.let {
                        val updatedFurniture = it.copy(width = width, height = height)
                        val command = UpdateFurnitureCommand(this@SeatingChartViewModel, it, updatedFurniture)
                        executeCommand(command)
                    }
                }
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

    /**
     * Aligns all currently selected students and furniture based on a specified anchor point.
     *
     * This method calculates the bounding box of all selected items and shifts their
     * positions to match the specified edge (top, bottom, left, right) or center line.
     * All movements are encapsulated into a single [MoveItemsCommand] for undo/redo support.
     *
     * @param alignment The alignment type: "top", "bottom", "left", "right", "center_h", or "center_v".
     */
    fun alignSelectedItems(alignment: String) {
        viewModelScope.launch {
            val selectedIds = selectedItemIds.value ?: emptySet()
            if (selectedIds.size < 2) return@launch

            val itemsToAlign = mutableListOf<AlignmentItem>()

            selectedIds.forEach { itemId ->
                if (itemId.type == ItemType.STUDENT) {
                    studentsForDisplay.value?.find { it.id == itemId.id }?.let { ui ->
                        val student = allStudents.value?.find { it.id == itemId.id.toLong() }
                        itemsToAlign.add(AlignmentItem(itemId.id.toLong(), ItemType.STUDENT, ui.xPosition.value, ui.yPosition.value, ui.displayWidth.value.value, ui.displayHeight.value.value, student = student))
                    }
                } else {
                    furnitureForDisplay.value?.find { it.id == itemId.id }?.let { ui ->
                        val furniture = allFurniture.value?.find { it.id == itemId.id }
                        itemsToAlign.add(AlignmentItem(itemId.id.toLong(), ItemType.FURNITURE, ui.xPosition, ui.yPosition, ui.displayWidth.value, ui.displayHeight.value, furniture = furniture))
                    }
                }
            }

            if (itemsToAlign.size < 2) return@launch

            val moves = mutableListOf<ItemMove>()
            val targetCoord: Float

            when (alignment) {
                "top" -> {
                    targetCoord = itemsToAlign.minOf { it.y }
                    itemsToAlign.forEach { item ->
                        if (item.y != targetCoord) {
                            moves.add(ItemMove(item.id, item.type, item.x, item.y, item.x, targetCoord, student = item.student, furniture = item.furniture))
                            updateUiItemPosition(item.id, item.type, item.x, targetCoord)
                        }
                    }
                }
                "bottom" -> {
                    targetCoord = itemsToAlign.maxOf { it.y + it.h }
                    itemsToAlign.forEach { item ->
                        val newY = targetCoord - item.h
                        if (item.y != newY) {
                            moves.add(ItemMove(item.id, item.type, item.x, item.y, item.x, newY, student = item.student, furniture = item.furniture))
                            updateUiItemPosition(item.id, item.type, item.x, newY)
                        }
                    }
                }
                "left" -> {
                    targetCoord = itemsToAlign.minOf { it.x }
                    itemsToAlign.forEach { item ->
                        if (item.x != targetCoord) {
                            moves.add(ItemMove(item.id, item.type, item.x, item.y, targetCoord, item.y, student = item.student, furniture = item.furniture))
                            updateUiItemPosition(item.id, item.type, targetCoord, item.y)
                        }
                    }
                }
                "right" -> {
                    targetCoord = itemsToAlign.maxOf { it.x + it.w }
                    itemsToAlign.forEach { item ->
                        val newX = targetCoord - item.w
                        if (item.x != newX) {
                            moves.add(ItemMove(item.id, item.type, item.x, item.y, newX, item.y, student = item.student, furniture = item.furniture))
                            updateUiItemPosition(item.id, item.type, newX, item.y)
                        }
                    }
                }
                "center_h" -> {
                    val minX = itemsToAlign.minOf { it.x }
                    val maxX = itemsToAlign.maxOf { it.x + it.w }
                    targetCoord = (minX + maxX) / 2f
                    itemsToAlign.forEach { item ->
                        val newX = targetCoord - (item.w / 2f)
                        if (item.x != newX) {
                            moves.add(ItemMove(item.id, item.type, item.x, item.y, newX, item.y, student = item.student, furniture = item.furniture))
                            updateUiItemPosition(item.id, item.type, newX, item.y)
                        }
                    }
                }
                "center_v" -> {
                    val minY = itemsToAlign.minOf { it.y }
                    val maxY = itemsToAlign.maxOf { it.y + it.h }
                    targetCoord = (minY + maxY) / 2f
                    itemsToAlign.forEach { item ->
                        val newY = targetCoord - (item.h / 2f)
                        if (item.y != newY) {
                            moves.add(ItemMove(item.id, item.type, item.x, item.y, item.x, newY, student = item.student, furniture = item.furniture))
                            updateUiItemPosition(item.id, item.type, item.x, newY)
                        }
                    }
                }
            }
            if (moves.isNotEmpty()) {
                executeCommand(MoveItemsCommand(this@SeatingChartViewModel, moves))
            }
        }
    }

    private fun updateUiItemPosition(id: Long, type: ItemType, newX: Float, newY: Float) {
        if (type == ItemType.STUDENT) {
            studentsForDisplay.value?.find { it.id == id.toInt() }?.let {
                it.xPosition.value = newX
                it.yPosition.value = newY
            }
        } else {
            // Furniture positions in FurnitureUiItem are currently not MutableState, but we can update them in the LiveData if needed.
            // However, the command will update the DB which will trigger a refresh.
        }
    }

    private data class AlignmentItem(
        val id: Long,
        val type: ItemType,
        val x: Float,
        val y: Float,
        val w: Float,
        val h: Float,
        val student: Student? = null,
        val furniture: Furniture? = null
    )

    /**
     * Evenly distributes the currently selected items horizontally or vertically
     * between the two outermost items in the selection.
     *
     * Items are sorted by their position along the chosen axis, and then moved
     * so that the gap between each item (accounting for their individual widths/heights)
     * is equal.
     *
     * @param distribution The direction of distribution: "horizontal" or "vertical".
     */
    fun distributeSelectedItems(distribution: String) {
        viewModelScope.launch {
            val selectedIds = selectedItemIds.value ?: emptySet()
            if (selectedIds.size < 2) return@launch

            val itemsToDistribute = mutableListOf<AlignmentItem>()
            selectedIds.forEach { itemId ->
                if (itemId.type == ItemType.STUDENT) {
                    studentsForDisplay.value?.find { it.id == itemId.id }?.let { ui ->
                        val student = allStudents.value?.find { it.id == itemId.id.toLong() }
                        itemsToDistribute.add(AlignmentItem(itemId.id.toLong(), ItemType.STUDENT, ui.xPosition.value, ui.yPosition.value, ui.displayWidth.value.value, ui.displayHeight.value.value, student = student))
                    }
                } else {
                    furnitureForDisplay.value?.find { it.id == itemId.id }?.let { ui ->
                        val furniture = allFurniture.value?.find { it.id == itemId.id }
                        itemsToDistribute.add(AlignmentItem(itemId.id.toLong(), ItemType.FURNITURE, ui.xPosition, ui.yPosition, ui.displayWidth.value, ui.displayHeight.value, furniture = furniture))
                    }
                }
            }

            if (itemsToDistribute.size < 2) return@launch

            val moves = mutableListOf<ItemMove>()
            when (distribution) {
                "horizontal" -> {
                    itemsToDistribute.sortBy { it.x }
                    val minX = itemsToDistribute.first().x
                    val maxX = itemsToDistribute.last().x + itemsToDistribute.last().w
                    val totalWidth = itemsToDistribute.sumOf { it.w.toDouble() }.toFloat()
                    val spacing = if (itemsToDistribute.size > 1) (maxX - minX - totalWidth) / (itemsToDistribute.size - 1) else 0f
                    var currentX = minX
                    itemsToDistribute.forEach { item ->
                        if (item.x != currentX) {
                            moves.add(ItemMove(item.id, item.type, item.x, item.y, currentX, item.y, student = item.student, furniture = item.furniture))
                            updateUiItemPosition(item.id, item.type, currentX, item.y)
                        }
                        currentX += item.w + spacing
                    }
                }
                "vertical" -> {
                    itemsToDistribute.sortBy { it.y }
                    val minY = itemsToDistribute.first().y
                    val maxY = itemsToDistribute.last().y + itemsToDistribute.last().h
                    val totalHeight = itemsToDistribute.sumOf { it.h.toDouble() }.toFloat()
                    val spacing = if (itemsToDistribute.size > 1) (maxY - minY - totalHeight) / (itemsToDistribute.size - 1) else 0f
                    var currentY = minY
                    itemsToDistribute.forEach { item ->
                        if (item.y != currentY) {
                            moves.add(ItemMove(item.id, item.type, item.x, item.y, item.x, currentY, student = item.student, furniture = item.furniture))
                            updateUiItemPosition(item.id, item.type, item.x, currentY)
                        }
                        currentY += item.h + spacing
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

    suspend fun internalAddFurniture(furniture: Furniture): Long {
        return withContext(Dispatchers.IO) {
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

    suspend fun internalDeleteFurniture(furniture: Furniture) {
        withContext(Dispatchers.IO) {
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

    suspend fun internalUpdateFurniturePosition(furnitureId: Long, newX: Float, newY: Float) {
        withContext(Dispatchers.IO) {
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
            StudentLayout(
                id = student.id,
                x = student.xPosition,
                y = student.yPosition,
                firstName = student.firstName,
                lastName = student.lastName,
                nickname = student.nickname ?: ""
            )
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
        val currentStudents = studentDao.getAllStudentsNonLiveData()
        val matchedStudentIds = mutableSetOf<Long>()

        try {
            val layoutData = Json.decodeFromString<LayoutData>(layout.layoutDataJson)

            layoutData.students.forEach { studentLayout ->
                var targetStudentId: Long? = null

                // Stage 1: ID Match
                val idMatch = currentStudents.find { it.id == studentLayout.id }
                if (idMatch != null) {
                    targetStudentId = idMatch.id
                }

                // Stage 2: Exact Name Match (if ID fails)
                if (targetStudentId == null && studentLayout.firstName.isNotEmpty()) {
                    val nameMatch = currentStudents.find {
                        it.firstName.equals(studentLayout.firstName, ignoreCase = true) &&
                                it.lastName.equals(studentLayout.lastName, ignoreCase = true) &&
                                !matchedStudentIds.contains(it.id)
                    }
                    if (nameMatch != null) {
                        targetStudentId = nameMatch.id
                    }
                }

                // Stage 3: Fuzzy Name Match (threshold >= 0.85)
                if (targetStudentId == null && studentLayout.firstName.isNotEmpty()) {
                    val templateFullName = "${studentLayout.firstName} ${studentLayout.lastName}".trim()
                    val fuzzyMatch = currentStudents
                        .filter { !matchedStudentIds.contains(it.id) }
                        .map { student ->
                            val currentFullName = "${student.firstName} ${student.lastName}".trim()
                            val similarity = StringSimilarity.nameSimilarityRatio(templateFullName, currentFullName)
                            student to similarity
                        }
                        .filter { it.second >= 0.85f }
                        .maxByOrNull { it.second }
                        ?.first

                    if (fuzzyMatch != null) {
                        targetStudentId = fuzzyMatch.id
                    }
                }

                if (targetStudentId != null) {
                    studentDao.updatePosition(targetStudentId, studentLayout.x, studentLayout.y)
                    matchedStudentIds.add(targetStudentId)
                }
            }

            layoutData.furniture.forEach { furnitureLayout ->
                furnitureDao.updatePosition(furnitureLayout.id.toLong(), furnitureLayout.x, furnitureLayout.y)
            }
        } catch (e: Exception) {
            // Fallback for older JSON format or parsing errors
            val layoutData = JSONObject(layout.layoutDataJson)
            val studentPositions = JSONArray(layoutData.getString("students"))
            for (i in 0 until studentPositions.length()) {
                val pos = studentPositions.getJSONObject(i)
                val id = pos.getLong("id")
                val x = pos.getDouble("x").toFloat()
                val y = pos.getDouble("y").toFloat()

                // For legacy format, we only have ID
                if (currentStudents.any { it.id == id }) {
                    studentDao.updatePosition(id, x, y)
                }
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

    suspend fun internalAddBehaviorEvent(event: BehaviorEvent): Long {
        return withContext(Dispatchers.IO) {
            val id = repository.insertBehaviorEvent(event)
            withContext(Dispatchers.Main) {
                updateStudentsForDisplay(allStudents.value ?: emptyList())
            }
            id
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

    suspend fun internalSaveQuizLog(log: QuizLog): Long {
        return withContext(Dispatchers.IO) {
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

    suspend fun internalAddHomeworkLog(log: HomeworkLog): Long {
        return withContext(Dispatchers.IO) {
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
                "Quiz log added/updated in session for student ${quizLog.studentId.toString().takeLast(4)}."
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
                "Homework log added/updated in session for student ${homeworkLog.studentId.toString().takeLast(4)}."
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

    /**
     * Triggers the [GhostCognitiveEngine] to automatically optimize the classroom layout.
     *
     * The engine uses a force-directed graph algorithm where student behavioral history
     * (e.g., negative interactions) creates repulsion forces, and group membership
     * creates attraction forces. The resulting layout is applied as a single
     * [MoveItemsCommand], allowing the teacher to undo the entire reorganization if desired.
     */
    fun runCognitiveOptimization() {
        viewModelScope.launch {
            val students = allStudents.value ?: return@launch
            val behaviorLogs = allBehaviorEvents.value ?: emptyList()

            val optimizedPoints = withContext(Dispatchers.Default) {
                GhostCognitiveEngine.optimizeLayout(students, behaviorLogs, 4000, 4000)
            }

            val moves = students.mapNotNull { student ->
                val newPoint = optimizedPoints[student.id] ?: return@mapNotNull null
                if (student.xPosition != newPoint.x || student.yPosition != newPoint.y) {
                    ItemMove(
                        id = student.id,
                        itemType = ItemType.STUDENT,
                        oldX = student.xPosition,
                        oldY = student.yPosition,
                        newX = newPoint.x,
                        newY = newPoint.y,
                        student = student
                    )
                } else {
                    null
                }
            }

            if (moves.isNotEmpty()) {
                executeCommand(MoveItemsCommand(this@SeatingChartViewModel, moves))
            }
        }
    }

    /**
     * Handles application lifecycle termination logic, such as automatic data backup.
     *
     * If the "Auto-send email on close" preference is enabled, this method triggers
     * a background [EmailWorker] to export the current session data and email it to
     * the configured default address.
     *
     * @param context The application context required to enqueue WorkManager requests.
     */
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

    /**
     * Computes a stable hash code for a [Student] entity, excluding its volatile positioning fields.
     * This is used to detect "real" data changes for caching purposes.
     */
    private fun getStudentDataHash(student: Student): Int {
        var result = student.id.hashCode()
        result = 31 * result + student.firstName.hashCode()
        result = 31 * result + student.lastName.hashCode()
        result = 31 * result + (student.nickname?.hashCode() ?: 0)
        result = 31 * result + student.gender.hashCode()
        result = 31 * result + (student.groupId?.hashCode() ?: 0)
        result = 31 * result + (student.initials?.hashCode() ?: 0)
        result = 31 * result + (student.customWidth ?: 0)
        result = 31 * result + (student.customHeight ?: 0)
        result = 31 * result + (student.customBackgroundColor?.hashCode() ?: 0)
        result = 31 * result + (student.customOutlineColor?.hashCode() ?: 0)
        result = 31 * result + (student.customTextColor?.hashCode() ?: 0)
        result = 31 * result + (student.customOutlineThickness ?: 0)
        result = 31 * result + (student.customCornerRadius ?: 0)
        result = 31 * result + (student.customPadding ?: 0)
        result = 31 * result + (student.customFontFamily?.hashCode() ?: 0)
        result = 31 * result + (student.customFontSize ?: 0)
        result = 31 * result + (student.customFontColor?.hashCode() ?: 0)
        result = 31 * result + (student.temporaryTask?.hashCode() ?: 0)
        result = 31 * result + student.showLogs.hashCode()
        return result
    }
}
