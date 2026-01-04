package com.example.myapplication.util

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class EncryptionUtilTest {

    private lateinit var mockContext: Context
    private lateinit var tempDir: File
    private lateinit var encryptionUtil: EncryptionUtil

    @Before
    fun setup() {
        mockContext = mockk()
        tempDir = createTempDir()
        every { mockContext.filesDir } returns tempDir

        // Instantiate EncryptionUtil with the mocked context
        encryptionUtil = EncryptionUtil(mockContext)
    }

    @Test
    fun `encrypt and decrypt returns original plaintext`() {
        val plaintext = "This is a secret message."
        val plaintextBytes = plaintext.toByteArray()

        val encryptedToken = encryptionUtil.encrypt(plaintextBytes)
        val decryptedBytes = encryptionUtil.decrypt(encryptedToken)

        assertArrayEquals(plaintextBytes, decryptedBytes)
    }

    @Test
    fun `generated key is saved and reused`() {
        // First instance generates and saves the key
        val util1 = EncryptionUtil(mockContext)
        val plaintext = "test".toByteArray()
        val encrypted1 = util1.encrypt(plaintext)

        // Second instance should load the same key
        val util2 = EncryptionUtil(mockContext)
        val decrypted2 = util2.decrypt(encrypted1)

        assertArrayEquals(plaintext, decrypted2)
    }

    @Test
    fun `different keys produce different ciphertexts`() {
        val plaintext = "test".toByteArray()

        // Create a separate context and directory for the second util
        val mockContext2 = mockk<Context>()
        val tempDir2 = createTempDir()
        every { mockContext2.filesDir } returns tempDir2
        val util2 = EncryptionUtil(mockContext2)

        val encrypted1 = encryptionUtil.encrypt(plaintext)
        val encrypted2 = util2.encrypt(plaintext)

        assertNotEquals(encrypted1, encrypted2)
    }

    @Test(expected = SecurityException::class)
    fun `decrypting with wrong key throws SecurityException`() {
        val plaintext = "test".toByteArray()
        val encryptedToken = encryptionUtil.encrypt(plaintext)

        // Create a new util with a different key
        val mockContext2 = mockk<Context>()
        val tempDir2 = createTempDir()
        every { mockContext2.filesDir } returns tempDir2
        val wrongKeyUtil = EncryptionUtil(mockContext2)

        wrongKeyUtil.decrypt(encryptedToken)
    }
}
