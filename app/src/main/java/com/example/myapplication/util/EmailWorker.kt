package com.example.myapplication.util

import android.content.Context
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.exporter.ExportOptions
import com.example.myapplication.data.exporter.Exporter
import com.example.myapplication.preferences.AppPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import com.example.myapplication.data.exporter.ExportOptions

class EmailWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            val studentDao = db.studentDao()
            val behaviorEventDao = db.behaviorEventDao()
            val homeworkLogDao = db.homeworkLogDao()
            val quizLogDao = db.quizLogDao()
            val studentGroupDao = db.studentGroupDao()
            val quizMarkTypeDao = db.quizMarkTypeDao()
            val customHomeworkTypeDao = db.customHomeworkTypeDao()
            val customHomeworkStatusDao = db.customHomeworkStatusDao()

            val exporter = Exporter(applicationContext)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val preferencesRepository = AppPreferencesRepository(applicationContext)
            val from = preferencesRepository.defaultEmailAddressFlow.first()
            val password = preferencesRepository.emailPasswordFlow.first()

            if (password.isNullOrBlank()) {
                return@withContext Result.failure()
            }

            val to = inputData.getString("email_address") ?: from
            val exportOptionsJson = inputData.getString("export_options")
            val exportOptions = if (exportOptionsJson != null) {
                Json.decodeFromString<ExportOptions>(exportOptionsJson)
            } else {
                ExportOptions()
            }

            val students = studentDao.getAllStudentsNonLiveData()
            val behaviorEvents = behaviorEventDao.getAllBehaviorEventsList()
            val homeworkLogs = homeworkLogDao.getAllHomeworkLogsList()
            val quizLogs = quizLogDao.getAllQuizLogsList()
            val studentGroups = studentGroupDao.getAllStudentGroupsList()
            val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()
            val customHomeworkTypes = customHomeworkTypeDao.getAllCustomHomeworkTypesList()
            val customHomeworkStatuses = customHomeworkStatusDao.getAllCustomHomeworkStatusesList()

            val file = File(applicationContext.cacheDir, "export.xlsx")
            val uri = FileProvider.getUriForFile(
                applicationContext,
                "com.example.myapplication.fileprovider",
                file
            )

            exporter.export(
                uri = uri,
                options = exportOptions,
                students = students,
                behaviorEvents = behaviorEvents,
                homeworkLogs = homeworkLogs,
                quizLogs = quizLogs,
                studentGroups = studentGroups,
                quizMarkTypes = quizMarkTypes,
                customHomeworkTypes = customHomeworkTypes,
                customHomeworkStatuses = customHomeworkStatuses,
                encrypt = exportOptions.encrypt
            )

            EmailUtil(applicationContext).sendEmail(
                from = from,
                password = password,
                to = to,
                subject = "Exported Data - ${dateFormat.format(Date())}",
                body = "Please find the exported data attached.",
                attachmentPath = file.absolutePath
            )

            return@withContext Result.success()
        } catch (e: Exception) {
            return@withContext Result.failure()
        }
    }
}
