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

class EmailWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(applicationContext)
        val studentDao = db.studentDao()
        val behaviorEventDao = db.behaviorEventDao()
        val homeworkLogDao = db.homeworkLogDao()
        val quizLogDao = db.quizLogDao()
        val studentGroupDao = db.studentGroupDao()
        val quizMarkTypeDao = db.quizMarkTypeDao()
        val customHomeworkTypeDao = db.customHomeworkTypeDao()
        val customHomeworkStatusDao = db.customHomeworkStatusDao()
        val pendingEmailDao = db.pendingEmailDao()

        val exporter = Exporter(applicationContext)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val preferencesRepository = AppPreferencesRepository(applicationContext)
        val from = preferencesRepository.defaultEmailAddressFlow.first()
        val password = preferencesRepository.emailPasswordFlow.first()

        if (password.isNullOrBlank()) {
            return@withContext Result.failure()
        }

        val requestType = inputData.getString("request_type")
        when (requestType) {
            "daily_report" -> {
                val students = studentDao.getAllStudentsNonLiveData()
                val behaviorEvents = behaviorEventDao.getAllBehaviorEventsList()
                val homeworkLogs = homeworkLogDao.getAllHomeworkLogsList()
                val quizLogs = quizLogDao.getAllQuizLogsList()
                val studentGroups = studentGroupDao.getAllStudentGroupsList()
                val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()
                val customHomeworkTypes = customHomeworkTypeDao.getAllCustomHomeworkTypesList()
                val customHomeworkStatuses = customHomeworkStatusDao.getAllCustomHomeworkStatusesList()

                val subject = "Daily Report - ${dateFormat.format(Date())}"
                val options = inputData.getString("export_options")?.let {
                    // This is a placeholder for a proper deserialization
                    ExportOptions()
                } ?: ExportOptions()

                val file = File(applicationContext.cacheDir, "daily_report.xlsx")
                val uri = FileProvider.getUriForFile(
                    applicationContext,
                    "com.example.myapplication.fileprovider",
                    file
                )

                exporter.export(
                    uri = uri,
                    options = options,
                    students = students,
                    behaviorEvents = behaviorEvents,
                    homeworkLogs = homeworkLogs,
                    quizLogs = quizLogs,
                    studentGroups = studentGroups,
                    quizMarkTypes = quizMarkTypes,
                    customHomeworkTypes = customHomeworkTypes,
                    customHomeworkStatuses = customHomeworkStatuses
                )

                val to = inputData.getString("email_address") ?: from
                if (to.isBlank()) {
                    return@withContext Result.failure()
                }

                EmailUtil(applicationContext).sendEmail(
                    from = from,
                    password = password,
                    to = to,
                    subject = subject,
                    body = "Please find the daily report attached.",
                    attachmentPath = file.absolutePath
                )
            }
            "send_email" -> {
                val to = inputData.getString("to") ?: return@withContext Result.failure()
                val subject = inputData.getString("subject") ?: return@withContext Result.failure()
                val body = inputData.getString("body") ?: return@withContext Result.failure()
                val attachmentPath = inputData.getString("attachment_path")

                EmailUtil(applicationContext).sendEmail(
                    from = from,
                    password = password,
                    to = to,
                    subject = subject,
                    body = body,
                    attachmentPath = attachmentPath
                )
            }
            "process_pending_emails" -> {
                val pendingEmails = pendingEmailDao.getAll()
                pendingEmails.forEach { email ->
                    EmailUtil(applicationContext).sendEmail(
                        from = from,
                        password = password,
                        to = email.recipientAddress,
                        subject = email.subject,
                        body = email.body
                    )
                    pendingEmailDao.delete(email.id)
                }
            }
            else -> return@withContext Result.failure()
        }

        return@withContext Result.success()
    }
}
