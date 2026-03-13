package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEventDao
import com.example.myapplication.data.ConditionalFormattingRuleDao
import com.example.myapplication.data.CustomBehavior
import com.example.myapplication.data.CustomBehaviorDao
import com.example.myapplication.data.CustomHomeworkStatus
import com.example.myapplication.data.CustomHomeworkStatusDao
import com.example.myapplication.data.CustomHomeworkType
import com.example.myapplication.data.CustomHomeworkTypeDao
import com.example.myapplication.data.FurnitureDao
import com.example.myapplication.data.HomeworkLogDao
import com.example.myapplication.data.HomeworkTemplateDao
import com.example.myapplication.data.LayoutTemplateDao
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.QuizMarkTypeDao
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.data.QuizTemplateDao
import com.example.myapplication.data.SmtpSettings
import com.example.myapplication.data.StudentDao
import com.example.myapplication.data.StudentGroupDao
import com.example.myapplication.data.importer.JsonImporter
import com.example.myapplication.preferences.AppPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.example.myapplication.preferences.AppTheme
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_BG_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_HEIGHT_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_OUTLINE_THICKNESS_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_PADDING_DP
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_TEXT_COLOR_HEX
import com.example.myapplication.preferences.DEFAULT_STUDENT_BOX_WIDTH_DP
import com.example.myapplication.util.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.util.Log
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * SettingsViewModel: Manages application configuration, data migration, and archival.
 *
 * This ViewModel handles:
 * 1. **User Preferences**: Synchronizing local settings (e.g., UI timeouts, theme, font styles)
 *    with the [AppPreferencesRepository].
 * 2. **Database Lifecycle**: Managing the archival of historical data into dedicated `archives/`
 *    directories and restoring from backups.
 * 3. **Data Ingestion**: Coordinating the import of classroom data from cross-platform JSON
 *    exports using the [JsonImporter].
 * 4. **System Category Management**: Providing interfaces to manage custom behaviors,
 *    homework types, and quiz mark templates.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val preferencesRepository: AppPreferencesRepository,
    private val jsonImporter: JsonImporter,
    private val studentDao: StudentDao,
    private val furnitureDao: FurnitureDao,
    private val studentGroupDao: StudentGroupDao,
    private val layoutTemplateDao: LayoutTemplateDao,
    private val conditionalFormattingRuleDao: ConditionalFormattingRuleDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val customHomeworkTypeDao: CustomHomeworkTypeDao,
    private val customHomeworkStatusDao: CustomHomeworkStatusDao,
    private val quizMarkTypeDao: QuizMarkTypeDao,
    private val quizTemplateDao: QuizTemplateDao,
    private val homeworkTemplateDao: HomeworkTemplateDao,
    private val systemBehaviorDao: com.example.myapplication.data.SystemBehaviorDao,
    private val behaviorEventDao: BehaviorEventDao,
    private val homeworkLogDao: HomeworkLogDao,
    private val securityUtil: SecurityUtil
) : ViewModel() {

    private val _restoreComplete = MutableLiveData<Boolean>()
    val restoreComplete: LiveData<Boolean> = _restoreComplete

    /**
     * Creates a point-in-time backup of the current database.
     *
     * **Hardening Strategy**: Archives are stored in a dedicated 'archives' subdirectory
     * within the app's private internal storage. This allows for easier exclusion from
     * standard Android cloud backups if desired and protects student PII.
     */
    fun archiveCurrentYear() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = application
            AppDatabase.getDatabase(context).close()
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

            // HARDEN: Use a dedicated archives directory to allow exclusion from cloud backups
            val archiveDir = File(context.filesDir, "archives")
            if (!archiveDir.exists()) archiveDir.mkdirs()

            val archiveFile = File(archiveDir, "archive_$timestamp.db")
            dbFile.copyTo(archiveFile, overwrite = true)
            // The database will be re-created on next access
            _restoreComplete.postValue(true)
        }
    }

    /**
     * Scans the app's internal storage for archived database files.
     *
     * **Privacy Fix**: Includes a one-time migration to move legacy archives from
     * the root internal directory into the hardened `archives/` subdirectory.
     *
     * @return A list of archive filenames (e.g., "archive_2024-05-01.db").
     */
    fun listArchivedDatabases(): List<String> {
        val context = application
        val archiveDir = File(context.filesDir, "archives")
        if (!archiveDir.exists()) archiveDir.mkdirs()

        // PRIVACY: One-time migration of legacy archives from the root files directory
        // to the hardened archives subdirectory.
        context.filesDir.listFiles { _, name -> name.startsWith("archive_") && name.endsWith(".db") }?.forEach { legacyFile ->
            try {
                val targetFile = File(archiveDir, legacyFile.name)
                if (!targetFile.exists()) {
                    legacyFile.renameTo(targetFile)
                } else {
                    legacyFile.delete()
                }
            } catch (e: Exception) {
                // Ignore migration errors for individual files
            }
        }

        return archiveDir.listFiles { _, name -> name.startsWith("archive_") && name.endsWith(".db") }
            ?.map { it.name } ?: emptyList()
    }

    /**
     * Switches the application's active database to a specific archive.
     * This is used for viewing historical data without overwriting the live classroom state.
     */
    fun loadArchivedDatabase(fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = application
            AppDatabase.switchToArchive(context, fileName)
            _restoreComplete.postValue(true)
        }
    }

    /**
     * Resets the application to use the primary production database.
     */
    fun restoreLiveDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = application
            AppDatabase.switchToLive(context)
            _restoreComplete.postValue(true)
        }
    }

    /**
     * Imports a complete classroom snapshot from fragmented JSON files.
     *
     * This method orchestrates the [JsonImporter] to process students, groups, behaviors,
     * and templates exported from the Python desktop application.
     */
    suspend fun importFromJson(uri: Uri) {
        val directory = DocumentFile.fromTreeUri(application, uri)

        val classroomDataFile = directory?.findFile("classroom_data_v10.json")
        val studentGroupsFile = directory?.findFile("student_groups_v10.json")
        val customBehaviorsFile = directory?.findFile("custom_behaviors_v10.json")
        val customHomeworkStatusesFile = directory?.findFile("custom_homework_statuses_v10.json")
        val customHomeworkTypesFile = directory?.findFile("custom_homework_types_v10.json")
        val homeworkTemplatesFile = directory?.findFile("homework_templates_v10.json")

        if (classroomDataFile != null && studentGroupsFile != null && customBehaviorsFile != null && customHomeworkStatusesFile != null && customHomeworkTypesFile != null && homeworkTemplatesFile != null) {
            jsonImporter.importData(
                classroomDataUri = classroomDataFile.uri,
                studentGroupsUri = studentGroupsFile.uri,
                customBehaviorsUri = customBehaviorsFile.uri,
                customHomeworkStatusesUri = customHomeworkStatusesFile.uri,
                customHomeworkTypesUri = customHomeworkTypesFile.uri,
                homeworkTemplatesUri = homeworkTemplatesFile.uri
            )
        }
    }


    val recentLogsLimit: StateFlow<Int> = preferencesRepository.recentLogsLimitFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 3) // Default for homework logs

    fun updateRecentLogsLimit(limit: Int) {
        viewModelScope.launch {
            preferencesRepository.updateRecentLogsLimit(limit)
        }
    }

    val recentHomeworkLogsLimit: StateFlow<Int> = preferencesRepository.recentHomeworkLogsLimitFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 3)

    fun updateRecentHomeworkLogsLimit(limit: Int) {
        viewModelScope.launch {
            preferencesRepository.updateRecentHomeworkLogsLimit(limit)
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

    val useInitialsForHomework: StateFlow<Boolean> = preferencesRepository.useInitialsForHomeworkFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateUseInitialsForHomework(useInitials: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateUseInitialsForHomework(useInitials)
        }
    }

    val useInitialsForQuiz: StateFlow<Boolean> = preferencesRepository.useInitialsForQuizFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateUseInitialsForQuiz(useInitials: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateUseInitialsForQuiz(useInitials)
        }
    }

    val homeworkInitialsMap: StateFlow<String> = preferencesRepository.homeworkInitialsMapFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun updateHomeworkInitialsMap(map: String) {
        viewModelScope.launch {
            preferencesRepository.updateHomeworkInitialsMap(map)
        }
    }

    val quizInitialsMap: StateFlow<String> = preferencesRepository.quizInitialsMapFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun updateQuizInitialsMap(map: String) {
        viewModelScope.launch {
            preferencesRepository.updateQuizInitialsMap(map)
        }
    }

    val useFullNameForStudent: StateFlow<Boolean> = preferencesRepository.useFullNameForStudentFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateUseFullNameForStudent(useFullName: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateUseFullNameForStudent(useFullName)
        }
    }

    val useBoldFont: StateFlow<Boolean> = preferencesRepository.useBoldFontFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateUseBoldFont(useBoldFont: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateUseBoldFont(useBoldFont)
        }
    }

    val liveHomeworkSessionMode: StateFlow<String> = preferencesRepository.liveHomeworkSessionModeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "Yes/No")

    fun updateLiveHomeworkSessionMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.updateLiveHomeworkSessionMode(mode)
        }
    }

    val liveHomeworkSelectOptions: StateFlow<String> = preferencesRepository.liveHomeworkSelectOptionsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "Done,Not Done,Signed,Returned")

    fun updateLiveHomeworkSelectOptions(options: String) {
        viewModelScope.launch {
            preferencesRepository.updateLiveHomeworkSelectOptions(options)
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

    val emailPassword: StateFlow<String> = preferencesRepository.emailPasswordFlow
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun updateEmailPassword(password: String) {
        viewModelScope.launch {
            preferencesRepository.updateEmailPassword(password)
        }
    }

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

    fun resetBehaviorsToDefaults() = viewModelScope.launch {
        val defaults = listOf(
            "Talking", "Off Task", "Out of Seat", "Uneasy", "Placecheck",
            "Great Participation", "Called On", "Complimented", "Fighting", "Other"
        ).map { CustomBehavior(name = it) }
        customBehaviorDao.replaceAll(defaults)
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

    fun resetHomeworkAssignmentTypesToDefaults() = viewModelScope.launch {
        val defaults = listOf(
            "Reading Assignment", "Worksheet", "Math Problems", "Project Work", "Study for Test"
        ).map { CustomHomeworkType(name = it) }
        customHomeworkTypeDao.replaceAll(defaults)
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

    fun resetHomeworkStatusesToDefaults() = viewModelScope.launch {
        val defaults = listOf(
            "Done", "Not Done", "Partially Done", "Signed", "Returned", "Late", "Excellent Work"
        ).map { CustomHomeworkStatus(name = it) }
        customHomeworkStatusDao.replaceAll(defaults)
    }

    fun resetQuizMarkTypesToDefaults() = viewModelScope.launch {
        val defaults = listOf(
            QuizMarkType(name = "Correct", defaultPoints = 1.0, contributesToTotal = true, isExtraCredit = false),
            QuizMarkType(name = "Incorrect", defaultPoints = 0.0, contributesToTotal = true, isExtraCredit = false),
            QuizMarkType(name = "Partial Credit", defaultPoints = 0.5, contributesToTotal = true, isExtraCredit = false),
            QuizMarkType(name = "Bonus", defaultPoints = 1.0, contributesToTotal = false, isExtraCredit = true)
        )
        quizMarkTypeDao.replaceAll(defaults)
    }

    fun resetLiveHomeworkSelectOptionsToDefaults() {
        updateLiveHomeworkSelectOptions("Done,Not Done,Signed,Returned")
    }

    val quizMarkTypes: LiveData<List<QuizMarkType>> = quizMarkTypeDao.getAllQuizMarkTypes().asLiveData()
    val allQuizTemplates: LiveData<List<QuizTemplate>> = quizTemplateDao.getAll().asLiveData()
    val allSystemBehaviors: LiveData<List<com.example.myapplication.data.SystemBehavior>> = systemBehaviorDao.getAllSystemBehaviors().asLiveData()

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

    val defaultStudentBoxCornerRadius: StateFlow<Int> = preferencesRepository.defaultStudentBoxCornerRadiusFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_STUDENT_BOX_CORNER_RADIUS_DP)

    fun updateDefaultStudentBoxCornerRadius(cornerRadius: Int) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultStudentBoxCornerRadius(cornerRadius)
        }
    }

    val defaultStudentBoxPadding: StateFlow<Int> = preferencesRepository.defaultStudentBoxPaddingFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, DEFAULT_STUDENT_BOX_PADDING_DP)

    fun updateDefaultStudentBoxPadding(padding: Int) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultStudentBoxPadding(padding)
        }
    }

    val passwordEnabled: StateFlow<Boolean> = preferencesRepository.passwordEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val passwordAutoLockEnabled: StateFlow<Boolean> = preferencesRepository.passwordAutoLockEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updatePasswordAutoLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updatePasswordAutoLockEnabled(enabled)
        }
    }

    val passwordAutoLockTimeoutMinutes: StateFlow<Int> = preferencesRepository.passwordAutoLockTimeoutMinutesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 15)

    fun updatePasswordAutoLockTimeoutMinutes(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.updatePasswordAutoLockTimeoutMinutes(minutes)
        }
    }

    fun updatePasswordEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updatePasswordEnabled(enabled)
            if (!enabled) {
                preferencesRepository.updatePasswordHash("")
            }
        }
    }

    suspend fun checkPassword(password: String): Boolean {
        val storedHash = preferencesRepository.passwordHashFlow.first()
        if (storedHash.isNullOrEmpty()) {
            return password.isBlank()
        }

        if (SecurityUtil.verifyPassword(password, storedHash)) {
            // If it matches but is in a legacy format (unsalted or old salted), upgrade it automatically
            if (!storedHash.startsWith("pbkdf2:")) {
                preferencesRepository.updatePasswordHash(SecurityUtil.hashPassword(password))
            }
            return true
        }

        return false
    }

    fun setPassword(password: String) {
        viewModelScope.launch {
            if (password.isNotBlank()) {
                preferencesRepository.updatePasswordHash(SecurityUtil.hashPassword(password))
                updatePasswordEnabled(true)
            } else {
                preferencesRepository.updatePasswordHash("")
                updatePasswordEnabled(false)
            }
        }
    }

    /**
     * Copies the active database file to an external location.
     */
    suspend fun backupDatabase(uri: Uri) = withContext(Dispatchers.IO) {
        val dbFile = application.getDatabasePath(AppDatabase.DATABASE_NAME)
        val encrypt = preferencesRepository.encryptDataFilesFlow.first()
        application.contentResolver.openOutputStream(uri)?.use { outputStream ->
            val dbBytes = FileInputStream(dbFile).use { it.readBytes() }
            if (encrypt) {
                val encryptedToken = securityUtil.encrypt(dbBytes)
                outputStream.write(encryptedToken.toByteArray(Charsets.UTF_8))
            } else {
                outputStream.write(dbBytes)
            }
        }
    }

    /**
     * Overwrites the active database with a file from an external source.
     * Triggers a [restoreComplete] event upon success.
     */
    suspend fun restoreDatabase(uri: Uri) = withContext(Dispatchers.IO) {
        val dbFile = application.getDatabasePath(AppDatabase.DATABASE_NAME)
        AppDatabase.getDatabase(application).close()
        application.contentResolver.openInputStream(uri)?.use { inputStream ->
            val inputBytes = inputStream.readBytes()

            val decryptedBytes = if (inputBytes.size >= 6 &&
                inputBytes[0] == 'g'.toByte() &&
                inputBytes[1] == 'A'.toByte() &&
                inputBytes[2] == 'A'.toByte() &&
                inputBytes[3] == 'A'.toByte() &&
                inputBytes[4] == 'A'.toByte() &&
                inputBytes[5] == 'A'.toByte()) {
                try {
                    securityUtil.decryptToByteArray(String(inputBytes, Charsets.UTF_8))
                } catch (e: Exception) {
                    inputBytes
                }
            } else {
                inputBytes
            }

            FileOutputStream(dbFile, false).use { outputStream ->
                outputStream.write(decryptedBytes)
            }
        }
        _restoreComplete.postValue(true) // This will trigger the restart
    }

    /**
     * Force-restarts the application.
     * Used after a database restoration to ensure all components are re-initialized
     * with the new data source.
     */
    fun triggerRebirth() {
        val packageManager = application.packageManager
        val intent = packageManager.getLaunchIntentForPackage(application.packageName)
        val componentName = intent!!.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        application.startActivity(mainIntent)
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
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

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
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun updateEditModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateEditModeEnabled(enabled)
        }
    }

    val autoExpandStudentBoxes: StateFlow<Boolean> = preferencesRepository.autoExpandStudentBoxesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun updateAutoExpandStudentBoxes(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoExpandStudentBoxes(enabled)
        }
    }

    val lastExportPath: StateFlow<String?> = preferencesRepository.lastExportPathFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    fun updateLastExportPath(path: String) {
        viewModelScope.launch {
            preferencesRepository.updateLastExportPath(path)
        }
    }

    val encryptDataFiles: StateFlow<Boolean> = preferencesRepository.encryptDataFilesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun updateEncryptDataFiles(encrypt: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateEncryptDataFiles(encrypt)
        }
    }

    /**
     * Prepares the database file for sharing via system intents.
     * Copies the DB to the hardened shared cache directory and provides a content URI.
     */
    suspend fun shareDatabase(): Uri? = withContext(Dispatchers.IO) {
        val dbFile = application.getDatabasePath(AppDatabase.DATABASE_NAME)
        val sharedDir = java.io.File(application.cacheDir, "shared")
        if (!sharedDir.exists()) sharedDir.mkdirs()
        val sharedDbFile = java.io.File(sharedDir, "seating_chart_database.db")

        val encrypt = preferencesRepository.encryptDataFilesFlow.first()
        val dbBytes = FileInputStream(dbFile).use { it.readBytes() }

        if (encrypt) {
            val encryptedToken = securityUtil.encrypt(dbBytes)
            sharedDbFile.writeText(encryptedToken)
        } else {
            sharedDbFile.writeBytes(dbBytes)
        }

        return@withContext androidx.core.content.FileProvider.getUriForFile(
            application,
            "com.example.myapplication.fileprovider",
            sharedDbFile
        )
    }

    /**
     * Persists a seating chart screenshot to the app's hardened shared cache.
     * @return A content URI for sharing the image.
     */
    suspend fun saveScreenshot(bitmap: android.graphics.Bitmap): Uri? = withContext(Dispatchers.IO) {
        val context = application
        val filename = "screenshot_${System.currentTimeMillis()}.png"
        val sharedDir = File(context.cacheDir, "shared")
        if (!sharedDir.exists()) sharedDir.mkdirs()
        val screenshotFile = File(sharedDir, filename)

        try {
            FileOutputStream(screenshotFile).use { outputStream ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            return@withContext androidx.core.content.FileProvider.getUriForFile(
                context,
                "com.example.myapplication.fileprovider",
                screenshotFile
            )
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Failed to save screenshot", e)
            null
        }
    }

    suspend fun saveBlueprint(svgContent: String): Uri? = withContext(Dispatchers.IO) {
        val context = application
        val filename = "blueprint_${System.currentTimeMillis()}.svg"
        val sharedDir = File(context.cacheDir, "shared")
        if (!sharedDir.exists()) sharedDir.mkdirs()
        val blueprintFile = File(sharedDir, filename)

        try {
            FileOutputStream(blueprintFile).use { outputStream ->
                outputStream.write(svgContent.toByteArray())
            }
            return@withContext androidx.core.content.FileProvider.getUriForFile(
                context,
                "com.example.myapplication.fileprovider",
                blueprintFile
            )
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Failed to save blueprint SVG", e)
            null
        }
    }

    suspend fun exportDataFolder(directoryUri: Uri) = withContext(Dispatchers.IO) {
        val context = application
        val contentResolver = context.contentResolver
        val directory = DocumentFile.fromTreeUri(context, directoryUri)

        val filesToExport = listOf(
            "classroom_data_v10.json",
            "student_groups_v10.json",
            "custom_behaviors_v10.json",
            "custom_homework_statuses_v10.json",
            "custom_homework_types_v10.json",
            "homework_templates_v10.json",
            "quiz_templates_v10.json"
        )

        filesToExport.forEach { fileName ->
            val file = directory?.createFile("application/json", fileName)
            file?.uri?.let { fileUri ->
                contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }
    }

    val defaultEmailAddress: StateFlow<String> = preferencesRepository.defaultEmailAddressFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "behaviorlogger@gmail.com")
    val defaultEmailAddressValue: String
        get() = defaultEmailAddress.value
    fun updateDefaultEmailAddress(email: String) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultEmailAddress(email)
        }
    }

    val autoSendEmailOnClose: StateFlow<Boolean> = preferencesRepository.autoSendEmailOnCloseFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val autoSendEmailOnCloseValue: Boolean
        get() = autoSendEmailOnClose.value

    fun updateAutoSendEmailOnClose(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoSendEmailOnClose(enabled)
        }
    }

    val canvasBackgroundColor: StateFlow<String> = preferencesRepository.canvasBackgroundColorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "#FFFFFF") // Default white color

    fun updateCanvasBackgroundColor(colorHex: String) {
        viewModelScope.launch {
            preferencesRepository.updateCanvasBackgroundColor(colorHex)
        }
    }

    val guidesStayWhenRulersHidden: StateFlow<Boolean> = preferencesRepository.guidesStayWhenRulersHiddenFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateGuidesStayWhenRulersHidden(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateGuidesStayWhenRulersHidden(enabled)
        }
    }

    val behaviorDisplayTimeout: StateFlow<Int> = preferencesRepository.behaviorDisplayTimeoutFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun updateBehaviorDisplayTimeout(timeout: Int) {
        viewModelScope.launch {
            preferencesRepository.updateBehaviorDisplayTimeout(timeout)
        }
    }

    val homeworkDisplayTimeout: StateFlow<Int> = preferencesRepository.homeworkDisplayTimeoutFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun updateHomeworkDisplayTimeout(timeout: Int) {
        viewModelScope.launch {
            preferencesRepository.updateHomeworkDisplayTimeout(timeout)
        }
    }

    val quizDisplayTimeout: StateFlow<Int> = preferencesRepository.quizDisplayTimeoutFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun updateQuizDisplayTimeout(timeout: Int) {
        viewModelScope.launch {
            preferencesRepository.updateQuizDisplayTimeout(timeout)
        }
    }

    val smtpSettings: StateFlow<SmtpSettings> = preferencesRepository.smtpSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, SmtpSettings())

    fun updateSmtpSettings(smtpSettings: SmtpSettings) {
        viewModelScope.launch {
            preferencesRepository.updateSmtpSettings(smtpSettings)
        }
    }

    val liveQuizQuestionsGoal: StateFlow<Int> = preferencesRepository.liveQuizQuestionsGoalFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 5)

    fun updateLiveQuizQuestionsGoal(goal: Int) {
        viewModelScope.launch {
            preferencesRepository.updateLiveQuizQuestionsGoal(goal)
        }
    }

    val liveQuizInitialColor: StateFlow<String> = preferencesRepository.liveQuizInitialColorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "#FFFF0000")

    fun updateLiveQuizInitialColor(color: String) {
        viewModelScope.launch {
            preferencesRepository.updateLiveQuizInitialColor(color)
        }
    }

    val liveQuizFinalColor: StateFlow<String> = preferencesRepository.liveQuizFinalColorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "#FF00FF00")

    fun updateLiveQuizFinalColor(color: String) {
        viewModelScope.launch {
            preferencesRepository.updateLiveQuizFinalColor(color)
        }
    }

    val quizLogFontColor: StateFlow<String> = preferencesRepository.quizLogFontColorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "#FF006400")

    fun updateQuizLogFontColor(color: String) {
        viewModelScope.launch {
            preferencesRepository.updateQuizLogFontColor(color)
        }
    }

    val homeworkLogFontColor: StateFlow<String> = preferencesRepository.homeworkLogFontColorFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "#FF800080")

    fun updateHomeworkLogFontColor(color: String) {
        viewModelScope.launch {
            preferencesRepository.updateHomeworkLogFontColor(color)
        }
    }

    val quizLogFontBold: StateFlow<Boolean> = preferencesRepository.quizLogFontBoldFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun updateQuizLogFontBold(bold: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateQuizLogFontBold(bold)
        }
    }

    val homeworkLogFontBold: StateFlow<Boolean> = preferencesRepository.homeworkLogFontBoldFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    fun updateHomeworkLogFontBold(bold: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateHomeworkLogFontBold(bold)
        }
    }

    suspend fun importStudentsFromExcel(uri: Uri, studentRepository: com.example.myapplication.data.StudentRepository): Result<Int> {
        return com.example.myapplication.util.ExcelImportUtil.importStudentsFromExcel(
            uri, application, studentRepository, studentGroupDao
        )
    }
}
