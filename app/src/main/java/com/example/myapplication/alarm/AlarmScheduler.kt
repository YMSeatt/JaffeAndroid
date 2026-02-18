package com.example.myapplication.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.myapplication.data.Reminder
import java.util.Calendar

/**
 * AlarmScheduler: A secondary, legacy implementation for scheduling teacher reminders.
 *
 * This class is part of a redundant alarm subsystem that overlaps with the primary
 * [com.example.myapplication.util.ReminderManager]. It uses distinct **uppercase**
 * intent keys and works in conjunction with the [AlarmReceiver].
 *
 * ### ⚠️ Redundancy Note:
 * This implementation is currently considered **legacy**. Developers should prefer
 * using [com.example.myapplication.util.ReminderManager] for all new reminder logic.
 * Furthermore, the associated [AlarmReceiver] is not registered in the Manifest,
 * making this scheduler non-functional for system-level notifications.
 */
@Deprecated(
    message = "Use ReminderManager for the primary, Hilt-enabled reminder implementation.",
    replaceWith = ReplaceWith("ReminderManager", "com.example.myapplication.util.ReminderManager")
)
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules a [Reminder] using [AlarmManager.setExact].
     *
     * @param reminder The reminder to be scheduled.
     */
    fun schedule(reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("REMINDER_DESCRIPTION", reminder.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = reminder.timestamp
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    /**
     * Cancels a previously scheduled reminder broadcast.
     *
     * @param reminder The reminder to cancel.
     */
    fun cancel(reminder: Reminder) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
