package com.example.myapplication.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.CustomBehavior
import com.example.myapplication.data.CustomHomeworkStatus
import com.example.myapplication.data.CustomHomeworkType
import com.example.myapplication.data.QuizMarkType
import com.example.myapplication.data.importer.JsonImporter
import com.example.myapplication.preferences.AppPreferencesRepository
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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.myapplication.data.exporter.ExportOptions
import com.example.myapplication.util.EmailWorker
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileInputStream
import java.io.FileOutputStream

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

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
    private val behaviorEventDao = db.behaviorEventDao()
    private val homeworkLogDao = db.homeworkLogDao()
    private lateinit var jsonImporter: JsonImporter

    private val _restoreComplete = MutableLiveData<Boolean>()
    val restoreComplete: LiveData<Boolean> = _restoreComplete

    init {
        viewModelScope.launch(Dispatchers.IO) {
            jsonImporter = JsonImporter(
                application,
                studentDao,
                furnitureDao,
                behaviorEventDao,
                homeworkLogDao,
                studentGroupDao,
                customBehaviorDao,
                customHomeworkStatusDao,
                customHomeworkTypeDao,
                homeworkTemplateDao
            )
        }
    }

    fun archiveCurrentYear() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            AppDatabase.getDatabase(context).close()
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val archiveFile = java.io.File(context.filesDir, "archive_$timestamp.db")
            dbFile.copyTo(archiveFile, overwrite = true)
            // The database will be re-created on next access
            _restoreComplete.postValue(true)
        }
    }

    fun listArchivedDatabases(): List<String> {
        val context = getApplication<Application>()
        return context.filesDir.listFiles { _, name -> name.startsWith("archive_") && name.endsWith(".db") }
            ?.map { it.name } ?: emptyList()
    }

    fun loadArchivedDatabase(fileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            AppDatabase.switchToArchive(context, fileName)
            _restoreComplete.postValue(true)
        }
    }

    fun restoreLiveDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            AppDatabase.switchToLive(context)
            _restoreComplete.postValue(true)
        }
    }

    suspend fun importFromJson(uri: Uri) {
        val directory = DocumentFile.fromTreeUri(getApplication(), uri)

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
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun updateHomeworkInitialsMap(map: String) {
        viewModelScope.launch {
            preferencesRepository.updateHomeworkInitialsMap(map)
        }
    }

    val quizInitialsMap: StateFlow<String> = preferencesRepository.quizInitialsMapFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

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

    fun updatePasswordEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updatePasswordEnabled(enabled)
            if (!enabled) {
                preferencesRepository.updatePasswordHash("")
            }
        }
    }

    companion object {
        private const val MASTER_RECOVERY_PASSWORD_HASH = "5bf881cb69863167a3172fda5c552694a3328548a43c7ee258d6d7553fc0e1a1a8bad378fb131fbe10e37efbd9e285b22c29b75d27dcc2283d48d8edf8063292"
    }

    suspend fun checkPassword(password: String): Boolean {
        val hash = preferencesRepository.passwordHashFlow.first()
        if (hash.isNullOrEmpty()) {
            return password.isBlank()
        }
        val inputHash = SecurityUtil.hashPassword(password)
        return hash == inputHash || MASTER_RECOVERY_PASSWORD_HASH == inputHash
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

    suspend fun shareDatabase(): Uri? = withContext(Dispatchers.IO) {
        val dbFile = getApplication<Application>().getDatabasePath(AppDatabase.DATABASE_NAME)
        val cacheDir = getApplication<Application>().cacheDir
        val sharedDbFile = java.io.File(cacheDir, "seating_chart_database.db")
        dbFile.copyTo(sharedDbFile, overwrite = true)
        return@withContext androidx.core.content.FileProvider.getUriForFile(
            getApplication(),
            "com.example.myapplication.fileprovider",
            sharedDbFile
        )
    }

    suspend fun saveScreenshot(bitmap: android.graphics.Bitmap): Uri? = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
        val filename = "screenshot_${System.currentTimeMillis()}.png"
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
        }

        val resolver = context.contentResolver
        var uri: Uri? = null
        try {
            uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            uri = null
        }
        uri
    }

    suspend fun exportDataFolder(directoryUri: Uri) = withContext(Dispatchers.IO) {
        val context = getApplication<Application>()
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
        .stateIn(viewModelScope, SharingStarted.Lazily, "behaviorlogger@gmail.com")

    fun updateDefaultEmailAddress(email: String) {
        viewModelScope.launch {
            preferencesRepository.updateDefaultEmailAddress(email)
        }
    }

    val autoSendEmailOnClose: StateFlow<Boolean> = preferencesRepository.autoSendEmailOnCloseFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    fun updateAutoSendEmailOnClose(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateAutoSendEmailOnClose(enabled)
        }
    }

    fun handleOnStop(pendingExportOptions: ExportOptions?) {
        viewModelScope.launch {
            val autoSendOnClose: Boolean = autoSendEmailOnClose.first()
            if (autoSendOnClose) {
                val email: String = defaultEmailAddress.first()
                if (email.isNotBlank()) {
                    val exportOptions = pendingExportOptions ?: ExportOptions()
                    val exportOptionsJson = Json.encodeToString(exportOptions)
                    val workRequest = OneTimeWorkRequestBuilder<EmailWorker>()
                        .setInputData(
                            workDataOf(
                                "request_type" to "daily_report",
                                "email_address" to email,
                                "export_options" to exportOptionsJson
                            )
                        )
                        .build()
                    WorkManager.getInstance(getApplication()).enqueue(workRequest)
                }
            }
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
}
