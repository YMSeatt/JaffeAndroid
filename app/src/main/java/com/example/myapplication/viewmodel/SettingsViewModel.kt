package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CustomBehavior
import com.example.myapplication.data.CustomHomeworkStatus
import com.example.myapplication.data.CustomHomeworkType
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.preferences.AppTheme
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
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = AppPreferencesRepository(application)
    private val db = AppDatabase.getDatabase(application)
    private val studentDao = db.studentDao()
    private val furnitureDao = db.furnitureDao()
    private val studentGroupDao = db.studentGroupDao()
    private val layoutTemplateDao = db.layoutTemplateDao()
    private val conditionalFormattingRuleDao = db.conditionalFormattingRuleDao()
    private val customBehaviorDao = db.customBehaviorDao()
    private val customHomeworkTypeDao = db.customHomeworkTypeDao()
    private val customHomeworkStatusDao = db.customHomeworkStatusDao()
    private val quizMarkTypeDao = db.quizMarkTypeDao()
    private val quizTemplateDao = db.quizTemplateDao()
    private val homeworkTemplateDao = db.homeworkTemplateDao()

    private val _restoreComplete = MutableLiveData<Boolean>()
    val restoreComplete: LiveData<Boolean> = _restoreComplete

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

    val useFullNameForStudent: StateFlow<Boolean> = preferencesRepository.useFullNameForStudentFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateUseFullNameForStudent(useFullName: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateUseFullNameForStudent(useFullName)
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

    val customBehaviors: LiveData<List<CustomBehavior>> = customBehaviorDao.getAllCustomBehaviors()
    fun addCustomBehavior(name: String) = viewModelScope.launch {
        customBehaviorDao.insert(CustomBehavior(name = name))
    }
    fun updateCustomBehavior(customBehavior: CustomBehavior) = viewModelScope.launch {
        customBehaviorDao.update(customBehavior)
    }
    fun deleteCustomBehavior(customBehavior: CustomBehavior) = viewModelScope.launch {
        customBehaviorDao.delete(customBehavior)
    }

    val customHomeworkTypes: LiveData<List<CustomHomeworkType>> = customHomeworkTypeDao.getAllCustomHomeworkTypes()
    fun addCustomHomeworkType(name: String) = viewModelScope.launch {
        customHomeworkTypeDao.insert(CustomHomeworkType(name = name))
    }
    fun updateCustomHomeworkType(customHomeworkType: CustomHomeworkType) = viewModelScope.launch {
        customHomeworkTypeDao.update(customHomeworkType)
    }
    fun deleteCustomHomeworkType(customHomeworkType: CustomHomeworkType) = viewModelScope.launch {
        customHomeworkTypeDao.delete(customHomeworkType)
    }

    val customHomeworkStatuses: LiveData<List<CustomHomeworkStatus>> = customHomeworkStatusDao.getAllCustomHomeworkStatuses()
    fun addCustomHomeworkStatus(name: String) = viewModelScope.launch {
        customHomeworkStatusDao.insert(CustomHomeworkStatus(name = name))
    }
    fun updateCustomHomeworkStatus(customHomeworkStatus: CustomHomeworkStatus) = viewModelScope.launch {
        customHomeworkStatusDao.update(customHomeworkStatus)
    }
    fun deleteCustomHomeworkStatus(customHomeworkStatus: CustomHomeworkStatus) = viewModelScope.launch {
        customHomeworkStatusDao.delete(customHomeworkStatus)
    }

    val quizMarkTypes: LiveData<List<QuizMarkType>> = quizMarkTypeDao.getAllQuizMarkTypes().asLiveData()
    fun addQuizMarkType(quizMarkType: QuizMarkType) = viewModelScope.launch {
        quizMarkTypeDao.insert(quizMarkType)
    }
    fun updateQuizMarkType(quizMarkType: QuizMarkType) = viewModelScope.launch {
        quizMarkTypeDao.update(quizMarkType)
    }
    fun deleteQuizMarkType(quizMarkType: QuizMarkType) = viewModelScope.launch {
        quizMarkTypeDao.delete(quizMarkType)
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
        AppDatabase.getDatabase(getApplication()).close()
        getApplication<Application>().contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(dbFile, false).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        _restoreComplete.postValue(true) // This will trigger the restart
    }

    fun triggerRebirth() {
        val packageManager = getApplication<Application>().packageManager
        val intent = packageManager.getLaunchIntentForPackage(getApplication<Application>().packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        getApplication<Application>().startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    val stickyQuizNameDurationSeconds: StateFlow<Int> = preferencesRepository.stickyQuizNameDurationSecondsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun updateStickyQuizNameDurationSeconds(duration: Int) {
        viewModelScope.launch {
            preferencesRepository.updateStickyQuizNameDurationSeconds(duration)
        }
    }

    val stickyHomeworkNameDurationSeconds: StateFlow<Int> = preferencesRepository.stickyHomeworkNameDurationSecondsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun updateStickyHomeworkNameDurationSeconds(duration: Int) {
        viewModelScope.launch {
            preferencesRepository.updateStickyHomeworkNameDurationSeconds(duration)
        }
    }

    val behaviorInitialsMap: StateFlow<String> = preferencesRepository.behaviorInitialsMapFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun updateBehaviorInitialsMap(map: String) {
        viewModelScope.launch {
            preferencesRepository.updateBehaviorInitialsMap(map)
        }
    }

    val lastQuizName: StateFlow<String?> = preferencesRepository.lastQuizNameFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun updateLastQuizName(name: String) {
        viewModelScope.launch {
            preferencesRepository.updateLastQuizName(name)
        }
    }

    val lastQuizTimestamp: StateFlow<Long?> = preferencesRepository.lastQuizTimestampFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val lastHomeworkName: StateFlow<String?> = preferencesRepository.lastHomeworkNameFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun updateLastHomeworkName(name: String) {
        viewModelScope.launch {
            preferencesRepository.updateLastHomeworkName(name)
        }
    }

    val lastHomeworkTimestamp: StateFlow<Long?> = preferencesRepository.lastHomeworkTimestampFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val noAnimations: StateFlow<Boolean> = preferencesRepository.noAnimationsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateNoAnimations(noAnimations: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNoAnimations(noAnimations)
        }
    }

    val autosaveInterval: StateFlow<Int> = preferencesRepository.autosaveIntervalFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 30000)

    fun updateAutosaveInterval(interval: Int) {
        viewModelScope.launch {
            preferencesRepository.updateAutosaveInterval(interval)
        }
    }

    val gridSnapEnabled: StateFlow<Boolean> = preferencesRepository.gridSnapEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateGridSnapEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateGridSnapEnabled(enabled)
        }
    }

    val gridSize: StateFlow<Int> = preferencesRepository.gridSizeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 20)

    fun updateGridSize(size: Int) {
        viewModelScope.launch {
            preferencesRepository.updateGridSize(size)
        }
    }

    val showRulers: StateFlow<Boolean> = preferencesRepository.showRulersFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateShowRulers(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateShowRulers(show)
        }
    }

    val showGrid: StateFlow<Boolean> = preferencesRepository.showGridFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateShowGrid(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateShowGrid(show)
        }
    }

    val editModeEnabled: StateFlow<Boolean> = preferencesRepository.editModeEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateEditModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateEditModeEnabled(enabled)
        }
    }
}
