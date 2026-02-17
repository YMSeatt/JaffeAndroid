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
 * BroadcastReceiver triggered by [android.app.AlarmManager] when a reminder is due.
 *
 * This receiver is responsible for creating and displaying a system notification
 * to alert the teacher. It handles notification channel creation for Android O (API 26) and above.
 */
class ReminderReceiver : BroadcastReceiver() {
    /**
     * Called when the broadcast is received. Extracts reminder details from the [Intent]
     * and displays a notification.
     *
     * @param context The application context.
     * @param intent The intent containing "reminder_id", "reminder_title", and "reminder_description".
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
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "reminder_channel")
            .setContentTitle(title)
            .setContentText(description)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        notificationManager.notify(reminderId.toInt(), notification)
    }
}