package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an email message that is waiting to be sent by the application.
 *
 * This entity serves as a persistence-backed reliability queue for the email subsystem.
 * It is used to track messages that were either deferred or failed to send during
 * their initial attempt (e.g., due to network instability).
 *
 * ### Lifecycle:
 * 1. **Queueing**: If an email transmission fails, the metadata is persisted here.
 * 2. **Processing**: The [com.example.myapplication.worker.EmailWorker] periodically
 *    scans this table and attempts to re-send all entries.
 * 3. **Cleanup**: Successfully sent entries are permanently removed from the database.
 *
 * ### Security & PII Protection:
 * Sensitive fields—including [recipientAddress], [subject], and [body]—are
 * **transparently encrypted** at rest by the [EmailRepository] using [com.example.myapplication.util.SecurityUtil].
 * This prevents the leakage of student PII if the application's database file is accessed.
 *
 * @property id Unique primary key for the pending message.
 * @property recipientAddress The destination email address. (Encrypted at rest)
 * @property subject The subject line of the email. (Encrypted at rest)
 * @property body The message content. (Encrypted at rest)
 * @property timestamp The time the email was originally queued, in epoch milliseconds.
 */
@Entity(tableName = "pending_emails")
data class PendingEmail(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipientAddress: String,
    val subject: String,
    val body: String,
    val timestamp: Long
)
