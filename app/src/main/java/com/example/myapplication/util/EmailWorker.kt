package com.example.myapplication.util

import android.content.Context
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.EmailRepository
import com.example.myapplication.data.SmtpSettings
import com.example.myapplication.data.exporter.ExportOptions
import com.example.myapplication.data.exporter.Exporter
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.util.SecurityUtil
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

        val securityUtil = SecurityUtil(applicationContext)
        val preferencesRepository = AppPreferencesRepository(applicationContext, securityUtil)
        val from = preferencesRepository.defaultEmailAddressFlow.first()
        val password = preferencesRepository.emailPasswordFlow.first()
        val smtpSettings = preferencesRepository.smtpSettingsFlow.first()

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

                val subject = inputData.getString("subject")?.let { securityUtil.decryptSafe(it) } ?: "Daily Report - ${dateFormat.format(Date())}"
                val body = inputData.getString("body")?.let { securityUtil.decryptSafe(it) } ?: "Please find the daily report attached."
                val options = inputData.getString("export_options")?.let {
                    try {
                        val decrypted = securityUtil.decryptSafe(it)
                        Json.decodeFromString<ExportOptions>(decrypted)
                    } catch (e: Exception) {
                        ExportOptions()
                    }
                } ?: ExportOptions()

                val finalOptions = options.relativeDateRange?.let { range ->
                    val calendar = Calendar.getInstance()
                    val endDate = calendar.timeInMillis
                    when (range) {
                        "Past 24 hours" -> calendar.add(Calendar.HOUR, -24)
                        "Past 7 days" -> calendar.add(Calendar.DAY_OF_YEAR, -7)
                        "Past 30 days" -> calendar.add(Calendar.DAY_OF_YEAR, -30)
                        else -> null
                    }?.let {
                        options.copy(startDate = calendar.timeInMillis, endDate = endDate)
                    }
                } ?: options

                val file = File(applicationContext.cacheDir, "daily_report.xlsx")
                val uri = FileProvider.getUriForFile(
                    applicationContext,
                    "com.example.myapplication.fileprovider",
                    file
                )

                exporter.export(
                    uri = uri,
                    options = finalOptions,
                    students = students,
                    behaviorEvents = behaviorEvents,
                    homeworkLogs = homeworkLogs,
                    quizLogs = quizLogs,
                    studentGroups = studentGroups,
                    quizMarkTypes = quizMarkTypes,
                    customHomeworkTypes = customHomeworkTypes,
                    customHomeworkStatuses = customHomeworkStatuses,
                    encrypt = finalOptions.encrypt
                )

                val to = inputData.getString("email_address")?.let { securityUtil.decryptSafe(it) } ?: from
                if (to.isBlank()) {
                    return@withContext Result.failure()
                }

                EmailUtil(applicationContext).sendEmail(
                    from = from,
                    password = password,
                    to = to,
                    subject = subject,
                    body = "Please find the daily report attached.",
                    attachmentPath = file.absolutePath,
                    smtpSettings = smtpSettings
                )
            }
            "send_email" -> {
                val to = inputData.getString("to")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
                val subject = inputData.getString("subject")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
                val body = inputData.getString("body")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
                val attachmentPath = inputData.getString("attachment_path")?.let { securityUtil.decryptSafe(it) }
                val workerSmtpSettings = inputData.getString("smtp_settings")?.let {
                    try {
                        val decrypted = securityUtil.decryptSafe(it)
                        Json.decodeFromString<SmtpSettings>(decrypted)
                    } catch (e: Exception) {
                        smtpSettings
                    }
                } ?: smtpSettings

                EmailUtil(applicationContext).sendEmail(
                    from = from,
                    password = password,
                    to = to,
                    subject = subject,
                    body = body,
                    attachmentPath = attachmentPath,
                    smtpSettings = workerSmtpSettings
                )
            }
            "process_pending_emails" -> {
                val emailRepository = EmailRepository(db.emailScheduleDao(), pendingEmailDao, securityUtil)
                val pendingEmails = emailRepository.getAllPendingEmails()
                pendingEmails.forEach { email ->
                    EmailUtil(applicationContext).sendEmail(
                        from = from,
                        password = password,
                        to = email.recipientAddress,
                        subject = email.subject,
                        body = email.body,
                        smtpSettings = smtpSettings
                    )
                    emailRepository.deletePendingEmail(email.id)
                }
            }
            "on_stop_export" -> {
                val to = inputData.getString("email_address")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
                val exportOptionsJson = inputData.getString("export_options")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
                val exportOptions = Json.decodeFromString<ExportOptions>(exportOptionsJson)
                val file = File(applicationContext.cacheDir, "on_stop_export.xlsx")
                val uri = FileProvider.getUriForFile(
                    applicationContext,
                    "com.example.myapplication.fileprovider",
                    file
                )
                val students = studentDao.getAllStudentsNonLiveData()
                val behaviorEvents = behaviorEventDao.getAllBehaviorEventsList()
                val homeworkLogs = homeworkLogDao.getAllHomeworkLogsList()
                val quizLogs = quizLogDao.getAllQuizLogsList()
                val studentGroups = studentGroupDao.getAllStudentGroupsList()
                val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()
                val customHomeworkTypes = customHomeworkTypeDao.getAllCustomHomeworkTypesList()
                val customHomeworkStatuses = customHomeworkStatusDao.getAllCustomHomeworkStatusesList()

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
                    subject = "Seating Chart Export",
                    body = "Attached is your requested data export.",
                    attachmentPath = file.absolutePath,
                    smtpSettings = smtpSettings
                )
            }
            else -> return@withContext Result.failure()
        }

        return@withContext Result.success()
    }
}
