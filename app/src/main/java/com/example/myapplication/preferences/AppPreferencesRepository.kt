package com.example.myapplication.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.data.DefaultStudentStyle
import com.example.myapplication.data.EmailSchedule
import com.example.myapplication.data.SmtpSettings
import com.example.myapplication.util.SecurityUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/** Default width for a student's visual representation in DP. */
const val DEFAULT_STUDENT_BOX_WIDTH_DP = 120
/** Default height for a student's visual representation in DP. */
const val DEFAULT_STUDENT_BOX_HEIGHT_DP = 60
/** Default background color (White) for a student box. */
const val DEFAULT_STUDENT_BOX_BG_COLOR_HEX = "#FFFFFFFF"
/** Default outline color (Black) for a student box. */
const val DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX = "#FF000000"
/** Default text color (Black) for student labels. */
const val DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX = "#FF000000"
/** Default outline thickness for student boxes in DP. */
const val DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP = 1
/** Default corner radius for student boxes in DP. */
const val DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP = 8
/** Default internal padding for student boxes in DP. */
const val DEFAULT_STUDENT_BOX_PADDING_DP = 4
/** Default font family for student UI text. */
const val DEFAULT_STUDENT_FONT_FAMILY = "sans-serif"
/** Default font size for student UI text in SP. */
const val DEFAULT_STUDENT_FONT_SIZE_SP = 16
/** Default font color for student UI text. */
const val DEFAULT_STUDENT_FONT_COLOR_HEX = "#FF000000"
/** Default limit for the number of recent behavior incidents to display. */
const val DEFAULT_RECENT_BEHAVIOR_INCIDENTS_LIMIT = 3
/** Default timeout (0 = infinite) for how long a log entry remains visible in the UI. */
const val DEFAULT_LOG_DISPLAY_TIMEOUT = 0

/**
 * A unified state container representing the user's current configuration preferences.
 * This class is used to provide a cohesive view of application settings to the UI.
 *
 * @property recentBehaviorIncidentsLimit Max number of behavior logs to track in the recent list.
 * @property recentHomeworkLogsLimit Max number of homework logs to track in the recent list.
 * @property recentLogsLimit Max number of generic logs to track in the recent list.
 * @property maxRecentLogsToDisplay Number of log entries visible on the student icon.
 * @property useInitialsForBehavior Whether to use short initials for behavior types in the UI.
 * @property useInitialsForHomework Whether to use short initials for homework types in the UI.
 * @property useInitialsForQuiz Whether to use short initials for quiz types in the UI.
 * @property behaviorInitialsMap Mapping of behavior names to their short initials.
 * @property homeworkInitialsMap Mapping of homework names to their short initials.
 * @property quizInitialsMap Mapping of quiz names to their short initials.
 * @property studentLogsLastCleared Mapping of student ID to timestamp of when their logs were last cleared.
 * @property behaviorDisplayTimeout Duration in seconds behavior logs stay visible (0 = infinite).
 * @property homeworkDisplayTimeout Duration in seconds homework logs stay visible (0 = infinite).
 * @property quizDisplayTimeout Duration in seconds quiz logs stay visible (0 = infinite).
 * @property defaultStudentStyle The baseline visual style for student boxes.
 * @property autoExpandStudentBoxes Whether boxes should grow to accommodate log text.
 * @property editModeEnabled Whether the seating chart is in interactive move mode.
 * @property gridSnapEnabled Whether items snap to the grid during movement.
 * @property gridSize The size of the alignment grid in DP.
 * @property noAnimations Whether to disable visual transitions for performance.
 * @property passwordAutoLockEnabled Whether the app should lock after a period of inactivity.
 * @property passwordAutoLockTimeoutMinutes Minutes of inactivity before auto-locking.
 * @property liveQuizQuestionsGoal Target number of questions for a live session.
 * @property liveQuizInitialColor Start color for progress gradients in live quizzes.
 * @property liveQuizFinalColor Target color for progress gradients in live quizzes.
 * @property quizLogFontColor Primary color for quiz log text.
 * @property homeworkLogFontColor Primary color for homework log text.
 * @property quizLogFontBold Whether quiz log text should be rendered in bold.
 * @property homeworkLogFontBold Whether homework log text should be rendered in bold.
 */
data class UserPreferences(
    val recentBehaviorIncidentsLimit: Int,
    val recentHomeworkLogsLimit: Int,
    val recentLogsLimit: Int,
    val maxRecentLogsToDisplay: Int,
    val useInitialsForBehavior: Boolean,
    val useInitialsForHomework: Boolean,
    val useInitialsForQuiz: Boolean,
    val behaviorInitialsMap: String,
    val homeworkInitialsMap: String,
    val quizInitialsMap: String,
    val studentLogsLastCleared: Map<Long, Long>,
    val behaviorDisplayTimeout: Int,
    val homeworkDisplayTimeout: Int,
    val quizDisplayTimeout: Int,
    val defaultStudentStyle: DefaultStudentStyle,
    val autoExpandStudentBoxes: Boolean,
    val editModeEnabled: Boolean,
    val gridSnapEnabled: Boolean,
    val gridSize: Int,
    val noAnimations: Boolean,
    val passwordAutoLockEnabled: Boolean,
    val passwordAutoLockTimeoutMinutes: Int,
    val liveQuizQuestionsGoal: Int,
    val liveQuizInitialColor: String,
    val liveQuizFinalColor: String,
    val quizLogFontColor: String,
    val homeworkLogFontColor: String,
    val quizLogFontBold: Boolean,
    val homeworkLogFontBold: Boolean
)

/**
 * AppPreferencesRepository: The single source of truth for application settings.
 *
 * This repository manages user configuration and preference state using Jetpack DataStore.
 * It provides reactive [Flow] streams for all settings and atomic [suspend] methods for
 * updates, ensuring a Unidirectional Data Flow.
 *
 * ### Shield (Security Boundary):
 * This repository coordinates with [SecurityUtil] to ensure that sensitive preferences
 * (e.g., email passwords, SMTP settings, schedules) are encrypted at rest within the
 * DataStore. It transparently handles encryption on write and decryption on read.
 */
class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityUtil: SecurityUtil
) {

    /**
     * Central registry for DataStore preference keys.
     * Use these keys to access or modify specific settings in the repository.
     */
    object PreferencesKeys {
        val RECENT_LOGS_LIMIT = intPreferencesKey("recent_logs_limit")
        val RECENT_HOMEWORK_LOGS_LIMIT = intPreferencesKey("recent_homework_logs_limit")
        val RECENT_BEHAVIOR_INCIDENTS_LIMIT = intPreferencesKey("recent_behavior_incidents_limit")
        val MAX_RECENT_LOGS_TO_DISPLAY = intPreferencesKey("max_recent_logs_to_display")
        val USE_INITIALS_FOR_BEHAVIOR = booleanPreferencesKey("use_initials_for_behavior")
        val USE_INITIALS_FOR_HOMEWORK = booleanPreferencesKey("use_initials_for_homework")
        val USE_INITIALS_FOR_QUIZ = booleanPreferencesKey("use_initials_for_quiz")
        val HOMEWORK_INITIALS_MAP = stringPreferencesKey("homework_initials_map")
        val QUIZ_INITIALS_MAP = stringPreferencesKey("quiz_initials_map")
        val USE_FULL_NAME_FOR_STUDENT = booleanPreferencesKey("use_full_name_for_student")
        val APP_THEME = stringPreferencesKey("app_theme")
        val SHOW_RECENT_BEHAVIOR = booleanPreferencesKey("show_recent_behavior")

        // Keys for default student box appearance
        val DEFAULT_STUDENT_BOX_WIDTH = intPreferencesKey("default_student_box_width")
        val DEFAULT_STUDENT_BOX_HEIGHT = intPreferencesKey("default_student_box_height")
        val DEFAULT_STUDENT_BOX_BG_COLOR = stringPreferencesKey("default_student_box_bg_color")
        val DEFAULT_STUDENT_BOX_OUTLINE_COLOR = stringPreferencesKey("default_student_box_outline_color")
        val DEFAULT_STUDENT_BOX_TEXT_COLOR = stringPreferencesKey("default_student_box_text_color")
        val DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS = intPreferencesKey("default_student_box_outline_thickness")
        val DEFAULT_STUDENT_BOX_CORNER_RADIUS = intPreferencesKey("default_student_box_corner_radius")
        val DEFAULT_STUDENT_BOX_PADDING = intPreferencesKey("default_student_box_padding")

        val DEFAULT_STUDENT_FONT_FAMILY = stringPreferencesKey("default_student_font_family")
        val DEFAULT_STUDENT_FONT_SIZE = intPreferencesKey("default_student_font_size")
        val DEFAULT_STUDENT_FONT_COLOR = stringPreferencesKey("default_student_font_color")

        val PASSWORD_ENABLED = booleanPreferencesKey("password_enabled")
        val PASSWORD_HASH = stringPreferencesKey("password_hash")

        val BEHAVIOR_TYPES_LIST = stringSetPreferencesKey("behavior_types_list")
        val HOMEWORK_ASSIGNMENT_TYPES_LIST = stringSetPreferencesKey("homework_assignment_types_list")
        val HOMEWORK_STATUSES_LIST = stringSetPreferencesKey("homework_statuses_list")

        val STICKY_QUIZ_NAME_DURATION_SECONDS = intPreferencesKey("sticky_quiz_name_duration_seconds")
        val STICKY_HOMEWORK_NAME_DURATION_SECONDS = intPreferencesKey("sticky_homework_name_duration_seconds")
        val LAST_QUIZ_NAME = stringPreferencesKey("last_quiz_name")
        val LAST_QUIZ_TIMESTAMP = longPreferencesKey("last_quiz_timestamp")
        val LAST_HOMEWORK_NAME = stringPreferencesKey("last_homework_name")
        val LAST_HOMEWORK_TIMESTAMP = longPreferencesKey("last_homework_timestamp")
        val BEHAVIOR_INITIALS_MAP = stringPreferencesKey("behavior_initials_map")
        val NO_ANIMATIONS = booleanPreferencesKey("no_animations")
        val AUTOSAVE_INTERVAL = intPreferencesKey("autosave_interval")
        val GRID_SNAP_ENABLED = booleanPreferencesKey("grid_snap_enabled")
        val GRID_SIZE = intPreferencesKey("grid_size")
        val SHOW_RULERS = booleanPreferencesKey("show_rulers")
        val SHOW_GRID = booleanPreferencesKey("show_grid")
        val EDIT_MODE_ENABLED = booleanPreferencesKey("edit_mode_enabled")
        val AUTO_EXPAND_STUDENT_BOXES = booleanPreferencesKey("auto_expand_student_boxes")
        val STUDENT_LOGS_LAST_CLEARED = stringSetPreferencesKey("student_logs_last_cleared")
        val LAST_EXPORT_PATH = stringPreferencesKey("last_export_path")
        val ENCRYPT_DATA_FILES = booleanPreferencesKey("encrypt_data_files")
        val USE_BOLD_FONT = booleanPreferencesKey("use_bold_font")

        val DEFAULT_EMAIL_ADDRESS = stringPreferencesKey("default_email_address")
        val AUTO_SEND_EMAIL_ON_CLOSE = booleanPreferencesKey("auto_send_email_on_close")
        val EMAIL_SCHEDULES = stringSetPreferencesKey("email_schedules")
        val EMAIL_PASSWORD = stringPreferencesKey("email_password")

        val BEHAVIOR_DISPLAY_TIMEOUT = intPreferencesKey("behavior_display_timeout")
        val HOMEWORK_DISPLAY_TIMEOUT = intPreferencesKey("homework_display_timeout")
        val QUIZ_DISPLAY_TIMEOUT = intPreferencesKey("quiz_display_timeout")

        val CANVAS_BACKGROUND_COLOR = stringPreferencesKey("canvas_background_color")
        val GUIDES_STAY_WHEN_RULERS_HIDDEN = booleanPreferencesKey("guides_stay_when_rulers_hidden")

        val LIVE_HOMEWORK_SESSION_MODE = stringPreferencesKey("live_homework_session_mode")
        val LIVE_HOMEWORK_SELECT_OPTIONS = stringPreferencesKey("live_homework_select_options")
        val SMTP_SETTINGS = stringPreferencesKey("smtp_settings")
        val PASSWORD_AUTO_LOCK_ENABLED = booleanPreferencesKey("password_auto_lock_enabled")
        val PASSWORD_AUTO_LOCK_TIMEOUT_MINUTES = intPreferencesKey("password_auto_lock_timeout_minutes")

        val LIVE_QUIZ_QUESTIONS_GOAL = intPreferencesKey("live_quiz_questions_goal")
        val LIVE_QUIZ_INITIAL_COLOR = stringPreferencesKey("live_quiz_initial_color")
        val LIVE_QUIZ_FINAL_COLOR = stringPreferencesKey("live_quiz_final_color")
        val QUIZ_LOG_FONT_COLOR = stringPreferencesKey("quiz_log_font_color")
        val HOMEWORK_LOG_FONT_COLOR = stringPreferencesKey("homework_log_font_color")
        val QUIZ_LOG_FONT_BOLD = booleanPreferencesKey("quiz_log_font_bold")
        val HOMEWORK_LOG_FONT_BOLD = booleanPreferencesKey("homework_log_font_bold")

        val AUTH_FAILED_ATTEMPTS = intPreferencesKey("auth_failed_attempts")
        val AUTH_LOCKOUT_UNTIL = longPreferencesKey("auth_lockout_until")
    }

    /** Reactive stream of the number of failed password attempts. */
    val authFailedAttemptsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTH_FAILED_ATTEMPTS] ?: 0
        }

    /** Updates the number of failed password attempts. */
    suspend fun updateAuthFailedAttempts(attempts: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AUTH_FAILED_ATTEMPTS] = attempts
        }
    }

    /** Reactive stream of the lockout expiration timestamp. */
    val authLockoutUntilFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTH_LOCKOUT_UNTIL] ?: 0L
        }

    /** Updates the lockout expiration timestamp. */
    suspend fun updateAuthLockoutUntil(timestamp: Long) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AUTH_LOCKOUT_UNTIL] = timestamp
        }
    }

    /** Reactive stream of the target number of questions for live quiz sessions. */
    val liveQuizQuestionsGoalFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LIVE_QUIZ_QUESTIONS_GOAL] ?: 5
        }

    /** Updates the target number of questions for live quiz sessions. */
    suspend fun updateLiveQuizQuestionsGoal(goal: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LIVE_QUIZ_QUESTIONS_GOAL] = goal
        }
    }

    /** Reactive stream of the starting color for progress bars in live sessions. */
    val liveQuizInitialColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LIVE_QUIZ_INITIAL_COLOR] ?: "#FFFF0000"
        }

    /** Updates the starting color for progress bars in live sessions. */
    suspend fun updateLiveQuizInitialColor(color: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LIVE_QUIZ_INITIAL_COLOR] = color
        }
    }

    /** Reactive stream of the target completion color for progress bars in live sessions. */
    val liveQuizFinalColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LIVE_QUIZ_FINAL_COLOR] ?: "#FF00FF00"
        }

    /** Updates the target completion color for progress bars in live sessions. */
    suspend fun updateLiveQuizFinalColor(color: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LIVE_QUIZ_FINAL_COLOR] = color
        }
    }

    /** Reactive stream of the font color used for quiz log entries. */
    val quizLogFontColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.QUIZ_LOG_FONT_COLOR] ?: "#FF006400"
        }

    /** Updates the font color used for quiz log entries. */
    suspend fun updateQuizLogFontColor(color: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.QUIZ_LOG_FONT_COLOR] = color
        }
    }

    /** Reactive stream of the font color used for homework log entries. */
    val homeworkLogFontColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HOMEWORK_LOG_FONT_COLOR] ?: "#FF800080"
        }

    /** Updates the font color used for homework log entries. */
    suspend fun updateHomeworkLogFontColor(color: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_LOG_FONT_COLOR] = color
        }
    }

    /** Reactive stream indicating if quiz log text should be bold. */
    val quizLogFontBoldFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.QUIZ_LOG_FONT_BOLD] ?: true
        }

    /** Updates whether quiz log text should be bold. */
    suspend fun updateQuizLogFontBold(bold: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.QUIZ_LOG_FONT_BOLD] = bold
        }
    }

    /** Reactive stream indicating if homework log text should be bold. */
    val homeworkLogFontBoldFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HOMEWORK_LOG_FONT_BOLD] ?: true
        }

    /** Updates whether homework log text should be bold. */
    suspend fun updateHomeworkLogFontBold(bold: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_LOG_FONT_BOLD] = bold
        }
    }

    /** Reactive stream indicating if the auto-lock feature is enabled. */
    val passwordAutoLockEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSWORD_AUTO_LOCK_ENABLED] ?: false
        }

    /** Updates whether the auto-lock feature is enabled. */
    suspend fun updatePasswordAutoLockEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PASSWORD_AUTO_LOCK_ENABLED] = enabled
        }
    }

    /** Reactive stream of the inactivity timeout (minutes) for the auto-lock feature. */
    val passwordAutoLockTimeoutMinutesFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSWORD_AUTO_LOCK_TIMEOUT_MINUTES] ?: 15
        }

    /** Updates the inactivity timeout (minutes) for the auto-lock feature. */
    suspend fun updatePasswordAutoLockTimeoutMinutes(minutes: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PASSWORD_AUTO_LOCK_TIMEOUT_MINUTES] = minutes
        }
    }

    /**
     * Reactive stream of all automated email schedules.
     * Decodes and decrypts schedule metadata from DataStore.
     */
    val emailSchedulesFlow: Flow<List<EmailSchedule>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.EMAIL_SCHEDULES] ?: emptySet()).mapNotNull {
                try {
                    val decrypted = securityUtil.decryptSafe(it)
                    kotlinx.serialization.json.Json.decodeFromString<EmailSchedule>(decrypted)
                } catch (e: Exception) {
                    null
                }
            }
        }

    /**
     * Updates the set of automated email schedules.
     * Encrypts schedule metadata before persisting to DataStore.
     */
    suspend fun updateEmailSchedules(schedules: List<EmailSchedule>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.EMAIL_SCHEDULES] = schedules.map {
                val json = kotlinx.serialization.json.Json.encodeToString(it)
                securityUtil.encrypt(json)
            }.toSet()
        }
    }

    /** Reactive stream indicating if bold fonts are used globally in student boxes. */
    val useBoldFontFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_BOLD_FONT] ?: false
        }

    /** Updates whether bold fonts are used globally in student boxes. */
    suspend fun updateUseBoldFont(useBoldFont: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_BOLD_FONT] = useBoldFont
        }
    }
    /** Reactive stream of the last directory path used for exporting Excel reports. */
    val lastExportPathFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_EXPORT_PATH]?.let { securityUtil.decryptSafe(it) }
        }

    /** Updates the last used export path, encrypting the string for privacy. */
    suspend fun updateLastExportPath(path: String) {
        val encryptedPath = securityUtil.encrypt(path)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_EXPORT_PATH] = encryptedPath
        }
    }

    /** Reactive stream indicating if Fernet encryption is enabled for export files. */
    val encryptDataFilesFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENCRYPT_DATA_FILES] ?: true
        }

    /** Updates whether Fernet encryption is enabled for export files. */
    suspend fun updateEncryptDataFiles(encrypt: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.ENCRYPT_DATA_FILES] = encrypt
        }
    }

    /**
     * Reactive stream of log-clearing timestamps for all students.
     * Used to filter out historical logs that were cleared by the teacher.
     */
    val studentLogsLastClearedFlow: Flow<Map<Long, Long>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] ?: emptySet())
                .mapNotNull {
                    val decrypted = securityUtil.decryptSafe(it)
                    decrypted.split(":").let { parts ->
                        if (parts.size == 2) {
                            val studentId = parts[0].toLongOrNull()
                            val timestamp = parts[1].toLongOrNull()
                            if (studentId != null && timestamp != null) {
                                studentId to timestamp
                            } else null
                        } else null
                    }
                }.toMap()
        }

    /**
     * Updates the clear-log timestamp for a specific student.
     * Encrypts the mapping string before storage.
     */
    suspend fun updateStudentLogsLastCleared(studentId: Long, timestamp: Long) {
        context.dataStore.edit { settings ->
            val currentCleared = settings[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] ?: emptySet()
            val newCleared = currentCleared.toMutableSet()
            newCleared.removeAll { securityUtil.decryptSafe(it).startsWith("$studentId:") }
            newCleared.add(securityUtil.encrypt("$studentId:$timestamp"))
            settings[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] = newCleared
        }
    }

    /** Removes the clear-log record for a student. */
    suspend fun removeStudentLogsLastCleared(studentId: Long) {
        context.dataStore.edit { settings ->
            val currentCleared = settings[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] ?: emptySet()
            val newCleared = currentCleared.toMutableSet()
            newCleared.removeAll { securityUtil.decryptSafe(it).startsWith("$studentId:") }
            settings[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] = newCleared
        }
    }

    /** Reactive stream indicating if the seating chart is currently in edit mode. */
    val editModeEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EDIT_MODE_ENABLED] ?: false
        }

    /** Updates whether the seating chart is in edit mode. */
    suspend fun updateEditModeEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.EDIT_MODE_ENABLED] = enabled
        }
    }

    /** Reactive stream indicating if student boxes should auto-expand for logs. */
    val autoExpandStudentBoxesFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_EXPAND_STUDENT_BOXES] ?: true
        }

    /** Updates whether student boxes should auto-expand for logs. */
    suspend fun updateAutoExpandStudentBoxes(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AUTO_EXPAND_STUDENT_BOXES] = enabled
        }
    }

    /** Reactive stream of the max number of generic logs to track. */
    val recentLogsLimitFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RECENT_LOGS_LIMIT] ?: 3
        }

    /** Updates the max number of generic logs to track. */
    suspend fun updateRecentLogsLimit(limit: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.RECENT_LOGS_LIMIT] = limit
        }
    }

    /** Reactive stream of the max number of homework logs to track. */
    val recentHomeworkLogsLimitFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RECENT_HOMEWORK_LOGS_LIMIT] ?: 3
        }

    /** Updates the max number of homework logs to track. */
    suspend fun updateRecentHomeworkLogsLimit(limit: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.RECENT_HOMEWORK_LOGS_LIMIT] = limit
        }
    }

    /** Reactive stream of the max number of behavior incidents to track. */
    val recentBehaviorIncidentsLimitFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RECENT_BEHAVIOR_INCIDENTS_LIMIT] ?: DEFAULT_RECENT_BEHAVIOR_INCIDENTS_LIMIT
        }

    /** Updates the max number of behavior incidents to track. */
    suspend fun updateRecentBehaviorIncidentsLimit(limit: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.RECENT_BEHAVIOR_INCIDENTS_LIMIT] = limit
        }
    }

    /** Reactive stream of the number of recent logs to display on the student icon. */
    val maxRecentLogsToDisplayFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.MAX_RECENT_LOGS_TO_DISPLAY] ?: 1
        }

    /** Updates the number of recent logs to display on the student icon. */
    suspend fun updateMaxRecentLogsToDisplay(limit: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.MAX_RECENT_LOGS_TO_DISPLAY] = limit
        }
    }

    /** Reactive stream indicating if initials should be used for behavior logs. */
    val useInitialsForBehaviorFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_INITIALS_FOR_BEHAVIOR] ?: false
        }

    /** Updates whether to use initials for behavior logs. */
    suspend fun updateUseInitialsForBehavior(useInitials: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_INITIALS_FOR_BEHAVIOR] = useInitials
        }
    }

    /** Reactive stream indicating if initials should be used for homework logs. */
    val useInitialsForHomeworkFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_INITIALS_FOR_HOMEWORK] ?: false
        }

    /** Updates whether to use initials for homework logs. */
    suspend fun updateUseInitialsForHomework(useInitials: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_INITIALS_FOR_HOMEWORK] = useInitials
        }
    }

    /** Reactive stream indicating if initials should be used for quiz logs. */
    val useInitialsForQuizFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_INITIALS_FOR_QUIZ] ?: false
        }

    /** Updates whether to use initials for quiz logs. */
    suspend fun updateUseInitialsForQuiz(useInitials: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_INITIALS_FOR_QUIZ] = useInitials
        }
    }

    /** Reactive stream of the homework initials mapping. Decrypted for use. */
    val homeworkInitialsMapFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.HOMEWORK_INITIALS_MAP] ?: ""
            securityUtil.decryptSafe(value)
        }

    /** Updates the homework initials mapping. Encrypted for storage. */
    suspend fun updateHomeworkInitialsMap(map: String) {
        val encryptedMap = securityUtil.encrypt(map)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_INITIALS_MAP] = encryptedMap
        }
    }

    /** Reactive stream of the quiz initials mapping. Decrypted for use. */
    val quizInitialsMapFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.QUIZ_INITIALS_MAP] ?: ""
            securityUtil.decryptSafe(value)
        }

    /** Updates the quiz initials mapping. Encrypted for storage. */
    suspend fun updateQuizInitialsMap(map: String) {
        val encryptedMap = securityUtil.encrypt(map)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.QUIZ_INITIALS_MAP] = encryptedMap
        }
    }

    /** Reactive stream indicating if student full names (vs first names) are displayed. */
    val useFullNameForStudentFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_FULL_NAME_FOR_STUDENT] ?: false
        }

    /** Updates whether student full names are displayed. */
    suspend fun updateUseFullNameForStudent(useFullName: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_FULL_NAME_FOR_STUDENT] = useFullName
        }
    }

    /** Reactive stream of the current application theme name. */
    val appThemeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name
        }

    /** Updates the application theme. */
    suspend fun updateAppTheme(theme: AppTheme) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    /** Reactive stream of defined behavior types. Decrypted for use. */
    val behaviorTypesListFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.BEHAVIOR_TYPES_LIST] ?: emptySet()).map {
                securityUtil.decryptSafe(it)
            }.toSet()
        }

    /** Updates the list of behavior types. Encrypted for storage. */
    suspend fun updateBehaviorTypes(types: Set<String>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.BEHAVIOR_TYPES_LIST] = types.map {
                securityUtil.encrypt(it)
            }.toSet()
        }
    }

    /** Reactive stream of defined homework assignment types. Decrypted for use. */
    val homeworkAssignmentTypesListFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.HOMEWORK_ASSIGNMENT_TYPES_LIST] ?: emptySet()).map {
                securityUtil.decryptSafe(it)
            }.toSet()
        }

    /** Updates the list of homework assignment types. Encrypted for storage. */
    suspend fun updateHomeworkAssignmentTypes(types: Set<String>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_ASSIGNMENT_TYPES_LIST] = types.map {
                securityUtil.encrypt(it)
            }.toSet()
        }
    }

    /** Reactive stream of defined homework statuses. Decrypted for use. */
    val homeworkStatusesListFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.HOMEWORK_STATUSES_LIST] ?: emptySet()).map {
                securityUtil.decryptSafe(it)
            }.toSet()
        }

    /** Updates the list of homework statuses. Encrypted for storage. */
    suspend fun updateHomeworkStatuses(statuses: Set<String>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_STATUSES_LIST] = statuses.map {
                securityUtil.encrypt(it)
            }.toSet()
        }
    }

    /** Reactive stream indicating if recent behavior log summaries are shown. */
    val showRecentBehaviorFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_RECENT_BEHAVIOR] ?: true
        }

    /** Updates whether recent behavior log summaries are shown. */
    suspend fun updateShowRecentBehavior(show: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.SHOW_RECENT_BEHAVIOR] = show
        }
    }

    /** Reactive stream of the default student box width. */
    val defaultStudentBoxWidthFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_WIDTH] ?: DEFAULT_STUDENT_BOX_WIDTH_DP
        }

    /** Updates the default student box width. */
    suspend fun updateDefaultStudentBoxWidth(width: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_WIDTH] = width
        }
    }

    /** Reactive stream of the default student box height. */
    val defaultStudentBoxHeightFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_HEIGHT] ?: DEFAULT_STUDENT_BOX_HEIGHT_DP
        }

    /** Updates the default student box height. */
    suspend fun updateDefaultStudentBoxHeight(height: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_HEIGHT] = height
        }
    }

    /** Reactive stream of the default student box background color. */
    val defaultStudentBoxBackgroundColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_BG_COLOR] ?: DEFAULT_STUDENT_BOX_BG_COLOR_HEX
        }

    /** Updates the default student box background color. */
    suspend fun updateDefaultStudentBoxBackgroundColor(colorHex: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_BG_COLOR] = colorHex
        }
    }

    /** Reactive stream of the default student box outline color. */
    val defaultStudentBoxOutlineColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_COLOR] ?: DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX
        }

    /** Updates the default student box outline color. */
    suspend fun updateDefaultStudentBoxOutlineColor(colorHex: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_COLOR] = colorHex
        }
    }

    /** Reactive stream of the default student box text color. */
    val defaultStudentBoxTextColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_TEXT_COLOR] ?: DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX
        }

    /** Updates the default student box text color. */
    suspend fun updateDefaultStudentBoxTextColor(colorHex: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_TEXT_COLOR] = colorHex
        }
    }

    /** Reactive stream of the default student box outline thickness. */
    val defaultStudentBoxOutlineThicknessFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS] ?: DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP
        }

    /** Updates the default student box outline thickness. */
    suspend fun updateDefaultStudentBoxOutlineThickness(thickness: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS] = thickness
        }
    }

    /** Reactive stream of the default student box corner radius. */
    val defaultStudentBoxCornerRadiusFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_CORNER_RADIUS] ?: DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP
        }

    /** Updates the default student box corner radius. */
    suspend fun updateDefaultStudentBoxCornerRadius(cornerRadius: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_CORNER_RADIUS] = cornerRadius
        }
    }

    /** Reactive stream of the default student box internal padding. */
    val defaultStudentBoxPaddingFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_PADDING] ?: DEFAULT_STUDENT_BOX_PADDING_DP
        }

    /** Updates the default student box internal padding. */
    suspend fun updateDefaultStudentBoxPadding(padding: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_PADDING] = padding
        }
    }

    /** Reactive stream of the default font family for student UI text. */
    val defaultStudentFontFamilyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_FONT_FAMILY] ?: DEFAULT_STUDENT_FONT_FAMILY
        }

    /** Updates the default font family for student UI text. */
    suspend fun updateDefaultStudentFontFamily(fontFamily: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_FONT_FAMILY] = fontFamily
        }
    }

    /** Reactive stream of the default font size for student UI text. */
    val defaultStudentFontSizeFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_FONT_SIZE] ?: DEFAULT_STUDENT_FONT_SIZE_SP
        }

    /** Updates the default font size for student UI text. */
    suspend fun updateDefaultStudentFontSize(fontSize: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_FONT_SIZE] = fontSize
        }
    }

    /** Reactive stream of the default font color for student UI text. */
    val defaultStudentFontColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_FONT_COLOR] ?: DEFAULT_STUDENT_FONT_COLOR_HEX
        }

    /** Updates the default font color for student UI text. */
    suspend fun updateDefaultStudentFontColor(colorHex: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_FONT_COLOR] = colorHex
        }
    }

    /** Reactive stream indicating if password protection is enabled. */
    val passwordEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSWORD_ENABLED] ?: false
        }

    /** Updates whether password protection is enabled. */
    suspend fun updatePasswordEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PASSWORD_ENABLED] = enabled
        }
    }

    /**
     * Reactive stream of the password hash.
     * Decrypts the hash from DataStore for verification.
     */
    val passwordHashFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSWORD_HASH]?.let { securityUtil.decryptSafe(it) }
        }

    /**
     * Updates the password hash.
     * Encrypts the hash before storage.
     */
    suspend fun updatePasswordHash(hash: String) {
        val encryptedHash = securityUtil.encrypt(hash)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PASSWORD_HASH] = encryptedHash
        }
    }

    /** Reactive stream of the duration (seconds) that quiz names remain "sticky" in the UI. */
    val stickyQuizNameDurationSecondsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.STICKY_QUIZ_NAME_DURATION_SECONDS] ?: 0
        }

    /** Updates the duration that quiz names remain "sticky" in the UI. */
    suspend fun updateStickyQuizNameDurationSeconds(duration: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.STICKY_QUIZ_NAME_DURATION_SECONDS] = duration
        }
    }

    /** Reactive stream of the duration (seconds) that homework names remain "sticky" in the UI. */
    val stickyHomeworkNameDurationSecondsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.STICKY_HOMEWORK_NAME_DURATION_SECONDS] ?: 0
        }

    /** Updates the duration that homework names remain "sticky" in the UI. */
    suspend fun updateStickyHomeworkNameDurationSeconds(duration: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.STICKY_HOMEWORK_NAME_DURATION_SECONDS] = duration
        }
    }

    /** Reactive stream of the name of the last quiz logged. Decrypted for use. */
    val lastQuizNameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_QUIZ_NAME]?.let { securityUtil.decryptSafe(it) }
        }

    /** Updates the name of the last quiz logged. Encrypted for storage. */
    suspend fun updateLastQuizName(name: String) {
        val encryptedName = securityUtil.encrypt(name)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_QUIZ_NAME] = encryptedName
            settings[PreferencesKeys.LAST_QUIZ_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    /** Reactive stream of the timestamp when the last quiz was logged. */
    val lastQuizTimestampFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_QUIZ_TIMESTAMP]
        }

    /** Reactive stream of the name of the last homework logged. Decrypted for use. */
    val lastHomeworkNameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_HOMEWORK_NAME]?.let { securityUtil.decryptSafe(it) }
        }

    /** Updates the name of the last homework logged. Encrypted for storage. */
    suspend fun updateLastHomeworkName(name: String) {
        val encryptedName = securityUtil.encrypt(name)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_HOMEWORK_NAME] = encryptedName
            settings[PreferencesKeys.LAST_HOMEWORK_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    /** Reactive stream of the timestamp when the last homework was logged. */
    val lastHomeworkTimestampFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_HOMEWORK_TIMESTAMP]
        }

    /** Reactive stream of the behavior initials mapping. Decrypted for use. */
    val behaviorInitialsMapFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.BEHAVIOR_INITIALS_MAP] ?: ""
            securityUtil.decryptSafe(value)
        }

    /** Updates the behavior initials mapping. Encrypted for storage. */
    suspend fun updateBehaviorInitialsMap(map: String) {
        val encryptedMap = securityUtil.encrypt(map)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.BEHAVIOR_INITIALS_MAP] = encryptedMap
        }
    }

    /** Reactive stream indicating if visual animations are disabled. */
    val noAnimationsFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NO_ANIMATIONS] ?: false
        }

    /** Updates whether to disable visual animations. */
    suspend fun updateNoAnimations(noAnimations: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.NO_ANIMATIONS] = noAnimations
        }
    }

    /** Reactive stream of the interval (ms) for autosaving classroom state. */
    val autosaveIntervalFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTOSAVE_INTERVAL] ?: 30000
        }

    /** Updates the autosave interval. */
    suspend fun updateAutosaveInterval(interval: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AUTOSAVE_INTERVAL] = interval
        }
    }

    /** Reactive stream indicating if items snap to the grid during movement. */
    val gridSnapEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GRID_SNAP_ENABLED] ?: false
        }

    /** Updates whether items snap to the grid. */
    suspend fun updateGridSnapEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.GRID_SNAP_ENABLED] = enabled
        }
    }

    /** Reactive stream of the grid size (DP) for the seating chart. */
    val gridSizeFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GRID_SIZE] ?: 20
        }

    /** Updates the grid size. */
    suspend fun updateGridSize(size: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.GRID_SIZE] = size
        }
    }

    /** Reactive stream indicating if coordinate rulers are visible. */
    val showRulersFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_RULERS] ?: false
        }

    /** Updates whether coordinate rulers are visible. */
    suspend fun updateShowRulers(show: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.SHOW_RULERS] = show
        }
    }

    /** Reactive stream indicating if the alignment grid is visible. */
    val showGridFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_GRID] ?: false
        }

    /** Updates whether the alignment grid is visible. */
    suspend fun updateShowGrid(show: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.SHOW_GRID] = show
        }
    }

    /** Reactive stream providing a unified baseline style for student boxes. */
    val defaultStudentStyleFlow: Flow<DefaultStudentStyle> = context.dataStore.data
        .map { preferences ->
            DefaultStudentStyle(
                width = preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_WIDTH] ?: DEFAULT_STUDENT_BOX_WIDTH_DP,
                height = preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_HEIGHT] ?: DEFAULT_STUDENT_BOX_HEIGHT_DP,
                backgroundColor = preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_BG_COLOR] ?: DEFAULT_STUDENT_BOX_BG_COLOR_HEX,
                outlineColor = preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_COLOR] ?: DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX,
                textColor = preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_TEXT_COLOR] ?: DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX,
                outlineThickness = preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS] ?: DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP,
                cornerRadius = preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_CORNER_RADIUS] ?: DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP,
                padding = preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_PADDING] ?: DEFAULT_STUDENT_BOX_PADDING_DP,
                fontFamily = preferences[PreferencesKeys.DEFAULT_STUDENT_FONT_FAMILY] ?: DEFAULT_STUDENT_FONT_FAMILY,
                fontSize = preferences[PreferencesKeys.DEFAULT_STUDENT_FONT_SIZE] ?: DEFAULT_STUDENT_FONT_SIZE_SP,
                fontColor = preferences[PreferencesKeys.DEFAULT_STUDENT_FONT_COLOR] ?: DEFAULT_STUDENT_FONT_COLOR_HEX
            )
        }

    /** Reactive stream of the default email address for reports. Decrypted for use. */
    val defaultEmailAddressFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.DEFAULT_EMAIL_ADDRESS] ?: "behaviorlogger@gmail.com"
            securityUtil.decryptSafe(value)
        }

    /** Updates the default email address. Encrypted for storage. */
    suspend fun updateDefaultEmailAddress(email: String) {
        val encryptedEmail = securityUtil.encrypt(email)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_EMAIL_ADDRESS] = encryptedEmail
        }
    }

    /** Reactive stream indicating if reports should be automatically emailed on app close. */
    val autoSendEmailOnCloseFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_SEND_EMAIL_ON_CLOSE] ?: false
        }

    /** Updates whether to auto-send emails on close. */
    suspend fun updateAutoSendEmailOnClose(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AUTO_SEND_EMAIL_ON_CLOSE] = enabled
        }
    }

    /** Reactive stream of the display timeout (seconds) for behavior logs. */
    val behaviorDisplayTimeoutFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BEHAVIOR_DISPLAY_TIMEOUT] ?: DEFAULT_LOG_DISPLAY_TIMEOUT
        }

    /** Updates the display timeout for behavior logs. */
    suspend fun updateBehaviorDisplayTimeout(timeout: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.BEHAVIOR_DISPLAY_TIMEOUT] = timeout
        }
    }

    /** Reactive stream of the display timeout (seconds) for homework logs. */
    val homeworkDisplayTimeoutFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HOMEWORK_DISPLAY_TIMEOUT] ?: DEFAULT_LOG_DISPLAY_TIMEOUT
        }

    /** Updates the display timeout for homework logs. */
    suspend fun updateHomeworkDisplayTimeout(timeout: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_DISPLAY_TIMEOUT] = timeout
        }
    }

    /** Reactive stream of the display timeout (seconds) for quiz logs. */
    val quizDisplayTimeoutFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.QUIZ_DISPLAY_TIMEOUT] ?: DEFAULT_LOG_DISPLAY_TIMEOUT
        }

    /** Updates the display timeout for quiz logs. */
    suspend fun updateQuizDisplayTimeout(timeout: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.QUIZ_DISPLAY_TIMEOUT] = timeout
        }
    }

    /** Reactive stream of the SMTP email password. Decrypted for use. */
    val emailPasswordFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            val encryptedPassword = preferences[PreferencesKeys.EMAIL_PASSWORD]
            encryptedPassword?.let { securityUtil.decryptSafe(it) }
        }

    /** Updates the SMTP email password. Encrypted for storage. */
    suspend fun updateEmailPassword(password: String) {
        val encryptedPassword = securityUtil.encrypt(password)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.EMAIL_PASSWORD] = encryptedPassword
        }
    }

    /** Reactive stream of the background color for the seating chart canvas. */
    val canvasBackgroundColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CANVAS_BACKGROUND_COLOR] ?: "#FFFFFFFF"
        }

    /** Updates the seating chart canvas background color. */
    suspend fun updateCanvasBackgroundColor(color: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.CANVAS_BACKGROUND_COLOR] = color
        }
    }

    /** Reactive stream indicating if alignment guides remain visible when rulers are hidden. */
    val guidesStayWhenRulersHiddenFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GUIDES_STAY_WHEN_RULERS_HIDDEN] ?: false
        }

    /** Updates whether alignment guides remain visible when rulers are hidden. */
    suspend fun updateGuidesStayWhenRulersHidden(stay: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.GUIDES_STAY_WHEN_RULERS_HIDDEN] = stay
        }
    }

    /** Reactive stream of the current live homework session mode ("Yes/No" or "Select"). */
    val liveHomeworkSessionModeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LIVE_HOMEWORK_SESSION_MODE] ?: "Yes/No"
        }

    /** Updates the live homework session mode. */
    suspend fun updateLiveHomeworkSessionMode(mode: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LIVE_HOMEWORK_SESSION_MODE] = mode
        }
    }

    /** Reactive stream of options for "Select" mode live homework sessions. Decrypted for use. */
    val liveHomeworkSelectOptionsFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.LIVE_HOMEWORK_SELECT_OPTIONS] ?: "Done,Not Done,Signed,Returned"
            securityUtil.decryptSafe(value)
        }

    /** Updates options for "Select" mode live homework sessions. Encrypted for storage. */
    suspend fun updateLiveHomeworkSelectOptions(options: String) {
        val encryptedOptions = securityUtil.encrypt(options)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LIVE_HOMEWORK_SELECT_OPTIONS] = encryptedOptions
        }
    }

    /** Reactive stream of SMTP server configuration. Decrypted and deserialized for use. */
    val smtpSettingsFlow: Flow<SmtpSettings> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SMTP_SETTINGS]?.let {
                try {
                    val decrypted = securityUtil.decryptSafe(it)
                    Json.decodeFromString<SmtpSettings>(decrypted)
                } catch (e: Exception) {
                    SmtpSettings()
                }
            } ?: SmtpSettings()
        }

    /** Updates SMTP server configuration. Serialized and encrypted for storage. */
    suspend fun updateSmtpSettings(smtpSettings: SmtpSettings) {
        val json = Json.encodeToString(smtpSettings)
        val encrypted = securityUtil.encrypt(json)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.SMTP_SETTINGS] = encrypted
        }
    }

    /**
     * A combined reactive stream providing a unified [UserPreferences] object.
     * This flow emits whenever any individual constituent preference changes,
     * providing the UI with a consistent snapshot of the application state.
     */
    val userPreferencesFlow: Flow<UserPreferences> = combine(
        recentBehaviorIncidentsLimitFlow,
        recentHomeworkLogsLimitFlow,
        recentLogsLimitFlow,
        maxRecentLogsToDisplayFlow,
        useInitialsForBehaviorFlow,
        useInitialsForHomeworkFlow,
        useInitialsForQuizFlow,
        behaviorInitialsMapFlow,
        homeworkInitialsMapFlow,
        quizInitialsMapFlow,
        studentLogsLastClearedFlow,
        behaviorDisplayTimeoutFlow,
        homeworkDisplayTimeoutFlow,
        quizDisplayTimeoutFlow,
        defaultStudentStyleFlow,
        autoExpandStudentBoxesFlow,
        editModeEnabledFlow,
        gridSnapEnabledFlow,
        gridSizeFlow,
        noAnimationsFlow,
        passwordAutoLockEnabledFlow,
        passwordAutoLockTimeoutMinutesFlow,
        liveQuizQuestionsGoalFlow,
        liveQuizInitialColorFlow,
        liveQuizFinalColorFlow,
        quizLogFontColorFlow,
        homeworkLogFontColorFlow,
        quizLogFontBoldFlow,
        homeworkLogFontBoldFlow
    ) { args ->
        UserPreferences(
            recentBehaviorIncidentsLimit = args[0] as Int,
            recentHomeworkLogsLimit = args[1] as Int,
            recentLogsLimit = args[2] as Int,
            maxRecentLogsToDisplay = args[3] as Int,
            useInitialsForBehavior = args[4] as Boolean,
            useInitialsForHomework = args[5] as Boolean,
            useInitialsForQuiz = args[6] as Boolean,
            behaviorInitialsMap = args[7] as String,
            homeworkInitialsMap = args[8] as String,
            quizInitialsMap = args[9] as String,
            studentLogsLastCleared = args[10] as Map<Long, Long>,
            behaviorDisplayTimeout = args[11] as Int,
            homeworkDisplayTimeout = args[12] as Int,
            quizDisplayTimeout = args[13] as Int,
            defaultStudentStyle = args[14] as DefaultStudentStyle,
            autoExpandStudentBoxes = args[15] as Boolean,
            editModeEnabled = args[16] as Boolean,
            gridSnapEnabled = args[17] as Boolean,
            gridSize = args[18] as Int,
            noAnimations = args[19] as Boolean,
            passwordAutoLockEnabled = args[20] as Boolean,
            passwordAutoLockTimeoutMinutes = args[21] as Int,
            liveQuizQuestionsGoal = args[22] as Int,
            liveQuizInitialColor = args[23] as String,
            liveQuizFinalColor = args[24] as String,
            quizLogFontColor = args[25] as String,
            homeworkLogFontColor = args[26] as String,
            quizLogFontBold = args[27] as Boolean,
            homeworkLogFontBold = args[28] as Boolean
        )
    }
}

/**
 * Defines the available visual themes for the application.
 */
enum class AppTheme {
    /** Forced light mode. */
    LIGHT,
    /** Forced dark mode. */
    DARK,
    /** Follows the Android system theme (Day/Night). */
    SYSTEM,
    /** Follows Android 12+ dynamic color (Material You) if available. */
    DYNAMIC
}
