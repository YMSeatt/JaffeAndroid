package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a user-defined schedule for automated classroom report delivery.
 *
 * This entity is the primary configuration object for the application's automated reporting
 * subsystem. It defines when a report should be generated and where it should be sent.
 *
 * ### Lifecycle & Evaluation:
 * Schedules are evaluated periodically (typically every 15 minutes) by the
 * [com.example.myapplication.worker.EmailSchedulerWorker]. If the current system time and day
 * match an active schedule, an export/email task is dispatched to the background execution engine.
 *
 * ### Security & PII Protection:
 * Sensitive fields—including [recipientEmail], [subject], [body], and [exportOptionsJson]—are
 * **transparently encrypted** at rest by the [EmailRepository]. The raw database values
 * are CipherText, ensuring that student PII remains protected even if the physical storage
 * is compromised.
 *
 * @property id Unique primary key for the schedule.
 * @property hour The hour of the day (0-23) when the report should be triggered.
 * @property minute The minute of the hour (0-59) when the report should be triggered.
 * @property daysOfWeek A legacy bitmask representation of the active days of the week.
 * @property recipientEmail The destination email address. (Encrypted at rest)
 * @property subject The subject line for the automated email. (Encrypted at rest)
 * @property body The message body for the automated email. (Encrypted at rest)
 * @property enabled Whether the schedule is currently active and eligible for evaluation.
 * @property days A set of abbreviated day strings (e.g., "Mon", "Tue") indicating when
 *                 the schedule is active. This is the primary field used by the
 *                 [com.example.myapplication.worker.EmailSchedulerWorker].
 * @property exportOptionsJson A JSON-serialized [com.example.myapplication.data.exporter.ExportOptions]
 *                             string defining what data to include in the report. (Encrypted at rest)
 */
@Serializable
@Entity(tableName = "email_schedules")
data class EmailSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val daysOfWeek: Int, // Bitmask for days
    val recipientEmail: String,
    val subject: String,
    val body: String,
    val enabled: Boolean = true,
    val days: Set<String>,
    val exportOptionsJson: String? = null
)
