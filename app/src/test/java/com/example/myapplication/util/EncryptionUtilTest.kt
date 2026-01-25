package com.example.myapplication.util

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.SecureRandom
import java.util.*

class EncryptionUtilTest {

    private lateinit var mockContext: Context
    private lateinit var tempFilesDir: File

    @Before
    fun setUp() {
        mockContext = mockk(relaxed = true)
        // Create a temporary directory that acts as the app's internal storage for tests
        tempFilesDir = createTempDir("test_files")
        every { mockContext.filesDir } returns tempFilesDir
        every { mockContext.applicationContext } returns mockContext
    }

    @After
    fun tearDown() {
        // Clean up the temporary directory after tests
        tempFilesDir.deleteRecursively()

        // Reset the singleton's state via reflection to ensure test isolation
        try {
            val legacyCipherField = EncryptionUtil::class.java.getDeclaredField("legacyCipher")
            legacyCipherField.isAccessible = true
            legacyCipherField.set(EncryptionUtil, null)
        } catch (e: Exception) {
            // Handle reflection exceptions
            e.printStackTrace()
        }
    }

    private fun generateRandomKey(): ByteArray {
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)
        return key
    }

    @Test
    fun `encrypt always uses the primary key`() {
        val plaintext = "new data for encryption".toByteArray()
        val token = EncryptionUtil.encrypt(plaintext)

        // Decryption with the primary key should succeed.
        val decrypted = EncryptionUtil.decrypt(mockContext, token)
        assertArrayEquals(plaintext, decrypted)

        // Decryption with a random "legacy" key should fail, proving the primary key was used.
        val legacyKey = generateRandomKey()
        val legacyCipher = FernetCipher(legacyKey)
        assertThrows(SecurityException::class.java) {
            legacyCipher.decrypt(token, 0)
        }
    }

    @Test
    fun `decrypt successfully uses primary key for new data`() {
        val plaintext = "data encrypted with primary key".toByteArray()
        val primaryKey = Base64.getUrlDecoder().decode("7-BH7qsnKyRK0jdAZrjXSIW9VmcdpfHHeZor0ACBkmU=")
        val primaryCipher = FernetCipher(primaryKey)
        val token = primaryCipher.encrypt(plaintext)

        val decrypted = EncryptionUtil.decrypt(mockContext, token)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `decrypt successfully falls back to legacy key for old data`() {
        // 1. Set up a legacy key file in the mocked directory.
        val legacyKey = generateRandomKey()
        val keyFile = File(tempFilesDir, "fernet.key")
        keyFile.writeBytes(legacyKey)

        // 2. Encrypt data using the legacy key.
        val plaintext = "old data encrypted with legacy key".toByteArray()
        val legacyCipher = FernetCipher(legacyKey)
        val token = legacyCipher.encrypt(plaintext)

        // 3. Decrypt using the main utility. It should fail with the primary key and fall back.
        val decrypted = EncryptionUtil.decrypt(mockContext, token)
        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun `decrypt throws SecurityException if token is invalid for both keys`() {
        // 1. Ensure a legacy key file exists.
        val legacyKey = generateRandomKey()
        val keyFile = File(tempFilesDir, "fernet.key")
        keyFile.writeBytes(legacyKey)

        // 2. Encrypt with a completely different, unknown key.
        val unknownKey = generateRandomKey()
        val unknownCipher = FernetCipher(unknownKey)
        val plaintext = "data from an unknown source".toByteArray()
        val token = unknownCipher.encrypt(plaintext)

        // 3. Decryption should fail with both the primary and legacy keys.
        val exception = assertThrows(SecurityException::class.java) {
            EncryptionUtil.decrypt(mockContext, token)
        }
        assertTrue(exception.message?.contains("Failed to decrypt with both primary and legacy keys") ?: false)
    }

    @Test
    fun `decrypt throws SecurityException if only primary key fails and no legacy key exists`() {
        // 1. Ensure no legacy key file exists.
        val keyFile = File(tempFilesDir, "fernet.key")
        assertFalse(keyFile.exists())

        // 2. Encrypt with an unknown key.
        val unknownKey = generateRandomKey()
        val unknownCipher = FernetCipher(unknownKey)
        val plaintext = "some random data".toByteArray()
        val token = unknownCipher.encrypt(plaintext)

        // 3. Decryption should fail, and not throw the compound "both keys" exception.
        val exception = assertThrows(SecurityException::class.java) {
            EncryptionUtil.decrypt(mockContext, token)
        }
        // The message should be the original one from the primary cipher failing.
        assertEquals("Invalid token signature", exception.message)
    }
}
