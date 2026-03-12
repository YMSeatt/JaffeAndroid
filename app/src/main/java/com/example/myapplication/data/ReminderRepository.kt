package com.example.myapplication.data

import com.example.myapplication.util.SecurityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ReminderRepository: Handles transparent encryption/decryption of teacher reminders.
 *
 * This repository secures sensitive PII and classroom tasks stored in the [Reminder] entity
 * by encrypting the 'title' and 'description' fields at rest using [SecurityUtil].
 *
 * It utilizes [SecurityUtil.decryptSafe] to ensure that any existing unencrypted reminders
 * in the database are migrated seamlessly without data loss.
 */
@Singleton
class ReminderRepository @Inject constructor(
    private val reminderDao: ReminderDao,
    private val securityUtil: SecurityUtil
) {
    /**
     * Retrieves all reminders from the database, decrypting sensitive fields on the fly.
     * Decryption is performed on [Dispatchers.Default] to avoid blocking the main thread.
     */
    fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders().map { list ->
            list.map { decryptReminder(it) }
        }.flowOn(Dispatchers.Default)
    }

    /**
     * Encrypts and inserts a new reminder into the database.
     */
    suspend fun insert(reminder: Reminder): Long {
        return reminderDao.insert(encryptReminder(reminder))
    }

    /**
     * Encrypts and updates an existing reminder in the database.
     */
    suspend fun update(reminder: Reminder) {
        reminderDao.update(encryptReminder(reminder))
    }

    /**
     * Deletes a reminder by its unique identifier.
     */
    suspend fun delete(id: Long) {
        reminderDao.delete(id)
    }

    private fun encryptReminder(reminder: Reminder): Reminder {
        return reminder.copy(
            title = securityUtil.encrypt(reminder.title),
            description = securityUtil.encrypt(reminder.description)
        )
    }

    private fun decryptReminder(reminder: Reminder): Reminder {
        return reminder.copy(
            title = securityUtil.decryptSafe(reminder.title),
            description = securityUtil.decryptSafe(reminder.description)
        )
    }
}
