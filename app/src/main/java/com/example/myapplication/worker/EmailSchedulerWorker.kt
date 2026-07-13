package com.example.myapplication.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.EmailRepository
import com.example.myapplication.util.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * EmailSchedulerWorker: The periodic evaluation engine for automated reporting.
 *
 * This [CoroutineWorker] acts as the "Metronome" for the application's scheduled email system.
 * It is registered as a unique periodic task (typically every 15 minutes) and is responsible
 * for scanning the [EmailRepository] to identify any reports that are due for transmission.
 *
 * ### Architectural Role:
 * - **Schedule Evaluation**: Compares the current system time and day against the user-defined
 *   schedules stored in the database.
 * - **Task Dispatcher**: Instead of performing the heavy lifting (Excel generation and SMTP)
 *   itself, it dispatches specialized [com.example.myapplication.util.EmailWorker] tasks
 *   via [WorkManager]. This ensures that scheduling logic is isolated from execution logic.
 *
 * ### Performance & Security:
 * - **Dispatchers.IO**: Runs strictly on the IO thread to avoid blocking background task slots.
 * - **Encrypted Handshake**: Ensures that all sensitive schedule metadata is encrypted using
 *   [SecurityUtil] before being passed to the execution worker.
 */
class EmailSchedulerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    /**
     * Executes the periodic check for due reports.
     *
     * 1. Retrieves all active schedules from the [EmailRepository].
     * 2. Matches the current [Calendar] state (Hour, Minute, Day) against each schedule.
     * 3. If a match is found, enqueues a standard `daily_report` task for the [EmailWorker].
     *
     * @return [Result.success] once all schedules have been evaluated.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(applicationContext)
        val securityUtil = SecurityUtil(applicationContext)
        val emailRepository = EmailRepository(db.emailScheduleDao(), db.pendingEmailDao(), securityUtil)

        val schedules = emailRepository.getAllSchedulesList()
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        schedules.forEach { schedule ->
            if (schedule.enabled) {
                val dayMatches = schedule.days.contains(getDayString(dayOfWeek))
                val timeMatches = schedule.hour == hour && schedule.minute == minute
                if (dayMatches && timeMatches) {
                    val workRequest = OneTimeWorkRequestBuilder<com.example.myapplication.util.EmailWorker>()
                        .setInputData(
                            workDataOf(
                                "request_type" to "daily_report",
                                "email_address" to securityUtil.encrypt(schedule.recipientEmail),
                                "subject" to securityUtil.encrypt(schedule.subject),
                                "body" to securityUtil.encrypt(schedule.body),
                                "export_options" to (schedule.exportOptionsJson?.let { securityUtil.encrypt(it) } ?: "")
                            )
                        )
                        .build()
                    WorkManager.getInstance(applicationContext).enqueue(workRequest)
                }
            }
        }

        return@withContext Result.success()
    }

    private fun getDayString(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }
    }
}
