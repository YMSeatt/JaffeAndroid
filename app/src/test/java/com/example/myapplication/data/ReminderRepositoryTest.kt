package com.example.myapplication.data

import com.example.myapplication.util.SecurityUtil
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderRepositoryTest {

    private val dao = mockk<ReminderDao>()
    private val securityUtil = mockk<SecurityUtil>()
    private val repository = ReminderRepository(dao, securityUtil)

    @Test
    fun `getAllReminders should decrypt fields`() = runBlocking {
        val encryptedReminder = Reminder(id = 1, title = "encrypted_title", description = "encrypted_desc", timestamp = 12345L)
        val decryptedReminder = Reminder(id = 1, title = "decrypted_title", description = "decrypted_desc", timestamp = 12345L)

        every { dao.getAllReminders() } returns flowOf(listOf(encryptedReminder))
        every { securityUtil.decryptSafe("encrypted_title") } returns "decrypted_title"
        every { securityUtil.decryptSafe("encrypted_desc") } returns "decrypted_desc"

        val result = repository.getAllReminders().first()

        assertEquals(listOf(decryptedReminder), result)
    }

    @Test
    fun `insert should encrypt fields`() = runBlocking {
        val plainReminder = Reminder(title = "plain_title", description = "plain_desc", timestamp = 12345L)
        val encryptedReminder = Reminder(title = "encrypted_title", description = "encrypted_desc", timestamp = 12345L)

        every { securityUtil.encrypt("plain_title") } returns "encrypted_title"
        every { securityUtil.encrypt("plain_desc") } returns "encrypted_desc"
        coEvery { dao.insert(encryptedReminder) } returns 1L

        val id = repository.insert(plainReminder)

        assertEquals(1L, id)
        coVerify { dao.insert(encryptedReminder) }
    }

    @Test
    fun `update should encrypt fields`() = runBlocking {
        val plainReminder = Reminder(id = 1, title = "plain_title", description = "plain_desc", timestamp = 12345L)
        val encryptedReminder = Reminder(id = 1, title = "encrypted_title", description = "encrypted_desc", timestamp = 12345L)

        every { securityUtil.encrypt("plain_title") } returns "encrypted_title"
        every { securityUtil.encrypt("plain_desc") } returns "encrypted_desc"
        coEvery { dao.update(encryptedReminder) } returns Unit

        repository.update(plainReminder)

        coVerify { dao.update(encryptedReminder) }
    }

    @Test
    fun `delete should call dao delete`() = runBlocking {
        coEvery { dao.delete(1L) } returns Unit

        repository.delete(1L)

        coVerify { dao.delete(1L) }
    }

    @Test
    fun `getReminderById should decrypt fields`() = runBlocking {
        val encryptedReminder = Reminder(id = 1, title = "encrypted_title", description = "encrypted_desc", timestamp = 12345L)
        val decryptedReminder = Reminder(id = 1, title = "decrypted_title", description = "decrypted_desc", timestamp = 12345L)

        coEvery { dao.getReminderById(1L) } returns encryptedReminder
        every { securityUtil.decryptSafe("encrypted_title") } returns "decrypted_title"
        every { securityUtil.decryptSafe("encrypted_desc") } returns "decrypted_desc"

        val result = repository.getReminderById(1L)

        assertEquals(decryptedReminder, result)
    }

    @Test
    fun `getReminderById should return null if not found`() = runBlocking {
        coEvery { dao.getReminderById(1L) } returns null

        val result = repository.getReminderById(1L)

        assertEquals(null, result)
    }
}
