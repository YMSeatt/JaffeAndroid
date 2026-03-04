package com.example.myapplication.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.myapplication.R

/**
 * ReminderReceiver: The primary entry point for scheduled reminder broadcasts.
 *
 * This [BroadcastReceiver] is triggered by the system [android.app.AlarmManager] at
 * the requested time. Its primary responsibility is to transform a background alarm
 * event into a user-visible system notification.
 *
 * ### Architecture & Lifecycle:
 * - **Trigger**: Activated via a [PendingIntent] sent from [ReminderManager].
 * - **Execution Window**: Runs on the main thread for a brief period. Heavy processing
 *   is avoided to ensure the system doesn't kill the receiver.
 * - **Intent Protocol**: Expects metadata in **strictly lowercase** keys (`reminder_id`, etc.)
 *   to maintain parity with the Android ecosystem standards used in this app.
 *
 * ### Security & Privacy:
 * - **Lock Screen Privacy**: Explicitly sets visibility to [NotificationCompat.VISIBILITY_PRIVATE]
 *   to prevent Personally Identifiable Information (PII), such as student names or
 *   sensitive classroom tasks, from being visible on a locked device.
 */
class ReminderReceiver : BroadcastReceiver() {
    /**
     * Processes the incoming alarm intent and triggers a notification.
     *
     * This method handles the creation of the notification channel (on API 26+) and
     * the construction of the individual notification using [NotificationCompat.Builder].
     *
     * @param context The application context.
     * @param intent The intent containing lowercase reminder metadata (id, title, description).
     */
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", 0)
        val title = intent.getStringExtra("reminder_title")
        val description = intent.getStringExtra("reminder_description")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Notification channels are required for Android O and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "reminder_channel",
                "Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                // HARDEN: Ensure PII is not leaked on the lockscreen for the entire channel
                lockscreenVisibility = android.app.Notification.VISIBILITY_PRIVATE
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE) // HARDEN: Protect PII on lockscreen for individual notifications
            .build()

        notificationManager.notify(reminderId.toInt(), notification)
    }
}