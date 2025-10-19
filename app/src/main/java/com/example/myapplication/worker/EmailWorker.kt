/*
package com.example.myapplication.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.EmailSchedule
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

@HiltWorker
class EmailWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val db: AppDatabase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val now = Calendar.getInstance()
            val currentHour = now.get(Calendar.HOUR_OF_DAY)
            val currentMinute = now.get(Calendar.MINUTE)
            val currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK) // Sunday is 1, Saturday is 7

            val schedules = db.emailScheduleDao().getAllSchedules().first()

            schedules.forEach { schedule ->
                if (schedule.hour == currentHour && schedule.minute == currentMinute && (schedule.daysOfWeek and (1 shl (currentDayOfWeek - 1))) != 0) {
                    sendEmail(schedule)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun sendEmail(schedule: EmailSchedule) {
        // Implement email sending logic here
    }
}
*/