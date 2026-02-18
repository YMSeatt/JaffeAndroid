package com.example.myapplication.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.R

/**
 * AlarmReceiver: A legacy broadcast receiver for teacher reminders.
 *
 * This receiver is part of a redundant implementation that overlaps with
 * [com.example.myapplication.util.ReminderReceiver]. It expects **uppercase**
 * intent keys: `REMINDER_ID`, `REMINDER_TITLE`, and `REMINDER_DESCRIPTION`.
 *
 * ### ⚠️ Redundancy Note:
 * This receiver is currently **not registered** in the `AndroidManifest.xml`, which
 * means it will never trigger for system alarms. It is maintained for legacy
 * compatibility with older settings screens but should be avoided in favor of
 * [com.example.myapplication.util.ReminderReceiver].
 */
@Deprecated("Use ReminderReceiver for the primary reminder implementation.")
class AlarmReceiver : BroadcastReceiver() {

    /**
     * Extracts reminder details from the intent and triggers a system notification.
     *
     * @param context The application context.
     * @param intent The intent containing "REMINDER_ID", "REMINDER_TITLE", and "REMINDER_DESCRIPTION".
     */
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("REMINDER_ID", -1)
        val title = intent.getStringExtra("REMINDER_TITLE") ?: "Reminder"
        val description = intent.getStringExtra("REMINDER_DESCRIPTION") ?: "You have a reminder."

        if (reminderId != -1L) {
            showNotification(context, reminderId.toInt(), title, description)
        }
    }

    /**
     * Creates and displays a system notification for a reminder.
     * Handles notification channel initialization for API 26+.
     */
    private fun showNotification(context: Context, id: Int, title: String, description: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "REMINDERS",
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "REMINDERS")
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your app's icon
            .build()

        notificationManager.notify(id, notification)
    }
}
