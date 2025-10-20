package com.example.myapplication.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.PendingEmail
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
        val pendingEmailDao = db.pendingEmailDao()

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
                    pendingEmailDao.insert(
                        PendingEmail(
                            recipientAddress = schedule.recipientEmail,
                            subject = schedule.subject,
                            body = schedule.body,
                            timestamp = System.currentTimeMillis()
                        )
                    )
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
