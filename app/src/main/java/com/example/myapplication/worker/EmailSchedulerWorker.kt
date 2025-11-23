package com.example.myapplication.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.myapplication.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class EmailSchedulerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(applicationContext)
        val emailScheduleDao = db.emailScheduleDao()

        val schedules = emailScheduleDao.getAllSchedulesList()
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
                                "email_address" to schedule.recipientEmail,
                                "subject" to schedule.subject,
                                "body" to schedule.body,
                                "export_options" to schedule.exportOptionsJson
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
