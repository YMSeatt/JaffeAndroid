package com.example.myapplication.data

import com.example.myapplication.util.SecurityUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * EmailRepository: Coordinates the persistence and security of email-related data.
 *
 * This repository serves as the single source of truth for managing [EmailSchedule]s
 * and [PendingEmail]s. It implements a "Secure-at-Rest" model by ensuring that
 * all sensitive Personally Identifiable Information (PII)—including recipient
 * addresses, email subjects, and message bodies—is transparently encrypted before
 * being stored in the database.
 *
 * ### Responsibilities:
 * - **Transparent Encryption**: Automatically encrypts sensitive fields in [EmailSchedule]
 *   and [PendingEmail] using [SecurityUtil] during insertion or update operations.
 * - **Transparent Decryption**: Automatically decrypts fields when retrieving data
 *   from the database.
 * - **Queue Management**: Handles the storage and retrieval of [PendingEmail]s for
 *   background retry logic.
 */
@Singleton
class EmailRepository @Inject constructor(
    private val emailScheduleDao: EmailScheduleDao,
    private val pendingEmailDao: PendingEmailDao,
    private val securityUtil: SecurityUtil
) {
    /**
     * Returns a reactive [Flow] of all automated email schedules.
     * Sensitive fields are decrypted on-the-fly for presentation in the UI.
     */
    fun getAllSchedules(): Flow<List<EmailSchedule>> {
        return emailScheduleDao.getAllSchedules().map { list ->
            list.map { decryptSchedule(it) }
        }
    }

    /**
     * Retrieves a static list of all email schedules.
     * Used primarily by background workers (e.g., [com.example.myapplication.worker.EmailSchedulerWorker])
     * to evaluate schedule triggers.
     */
    suspend fun getAllSchedulesList(): List<EmailSchedule> {
        return emailScheduleDao.getAllSchedulesList().map { decryptSchedule(it) }
    }

    /**
     * Encrypts and inserts a new [EmailSchedule] into the database.
     *
     * @param schedule The schedule to persist.
     */
    suspend fun insertSchedule(schedule: EmailSchedule) {
        emailScheduleDao.insert(encryptSchedule(schedule))
    }

    /**
     * Encrypts and updates an existing [EmailSchedule].
     *
     * @param schedule The schedule containing updated metadata.
     */
    suspend fun updateSchedule(schedule: EmailSchedule) {
        emailScheduleDao.update(encryptSchedule(schedule))
    }

    /**
     * Permanently removes an [EmailSchedule] from the database.
     */
    suspend fun deleteSchedule(schedule: EmailSchedule) {
        emailScheduleDao.delete(schedule)
    }

    /**
     * Encrypts and persists an email that is waiting to be sent.
     * This is typically used when an initial send attempt fails or is deferred.
     *
     * @param email The email metadata to store in the pending queue.
     */
    suspend fun insertPendingEmail(email: PendingEmail) {
        pendingEmailDao.insert(encryptPendingEmail(email))
    }

    /**
     * Retrieves all [PendingEmail]s currently in the queue, decrypting them for processing.
     * @return A list of emails ready for a background send attempt.
     */
    suspend fun getAllPendingEmails(): List<PendingEmail> {
        return pendingEmailDao.getAll().map { decryptPendingEmail(it) }
    }

    /**
     * Removes a pending email from the queue once it has been successfully processed.
     * @param id The unique identifier of the [PendingEmail] to remove.
     */
    suspend fun deletePendingEmail(id: Long) {
        pendingEmailDao.delete(id)
    }

    private fun encryptSchedule(schedule: EmailSchedule): EmailSchedule {
        return schedule.copy(
            recipientEmail = securityUtil.encrypt(schedule.recipientEmail),
            subject = securityUtil.encrypt(schedule.subject),
            body = securityUtil.encrypt(schedule.body),
            exportOptionsJson = schedule.exportOptionsJson?.let { securityUtil.encrypt(it) }
        )
    }

    private fun decryptSchedule(schedule: EmailSchedule): EmailSchedule {
        return schedule.copy(
            recipientEmail = securityUtil.decryptSafe(schedule.recipientEmail),
            subject = securityUtil.decryptSafe(schedule.subject),
            body = securityUtil.decryptSafe(schedule.body),
            exportOptionsJson = schedule.exportOptionsJson?.let { securityUtil.decryptSafe(it) }
        )
    }

    private fun encryptPendingEmail(email: PendingEmail): PendingEmail {
        return email.copy(
            recipientAddress = securityUtil.encrypt(email.recipientAddress),
            subject = securityUtil.encrypt(email.subject),
            body = securityUtil.encrypt(email.body)
        )
    }

    private fun decryptPendingEmail(email: PendingEmail): PendingEmail {
        return email.copy(
            recipientAddress = securityUtil.decryptSafe(email.recipientAddress),
            subject = securityUtil.decryptSafe(email.subject),
            body = securityUtil.decryptSafe(email.body)
        )
    }
}
