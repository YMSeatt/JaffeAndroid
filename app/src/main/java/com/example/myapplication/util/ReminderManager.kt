package com.example.myapplication.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.myapplication.data.Reminder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ReminderManager: The primary authority for teacher reminder scheduling.
 *
 * This utility serves as the modern, Hilt-enabled implementation for managing [AlarmManager]
 * registrations. It is the core engine behind the Reminders screen accessible from the
 * main seating chart view.
 *
 * ### Key Characteristics:
 * - **Integration**: Works in tandem with [ReminderReceiver].
 * - **DI**: fully supported by Hilt for constructor injection.
 * - **Consistency**: Uses **lowercase** intent keys (e.g., `reminder_id`) for broadcast data.
 * - **Modernity**: Handles API 31+ exact alarm permission checks.
 *
 * Note: This implementation is distinct from the legacy [com.example.myapplication.alarm.AlarmScheduler].
 */
@Singleton
class ReminderManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Checks if the application has permission to schedule exact alarms.
     * On Android 12 (API 31) and above, this requires the [android.Manifest.permission.SCHEDULE_EXACT_ALARM]
     * permission and user approval in system settings.
     *
     * @return True if exact alarms can be scheduled, false otherwise.
     */
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Schedules a [Reminder] to trigger a notification at its specified timestamp.
     * Uses [AlarmManager.setExact] to ensure precise delivery.
     *
     * @param reminder The reminder entity containing the title, description, and trigger time.
     */
    fun scheduleReminder(reminder: Reminder) {
        if (!canScheduleExactAlarms()) {
            Log.d("ReminderManager", "Cannot schedule exact alarms. App needs permission.")
            // Optionally, you could throw an exception or return a status
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_description", reminder.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminder.timestamp, pendingIntent)
    }

    /**
     * Cancels a previously scheduled reminder.
     *
     * @param reminderId The unique identifier of the reminder to cancel.
     */
    fun cancelReminder(reminderId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}