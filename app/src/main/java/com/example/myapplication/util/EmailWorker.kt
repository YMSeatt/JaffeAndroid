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

/**
 * EmailWorker: A high-performance background task handler for classroom reporting.
 *
 * This [CoroutineWorker] implementation coordinates the generation, encryption, and
 * transmission of classroom data reports. It serves as the primary engine for all
 * asynchronous email operations, ensuring that heavy tasks like Excel generation
 * and multi-pass database synthesis do not impact UI responsiveness.
 *
 * ### Security Model:
 * To protect Personally Identifiable Information (PII), all sensitive data passed to
 * this worker via `inputData` (emails, passwords, report configurations) must be
 * encrypted using [SecurityUtil]. The worker automatically decrypts these values
 * before execution.
 *
 * ### Architectural Performance (BOLT):
 * - **Single-Pass Synthesis**: Aggregates data from multiple DAOs in a single IO block.
 * - **Resource Isolation**: Runs strictly on [Dispatchers.IO].
 * - **Resource Cleanup**: Employs mandatory `finally` blocks to ensure temporary report
 *   files are purged from local storage immediately after transmission.
 */
class EmailWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    /**
     * Executes the requested background task based on the provided "request_type".
     *
     * #### Supported Operations:
     * 1. **`daily_report`**: Generates a periodic classroom analytics report based on
     *    relative date ranges (e.g., "Past 7 days").
     * 2. **`send_email`**: Standard transactional email delivery with support for attachments.
     * 3. **`process_pending_emails`**: Reliability handler that flushes the [PendingEmail]
     *    queue (failed or deferred sends).
     * 4. **`on_stop_export`**: Triggered by the application lifecycle to ensure an up-to-date
     *    data backup is safely emailed when the app is closed.
     *
     * @return [Result.success] if the task completes, or [Result.failure] if mandatory
     *         credentials (like the SMTP password) are missing or decryption fails.
     */
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

            // BOLT: Instantiate repository once for reuse across request types
            val repository = com.example.myapplication.data.StudentRepository(
                studentDao, behaviorEventDao, homeworkLogDao, quizLogDao,
                db.furnitureDao(), db.layoutTemplateDao(), quizMarkTypeDao, applicationContext
            )

            when (requestType) {
                "daily_report" -> {
                    // HARDEN: Use unique temporary filename in restricted shared cache directory
                    val sharedDir = File(applicationContext.cacheDir, "shared")
                    if (!sharedDir.exists()) sharedDir.mkdirs()
                    val file = File.createTempFile("daily_report_", ".xlsx", sharedDir)
                    try {
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

                        val startDate = finalOptions.startDate ?: 0L
                        val endDate = finalOptions.endDate ?: Long.MAX_VALUE
                        val studentIds = finalOptions.studentIds

                        // BOLT: Use centralized filtered fetch logic
                        val students = repository.getFilteredStudents(studentIds)
                        val behaviorEvents = repository.getFilteredBehaviorEvents(startDate, endDate, studentIds)
                        val homeworkLogs = repository.getFilteredHomeworkLogs(startDate, endDate, studentIds)
                        val quizLogs = repository.getFilteredQuizLogs(startDate, endDate, studentIds)

                        val studentGroups = studentGroupDao.getAllStudentGroupsList()
                        val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()
                        val customHomeworkTypes = customHomeworkTypeDao.getAllCustomHomeworkTypesList()
                        val customHomeworkStatuses = customHomeworkStatusDao.getAllCustomHomeworkStatusesList()

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
                        if (to.isNotBlank()) {
                            EmailUtil(applicationContext).sendEmail(
                                from = from,
                                password = password,
                                to = to,
                                subject = subject,
                                body = body,
                                attachmentPath = file.absolutePath,
                                smtpSettings = smtpSettings
                            )
                        }
                    } finally {
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }
                "send_email" -> {
                    val attachmentPath = inputData.getString("attachment_path")?.let { securityUtil.decryptSafe(it) }
                    try {
                        val to = inputData.getString("to")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
                        val subject = inputData.getString("subject")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
                        val body = inputData.getString("body")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
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
                    } finally {
                        attachmentPath?.let { path ->
                            val file = File(path)
                            if (file.exists()) {
                                file.delete()
                            }
                        }
                    }
                }
                "process_pending_emails" -> {
                    val emailRepository = EmailRepository(db.emailScheduleDao(), pendingEmailDao, securityUtil)
                    val pendingEmails = emailRepository.getAllPendingEmails()
                    pendingEmails.forEach { email ->
                        try {
                            EmailUtil(applicationContext).sendEmail(
                                from = from,
                                password = password,
                                to = email.recipientAddress,
                                subject = email.subject,
                                body = email.body,
                                smtpSettings = smtpSettings
                            )
                            emailRepository.deletePendingEmail(email.id)
                        } catch (e: Exception) {
                            Log.e("EmailWorker", "Failed to send pending email ${email.id}", e)
                        }
                    }
                }
                "on_stop_export" -> {
                    // HARDEN: Use unique temporary filename in restricted shared cache directory
                    val sharedDir = File(applicationContext.cacheDir, "shared")
                    if (!sharedDir.exists()) sharedDir.mkdirs()
                    val file = File.createTempFile("on_stop_export_", ".xlsx", sharedDir)
                    try {
                        val to = inputData.getString("email_address")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
                        val exportOptionsJson = inputData.getString("export_options")?.let { securityUtil.decryptSafe(it) } ?: return@withContext Result.failure()
                        val exportOptions = Json.decodeFromString<ExportOptions>(exportOptionsJson)

                        val startDate = exportOptions.startDate ?: 0L
                        val endDate = exportOptions.endDate ?: Long.MAX_VALUE
                        val studentIds = exportOptions.studentIds

                        // BOLT: Use centralized filtered fetch logic
                        val students = repository.getFilteredStudents(studentIds)
                        val behaviorEvents = repository.getFilteredBehaviorEvents(startDate, endDate, studentIds)
                        val homeworkLogs = repository.getFilteredHomeworkLogs(startDate, endDate, studentIds)
                        val quizLogs = repository.getFilteredQuizLogs(startDate, endDate, studentIds)

                        val studentGroups = studentGroupDao.getAllStudentGroupsList()
                        val quizMarkTypes = quizMarkTypeDao.getAllQuizMarkTypesList()
                        val customHomeworkTypes = customHomeworkTypeDao.getAllCustomHomeworkTypesList()
                        val customHomeworkStatuses = customHomeworkStatusDao.getAllCustomHomeworkStatusesList()

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
                            subject = "Seating Chart Export",
                            body = "Attached is your requested data export.",
                            attachmentPath = file.absolutePath,
                            smtpSettings = smtpSettings
                        )
                    } finally {
                        if (file.exists()) {
                            file.delete()
                        }
                    }
                }
                else -> return@withContext Result.failure()
            }

            return@withContext Result.success()
        } catch (e: Exception) {
            Log.e("EmailWorker", "Critical failure in EmailWorker", e)
            return@withContext Result.failure()
        }
    }
}
