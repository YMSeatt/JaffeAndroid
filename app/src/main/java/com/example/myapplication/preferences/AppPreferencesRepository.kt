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
}

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}