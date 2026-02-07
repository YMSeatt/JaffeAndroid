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

class EmailSchedulerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

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
