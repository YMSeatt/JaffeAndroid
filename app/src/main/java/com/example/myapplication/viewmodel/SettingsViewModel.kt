package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.preferences.QuizMarkTypeSetting
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_WIDTH_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_HEIGHT_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_BG_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP // Added import
import com.example.myapplication.utils.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = AppPreferencesRepository(application)

    val recentLogsLimit: StateFlow<Int> = preferencesRepository.recentLogsLimitFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 3) // Default for homework logs

    fun updateRecentLogsLimit(limit: Int) {
        viewModelScope.launch {
            preferencesRepository.updateRecentLogsLimit(limit)
        }
    }

    // New setting for recent behavior incidents
    val recentBehaviorIncidentsLimit: StateFlow<Int> = preferencesRepository.recentBehaviorIncidentsLimitFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 3) // Default for behavior incidents

    fun updateRecentBehaviorIncidentsLimit(limit: Int) {
        viewModelScope.launch {
            preferencesRepository.updateRecentBehaviorIncidentsLimit(limit)
        }
    }

    val useInitialsForBehavior: StateFlow<Boolean> = preferencesRepository.useInitialsForBehaviorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateUseInitialsForBehavior(useInitials: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateUseInitialsForBehavior(useInitials)
        }
    }

    val appTheme: StateFlow<AppTheme> = preferencesRepository.appThemeFlow
        .map { themeName ->
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = AppTheme.SYSTEM
        )

    fun updateAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            preferencesRepository.updateAppTheme(theme)
        }
    }

    val behaviorTypesList: StateFlow<Set<String>> = preferencesRepository.behaviorTypesListFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    fun updateBehaviorTypes(types: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.updateBehaviorTypes(types)
        }
    }

    val homeworkAssignmentTypesList: StateFlow<Set<String>> = preferencesRepository.homeworkAssignmentTypesListFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    fun updateHomeworkAssignmentTypes(types: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.updateHomeworkAssignmentTypes(types)
        }
    }

    val homeworkStatusesList: StateFlow<Set<String>> = preferencesRepository.homeworkStatusesListFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    fun updateHomeworkStatuses(statuses: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.updateHomeworkStatuses(statuses)
        }
    }

    val quizMarkTypesList: StateFlow<List<QuizMarkTypeSetting>> = preferencesRepository.quizMarkTypesListFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateQuizMarkTypes(types: List<QuizMarkTypeSetting>) {
        viewModelScope.launch {
            preferencesRepository.updateQuizMarkTypes(types)
        }
    }

    val showRecentBehavior: StateFlow<Boolean> = preferencesRepository.showRecentBehaviorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun updateShowRecentBehavior(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateShowRecentBehavior(show)
        }
    }

    // StateFlows for default student box appearance
    val defaultStudentBoxWidth: StateFlow<Int> = preferencesRepository.defaultStudentBoxWidthFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_STUDENT_BOX_WIDTH_DP)

    fun updateDefaultStudentBoxWidth(width: Int) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultStudentBoxWidth(width)
        }
    }

    val defaultStudentBoxHeight: StateFlow<Int> = preferencesRepository.defaultStudentBoxHeightFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_STUDENT_BOX_HEIGHT_DP)

    fun updateDefaultStudentBoxHeight(height: Int) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultStudentBoxHeight(height)
        }
    }

    val defaultStudentBoxBackgroundColor: StateFlow<String> = preferencesRepository.defaultStudentBoxBackgroundColorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_STUDENT_BOX_BG_COLOR_HEX)

    fun updateDefaultStudentBoxBackgroundColor(colorHex: String) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultStudentBoxBackgroundColor(colorHex)
        }
    }

    val defaultStudentBoxOutlineColor: StateFlow<String> = preferencesRepository.defaultStudentBoxOutlineColorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX)

    fun updateDefaultStudentBoxOutlineColor(colorHex: String) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultStudentBoxOutlineColor(colorHex)
        }
    }

    val defaultStudentBoxTextColor: StateFlow<String> = preferencesRepository.defaultStudentBoxTextColorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX)

    fun updateDefaultStudentBoxTextColor(colorHex: String) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultStudentBoxTextColor(colorHex)
        }
    }

    // New StateFlow for default student box outline thickness
    val defaultStudentBoxOutlineThickness: StateFlow<Int> = preferencesRepository.defaultStudentBoxOutlineThicknessFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP)

    fun updateDefaultStudentBoxOutlineThickness(thickness: Int) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultStudentBoxOutlineThickness(thickness)
        }
    }

    val passwordEnabled: StateFlow<Boolean> = preferencesRepository.passwordEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updatePasswordEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updatePasswordEnabled(enabled)
        }
    }

    suspend fun checkPassword(password: String): Boolean {
        val hash = preferencesRepository.passwordHashFlow.first()
        return hash == SecurityUtil.hashPassword(password)
    }

    fun setPassword(password: String) {
        viewModelScope.launch {
            preferencesRepository.updatePasswordHash(SecurityUtil.hashPassword(password))
        }
    }

    suspend fun backupDatabase(uri: Uri) = withContext(Dispatchers.IO) {
        val dbFile = getApplication<Application>().getDatabasePath(AppDatabase.DATABASE_NAME)
        getApplication<Application>().contentResolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(dbFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    suspend fun restoreDatabase(uri: Uri) = withContext(Dispatchers.IO) {
        val dbFile = getApplication<Application>().getDatabasePath(AppDatabase.DATABASE_NAME)
        getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(dbFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        // Restart the app to apply the new database
        val intent = getApplication<Application>().packageManager.getLaunchIntentForPackage(getApplication<Application>().packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        getApplication<Application>().startActivity(intent)
    }
}
