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
 * registrations. It acts as the core coordination engine between the UI (RemindersScreen)
 * and the Android system alarm service.
 *
 * ### Architectural Role:
 * - **Centralized Control**: Ensures all alarms are scheduled through a single, consistent path.
 * - **Modern Compatibility**: Gracefully handles API 31+ restrictions regarding exact alarms.
 * - **Security-First**: Enforces [PendingIntent.FLAG_IMMUTABLE] to prevent external manipulation
 *   of scheduled alarms.
 *
 * ### Intent Protocol:
 * To ensure cross-platform compatibility and system consistency, this manager utilizes
 * **strictly lowercase** intent keys (e.g., `reminder_id`).
 *
 * Note: This implementation supersedes the decommissioned legacy alarm scheduler.
 */
@Singleton
class ReminderManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Checks if the application has permission to schedule exact alarms.
     *
     * Beginning with Android 12 (API 31), apps must hold the [android.Manifest.permission.SCHEDULE_EXACT_ALARM]
     * permission and it must be granted by the user in system settings. This method provides
     * a safe check before attempting to call [AlarmManager.setExact].
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
     *
     * This method utilizes [AlarmManager.setExact] with [AlarmManager.RTC_WAKEUP] to ensure
     * that the teacher is notified at the precise moment requested, even if the device
     * is currently in a low-power state.
     *
     * ### Implementation Safety:
     * - **ID Collision**: Uses the [Reminder.id] as the `requestCode` to ensure that
     *   multiple reminders do not overwrite each other in the system registry.
     * - **Immutability**: Appends [PendingIntent.FLAG_IMMUTABLE] to satisfy Android 12+
     *   security requirements and prevent malicious tampering with the intent data.
     * - **Naming**: Adheres to lowercase intent keys (`reminder_id`, etc.) for consistency.
     *
     * @param reminder The reminder entity containing the title, description, and trigger time.
     */
    fun scheduleReminder(reminder: Reminder) {
        if (!canScheduleExactAlarms()) {
            Log.w("ReminderManager", "Permission denied: Cannot schedule exact alarm for reminder ${reminder.id}.")
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
     * Cancels a previously scheduled reminder in the system [AlarmManager].
     *
     * To successfully cancel an alarm, the [PendingIntent] must exactly match the one
     * used during scheduling (including the `requestCode` and `Intent` action/component).
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