package com.example.myapplication.data

import com.example.myapplication.util.SecurityUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmailRepository @Inject constructor(
    private val emailScheduleDao: EmailScheduleDao,
    private val pendingEmailDao: PendingEmailDao,
    private val securityUtil: SecurityUtil
) {
    fun getAllSchedules(): Flow<List<EmailSchedule>> {
        return emailScheduleDao.getAllSchedules().map { list ->
            list.map { decryptSchedule(it) }
        }
    }

    suspend fun getAllSchedulesList(): List<EmailSchedule> {
        return emailScheduleDao.getAllSchedulesList().map { decryptSchedule(it) }
    }

    suspend fun insertSchedule(schedule: EmailSchedule) {
        emailScheduleDao.insert(encryptSchedule(schedule))
    }

    suspend fun updateSchedule(schedule: EmailSchedule) {
        emailScheduleDao.update(encryptSchedule(schedule))
    }

    suspend fun deleteSchedule(schedule: EmailSchedule) {
        emailScheduleDao.delete(schedule)
    }

    suspend fun insertPendingEmail(email: PendingEmail) {
        pendingEmailDao.insert(encryptPendingEmail(email))
    }

    suspend fun getAllPendingEmails(): List<PendingEmail> {
        return pendingEmailDao.getAll().map { decryptPendingEmail(it) }
    }

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
