package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class SecurityUtilTest {

    private lateinit var context: Context
    private lateinit var securityUtil: SecurityUtil

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Ensure the key file doesn't exist before each test
        val keyFile = File(context.filesDir, "fernet.key")
        if (keyFile.exists()) {
            keyFile.delete()
        }
        securityUtil = SecurityUtil(context)
    }

    @Test
    fun `test key generation and storage`() {
        val keyFile = File(context.filesDir, "fernet.key")
        assertTrue("Key file should be created", keyFile.exists())
        assertEquals("Key file should be 32 bytes long", 32, keyFile.readBytes().size)
    }

    @Test
    fun `test encrypt and decrypt`() {
        val originalText = "This is a secret message."
        val encryptedText = securityUtil.encrypt(originalText)
        val decryptedText = securityUtil.decrypt(encryptedText)
        assertEquals("Decrypted text should match original text", originalText, decryptedText)
    }

    @Test
    fun `test decryption failure`() {
        val invalidToken = "gAAAAABl2c5-j2E8_2_9..." // Just an example of an invalid token
        assertThrows(java.lang.SecurityException::class.java) {
            securityUtil.decrypt(invalidToken)
        }
    }

    @Test
    fun `test password hashing`() {
        val password = "password123"
        val hashedPassword = SecurityUtil.hashPassword(password)
        val expectedHash = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"
        assertEquals(expectedHash, hashedPassword)
    }
}
