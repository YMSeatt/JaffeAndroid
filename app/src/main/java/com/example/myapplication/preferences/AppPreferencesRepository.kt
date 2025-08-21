package com.example.myapplication.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

// Default values for student box appearance
const val DEFAULT_STUDENT_BOX_WIDTH_DP = 120
const val DEFAULT_STUDENT_BOX_HEIGHT_DP = 100
const val DEFAULT_STUDENT_BOX_BG_COLOR_HEX = "#FFFFFFFF" // White, recommend referencing theme colors if possible
const val DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX = "#FF000000" // Black for outline, adjust as needed
const val DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX = "#FF000000"    // Black for text
const val DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP = 1 // Default outline thickness in Dp
const val DEFAULT_RECENT_BEHAVIOR_INCIDENTS_LIMIT = 3

class AppPreferencesRepository(private val context: Context) {

    object PreferencesKeys {
        val RECENT_LOGS_LIMIT = intPreferencesKey("recent_logs_limit")
        val RECENT_BEHAVIOR_INCIDENTS_LIMIT = intPreferencesKey("recent_behavior_incidents_limit") // New key
        val USE_INITIALS_FOR_BEHAVIOR = booleanPreferencesKey("use_initials_for_behavior")
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

    val recentLogsLimitFlow: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.RECENT_LOGS_LIMIT] ?: 3 // Default to 3
        }

    suspend fun updateRecentLogsLimit(limit: Int) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.RECENT_LOGS_LIMIT] = limit
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

    val useInitialsForBehaviorFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.USE_INITIALS_FOR_BEHAVIOR] ?: false // Default to false
        }

    suspend fun updateUseInitialsForBehavior(useInitials: Boolean) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.USE_INITIALS_FOR_BEHAVIOR] = useInitials
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
            preferences[PreferencesKeys.PASSWORD_HASH]
        }

    suspend fun updatePasswordHash(hash: String) {
        context.dataStore.edit { settings ->
            settings[PreferencesKeys.PASSWORD_HASH] = hash
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
}

enum class AppTheme {
    LIGHT, DARK, SYSTEM, DYNAMIC
}