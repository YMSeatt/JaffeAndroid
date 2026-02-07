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

// Default values for student box appearance
const val DEFAULT_STUDENT_BOX_WIDTH_DP = 120
const val DEFAULT_STUDENT_BOX_HEIGHT_DP = 60
const val DEFAULT_STUDENT_BOX_BG_COLOR_HEX = "#FFFFFFFF" // White, recommend referencing theme colors if possible
const val DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX = "#FF000000" // Black for outline, adjust as needed
const val DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX = "#FF000000"    // Black for text
const val DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP = 1 // Default outline thickness in Dp
const val DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP = 8
const val DEFAULT_STUDENT_BOX_PADDING_DP = 4
const val DEFAULT_STUDENT_FONT_FAMILY = "sans-serif" // Android default
const val DEFAULT_STUDENT_FONT_SIZE_SP = 16 // Example size in SP
const val DEFAULT_STUDENT_FONT_COLOR_HEX = "#FF000000" // Black
const val DEFAULT_RECENT_BEHAVIOR_INCIDENTS_LIMIT = 3
const val DEFAULT_LOG_DISPLAY_TIMEOUT = 0 // 0 means no timeout

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
    val passwordAutoLockTimeoutMinutes: Int
)

class AppPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityUtil: SecurityUtil
) {

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

        // New keys for default student box appearance
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

        // New timeout preferences for specific log types
        val BEHAVIOR_DISPLAY_TIMEOUT = intPreferencesKey("behavior_display_timeout")
        val HOMEWORK_DISPLAY_TIMEOUT = intPreferencesKey("homework_display_timeout")
        val QUIZ_DISPLAY_TIMEOUT = intPreferencesKey("quiz_display_timeout")

        val CANVAS_BACKGROUND_COLOR = stringPreferencesKey("canvas_background_color")
        val GUIDES_STAY_WHEN_RULERS_HIDDEN = booleanPreferencesKey("guides_stay_when_rulers_hidden")

        // Live Homework Session Preferences
        val LIVE_HOMEWORK_SESSION_MODE = stringPreferencesKey("live_homework_session_mode") // "Yes/No" or "Select"
        val LIVE_HOMEWORK_SELECT_OPTIONS = stringPreferencesKey("live_homework_select_options") // JSON or delimited string
        val SMTP_SETTINGS = stringPreferencesKey("smtp_settings")
        val PASSWORD_AUTO_LOCK_ENABLED = booleanPreferencesKey("password_auto_lock_enabled")
        val PASSWORD_AUTO_LOCK_TIMEOUT_MINUTES = intPreferencesKey("password_auto_lock_timeout_minutes")
    }

    val passwordAutoLockEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSWORD_AUTO_LOCK_ENABLED] ?: false
        }

    suspend fun updatePasswordAutoLockEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PASSWORD_AUTO_LOCK_ENABLED] = enabled
        }
    }

    val passwordAutoLockTimeoutMinutesFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSWORD_AUTO_LOCK_TIMEOUT_MINUTES] ?: 15
        }

    suspend fun updatePasswordAutoLockTimeoutMinutes(minutes: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PASSWORD_AUTO_LOCK_TIMEOUT_MINUTES] = minutes
        }
    }

    val emailSchedulesFlow: Flow<List<EmailSchedule>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.EMAIL_SCHEDULES] ?: emptySet()).mapNotNull {
                try {
                    kotlinx.serialization.json.Json.decodeFromString<EmailSchedule>(it)
                } catch (e: Exception) {
                    null
                }
            }
        }

    suspend fun updateEmailSchedules(schedules: List<EmailSchedule>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.EMAIL_SCHEDULES] = schedules.map { kotlinx.serialization.json.Json.encodeToString(it) }.toSet()
        }
    }

    val useBoldFontFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_BOLD_FONT] ?: false
        }

    suspend fun updateUseBoldFont(useBoldFont: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_BOLD_FONT] = useBoldFont
        }
    }
    val lastExportPathFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_EXPORT_PATH]?.let { securityUtil.decryptSafe(it) }
        }

    suspend fun updateLastExportPath(path: String) {
        val encryptedPath = securityUtil.encrypt(path)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_EXPORT_PATH] = encryptedPath
        }
    }

    val encryptDataFilesFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ENCRYPT_DATA_FILES] ?: true
        }

    suspend fun updateEncryptDataFiles(encrypt: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.ENCRYPT_DATA_FILES] = encrypt
        }
    }

    val studentLogsLastClearedFlow: Flow<Map<Long, Long>> = context.dataStore.data
        .map { preferences ->
            (preferences[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] ?: emptySet())
                .mapNotNull { it.split(":").let { parts ->
                    if (parts.size == 2) {
                        val studentId = parts[0].toLongOrNull()
                        val timestamp = parts[1].toLongOrNull()
                        if (studentId != null && timestamp != null) {
                            studentId to timestamp
                        } else null
                    } else null
                } }.toMap()
        }

    suspend fun updateStudentLogsLastCleared(studentId: Long, timestamp: Long) {
        context.dataStore.edit { settings ->
            val currentCleared = settings[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] ?: emptySet()
            val newCleared = currentCleared.toMutableSet()
            newCleared.removeAll { it.startsWith("$studentId:") }
            newCleared.add("$studentId:$timestamp")
            settings[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] = newCleared
        }
    }

    suspend fun removeStudentLogsLastCleared(studentId: Long) {
        context.dataStore.edit { settings ->
            val currentCleared = settings[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] ?: emptySet()
            val newCleared = currentCleared.toMutableSet()
            newCleared.removeAll { it.startsWith("$studentId:") }
            settings[PreferencesKeys.STUDENT_LOGS_LAST_CLEARED] = newCleared
        }
    }

    val editModeEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EDIT_MODE_ENABLED] ?: false
        }

    suspend fun updateEditModeEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.EDIT_MODE_ENABLED] = enabled
        }
    }

    val autoExpandStudentBoxesFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_EXPAND_STUDENT_BOXES] ?: true // Default to true
        }

    suspend fun updateAutoExpandStudentBoxes(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AUTO_EXPAND_STUDENT_BOXES] = enabled
        }
    }

    val recentLogsLimitFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RECENT_LOGS_LIMIT] ?: 3 // Default to 3
        }

    suspend fun updateRecentLogsLimit(limit: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.RECENT_LOGS_LIMIT] = limit
        }
    }

    val recentHomeworkLogsLimitFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RECENT_HOMEWORK_LOGS_LIMIT] ?: 3 // Default to 3
        }

    suspend fun updateRecentHomeworkLogsLimit(limit: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.RECENT_HOMEWORK_LOGS_LIMIT] = limit
        }
    }

    // New flow and update function for recent behavior incidents limit
    val recentBehaviorIncidentsLimitFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RECENT_BEHAVIOR_INCIDENTS_LIMIT] ?: DEFAULT_RECENT_BEHAVIOR_INCIDENTS_LIMIT
        }

    suspend fun updateRecentBehaviorIncidentsLimit(limit: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.RECENT_BEHAVIOR_INCIDENTS_LIMIT] = limit
        }
    }

    val maxRecentLogsToDisplayFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.MAX_RECENT_LOGS_TO_DISPLAY] ?: 1 // Default to 1
        }

    suspend fun updateMaxRecentLogsToDisplay(limit: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.MAX_RECENT_LOGS_TO_DISPLAY] = limit
        }
    }

    val useInitialsForBehaviorFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_INITIALS_FOR_BEHAVIOR] ?: false // Default to false
        }

    suspend fun updateUseInitialsForBehavior(useInitials: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_INITIALS_FOR_BEHAVIOR] = useInitials
        }
    }

    val useInitialsForHomeworkFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_INITIALS_FOR_HOMEWORK] ?: false
        }

    suspend fun updateUseInitialsForHomework(useInitials: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_INITIALS_FOR_HOMEWORK] = useInitials
        }
    }

    val useInitialsForQuizFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_INITIALS_FOR_QUIZ] ?: false
        }

    suspend fun updateUseInitialsForQuiz(useInitials: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_INITIALS_FOR_QUIZ] = useInitials
        }
    }

    val homeworkInitialsMapFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HOMEWORK_INITIALS_MAP] ?: ""
        }

    suspend fun updateHomeworkInitialsMap(map: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_INITIALS_MAP] = map
        }
    }

    val quizInitialsMapFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.QUIZ_INITIALS_MAP] ?: ""
        }

    suspend fun updateQuizInitialsMap(map: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.QUIZ_INITIALS_MAP] = map
        }
    }

    val useFullNameForStudentFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_FULL_NAME_FOR_STUDENT] ?: false // Default to false
        }

    suspend fun updateUseFullNameForStudent(useFullName: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_FULL_NAME_FOR_STUDENT] = useFullName
        }
    }

    val appThemeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.APP_THEME] ?: AppTheme.SYSTEM.name // Default to System
        }

    suspend fun updateAppTheme(theme: AppTheme) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    val behaviorTypesListFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BEHAVIOR_TYPES_LIST] ?: emptySet()
        }

    suspend fun updateBehaviorTypes(types: Set<String>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.BEHAVIOR_TYPES_LIST] = types
        }
    }

    val homeworkAssignmentTypesListFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HOMEWORK_ASSIGNMENT_TYPES_LIST] ?: emptySet()
        }

    suspend fun updateHomeworkAssignmentTypes(types: Set<String>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_ASSIGNMENT_TYPES_LIST] = types
        }
    }

    val homeworkStatusesListFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HOMEWORK_STATUSES_LIST] ?: emptySet()
        }

    suspend fun updateHomeworkStatuses(statuses: Set<String>) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_STATUSES_LIST] = statuses
        }
    }

    val showRecentBehaviorFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_RECENT_BEHAVIOR] ?: true
        }

    suspend fun updateShowRecentBehavior(show: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.SHOW_RECENT_BEHAVIOR] = show
        }
    }

    // Flows and update functions for default student box appearance
    val defaultStudentBoxWidthFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_WIDTH] ?: DEFAULT_STUDENT_BOX_WIDTH_DP
        }

    suspend fun updateDefaultStudentBoxWidth(width: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_WIDTH] = width
        }
    }

    val defaultStudentBoxHeightFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_HEIGHT] ?: DEFAULT_STUDENT_BOX_HEIGHT_DP
        }

    suspend fun updateDefaultStudentBoxHeight(height: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_HEIGHT] = height
        }
    }

    val defaultStudentBoxBackgroundColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_BG_COLOR] ?: DEFAULT_STUDENT_BOX_BG_COLOR_HEX
        }

    suspend fun updateDefaultStudentBoxBackgroundColor(colorHex: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_BG_COLOR] = colorHex
        }
    }

    val defaultStudentBoxOutlineColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_COLOR] ?: DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX
        }

    suspend fun updateDefaultStudentBoxOutlineColor(colorHex: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_COLOR] = colorHex
        }
    }

    val defaultStudentBoxTextColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_TEXT_COLOR] ?: DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX
        }

    suspend fun updateDefaultStudentBoxTextColor(colorHex: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_TEXT_COLOR] = colorHex
        }
    }

    val defaultStudentBoxOutlineThicknessFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS] ?: DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP
        }

    suspend fun updateDefaultStudentBoxOutlineThickness(thickness: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS] = thickness
        }
    }

    val defaultStudentBoxCornerRadiusFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_CORNER_RADIUS] ?: DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP
        }

    suspend fun updateDefaultStudentBoxCornerRadius(cornerRadius: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_CORNER_RADIUS] = cornerRadius
        }
    }

    val defaultStudentBoxPaddingFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_BOX_PADDING] ?: DEFAULT_STUDENT_BOX_PADDING_DP
        }

    suspend fun updateDefaultStudentBoxPadding(padding: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_BOX_PADDING] = padding
        }
    }

    val defaultStudentFontFamilyFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_FONT_FAMILY] ?: DEFAULT_STUDENT_FONT_FAMILY
        }

    suspend fun updateDefaultStudentFontFamily(fontFamily: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_FONT_FAMILY] = fontFamily
        }
    }

    val defaultStudentFontSizeFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_FONT_SIZE] ?: DEFAULT_STUDENT_FONT_SIZE_SP
        }

    suspend fun updateDefaultStudentFontSize(fontSize: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_FONT_SIZE] = fontSize
        }
    }

    val defaultStudentFontColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DEFAULT_STUDENT_FONT_COLOR] ?: DEFAULT_STUDENT_FONT_COLOR_HEX
        }

    suspend fun updateDefaultStudentFontColor(colorHex: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_STUDENT_FONT_COLOR] = colorHex
        }
    }

    val passwordEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSWORD_ENABLED] ?: false
        }

    suspend fun updatePasswordEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PASSWORD_ENABLED] = enabled
        }
    }

    val passwordHashFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSWORD_HASH]?.let { securityUtil.decryptSafe(it) }
        }

    suspend fun updatePasswordHash(hash: String) {
        val encryptedHash = securityUtil.encrypt(hash)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PASSWORD_HASH] = encryptedHash
        }
    }

    val stickyQuizNameDurationSecondsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.STICKY_QUIZ_NAME_DURATION_SECONDS] ?: 0
        }

    suspend fun updateStickyQuizNameDurationSeconds(duration: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.STICKY_QUIZ_NAME_DURATION_SECONDS] = duration
        }
    }

    val stickyHomeworkNameDurationSecondsFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.STICKY_HOMEWORK_NAME_DURATION_SECONDS] ?: 0
        }
    suspend fun updateStickyHomeworkNameDurationSeconds(duration: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.STICKY_HOMEWORK_NAME_DURATION_SECONDS] = duration
        }
    }

    val lastQuizNameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_QUIZ_NAME]
        }

    suspend fun updateLastQuizName(name: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_QUIZ_NAME] = name
            settings[PreferencesKeys.LAST_QUIZ_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    val lastQuizTimestampFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_QUIZ_TIMESTAMP]
        }

    val lastHomeworkNameFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_HOMEWORK_NAME]
        }

    suspend fun updateLastHomeworkName(name: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LAST_HOMEWORK_NAME] = name
            settings[PreferencesKeys.LAST_HOMEWORK_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    val lastHomeworkTimestampFlow: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LAST_HOMEWORK_TIMESTAMP]
        }

    val behaviorInitialsMapFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BEHAVIOR_INITIALS_MAP] ?: ""
        }

    suspend fun updateBehaviorInitialsMap(map: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.BEHAVIOR_INITIALS_MAP] = map
        }
    }

    val noAnimationsFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NO_ANIMATIONS] ?: false
        }

    suspend fun updateNoAnimations(noAnimations: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.NO_ANIMATIONS] = noAnimations
        }
    }

    val autosaveIntervalFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTOSAVE_INTERVAL] ?: 30000
        }

    suspend fun updateAutosaveInterval(interval: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AUTOSAVE_INTERVAL] = interval
        }
    }

    val gridSnapEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GRID_SNAP_ENABLED] ?: false
        }

    suspend fun updateGridSnapEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.GRID_SNAP_ENABLED] = enabled
        }
    }

    val gridSizeFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GRID_SIZE] ?: 20
        }

    suspend fun updateGridSize(size: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.GRID_SIZE] = size
        }
    }

    val showRulersFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_RULERS] ?: false
        }

    suspend fun updateShowRulers(show: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.SHOW_RULERS] = show
        }
    }

    val showGridFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.SHOW_GRID] ?: false
        }

    suspend fun updateShowGrid(show: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.SHOW_GRID] = show
        }
    }

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

    val defaultEmailAddressFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            val value = preferences[PreferencesKeys.DEFAULT_EMAIL_ADDRESS] ?: "behaviorlogger@gmail.com"
            securityUtil.decryptSafe(value)
        }

    suspend fun updateDefaultEmailAddress(email: String) {
        val encryptedEmail = securityUtil.encrypt(email)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.DEFAULT_EMAIL_ADDRESS] = encryptedEmail
        }
    }

    val autoSendEmailOnCloseFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.AUTO_SEND_EMAIL_ON_CLOSE] ?: false
        }

    suspend fun updateAutoSendEmailOnClose(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.AUTO_SEND_EMAIL_ON_CLOSE] = enabled
        }
    }

    val behaviorDisplayTimeoutFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.BEHAVIOR_DISPLAY_TIMEOUT] ?: DEFAULT_LOG_DISPLAY_TIMEOUT
        }

    suspend fun updateBehaviorDisplayTimeout(timeout: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.BEHAVIOR_DISPLAY_TIMEOUT] = timeout
        }
    }

    val homeworkDisplayTimeoutFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HOMEWORK_DISPLAY_TIMEOUT] ?: DEFAULT_LOG_DISPLAY_TIMEOUT
        }

    suspend fun updateHomeworkDisplayTimeout(timeout: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.HOMEWORK_DISPLAY_TIMEOUT] = timeout
        }
    }

    val quizDisplayTimeoutFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.QUIZ_DISPLAY_TIMEOUT] ?: DEFAULT_LOG_DISPLAY_TIMEOUT
        }

    suspend fun updateQuizDisplayTimeout(timeout: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.QUIZ_DISPLAY_TIMEOUT] = timeout
        }
    }

    val emailPasswordFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            val encryptedPassword = preferences[PreferencesKeys.EMAIL_PASSWORD]
            encryptedPassword?.let { securityUtil.decryptSafe(it) }
        }

    suspend fun updateEmailPassword(password: String) {
        val encryptedPassword = securityUtil.encrypt(password)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.EMAIL_PASSWORD] = encryptedPassword
        }
    }

    val canvasBackgroundColorFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CANVAS_BACKGROUND_COLOR] ?: "#FFFFFFFF"
        }

    suspend fun updateCanvasBackgroundColor(color: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.CANVAS_BACKGROUND_COLOR] = color
        }
    }

    val guidesStayWhenRulersHiddenFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.GUIDES_STAY_WHEN_RULERS_HIDDEN] ?: false
        }

    suspend fun updateGuidesStayWhenRulersHidden(stay: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.GUIDES_STAY_WHEN_RULERS_HIDDEN] = stay
        }
    }

    val liveHomeworkSessionModeFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LIVE_HOMEWORK_SESSION_MODE] ?: "Yes/No"
        }

    suspend fun updateLiveHomeworkSessionMode(mode: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LIVE_HOMEWORK_SESSION_MODE] = mode
        }
    }

    val liveHomeworkSelectOptionsFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LIVE_HOMEWORK_SELECT_OPTIONS] ?: "Done,Not Done,Signed,Returned" // Default options
        }

    suspend fun updateLiveHomeworkSelectOptions(options: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.LIVE_HOMEWORK_SELECT_OPTIONS] = options
        }
    }

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

    suspend fun updateSmtpSettings(smtpSettings: SmtpSettings) {
        val json = Json.encodeToString(smtpSettings)
        val encrypted = securityUtil.encrypt(json)
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.SMTP_SETTINGS] = encrypted
        }
    }

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
        passwordAutoLockTimeoutMinutesFlow
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
            passwordAutoLockTimeoutMinutes = args[21] as Int
        )
    }
}

enum class AppTheme {
    LIGHT, DARK, SYSTEM, DYNAMIC
}
