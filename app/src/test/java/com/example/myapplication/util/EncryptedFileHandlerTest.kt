package com.example.myapplication.util

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File

class EncryptedFileHandlerTest {

    private lateinit var mockEncryptionUtil: EncryptionUtil
    private lateinit var encryptedFileHandler: EncryptedFileHandler
    private lateinit var mockContext: Context
    private lateinit var testFile: File

    @Before
    fun setUp() {
        mockEncryptionUtil = mockk()
        encryptedFileHandler = EncryptedFileHandler(mockEncryptionUtil)
        mockContext = mockk(relaxed = true)
        testFile = File.createTempFile("test", ".txt")
    }

    @Test
    fun `readFile decrypts when file is encrypted`() {
        val encryptedText = "encrypted_text"
        val decryptedText = "decrypted_text"
        testFile.writeBytes(encryptedText.toByteArray())
        every { mockEncryptionUtil.decrypt(encryptedText) } returns decryptedText.toByteArray()

        val result = encryptedFileHandler.readFile(mockContext, testFile)

        assertEquals(decryptedText, result)
        verify { mockEncryptionUtil.decrypt(encryptedText) }
    }

    @Test
    fun `readFile returns plaintext when decryption fails`() {
        val plaintext = "plaintext"
        testFile.writeBytes(plaintext.toByteArray())
        every { mockEncryptionUtil.decrypt(any()) } throws SecurityException("Decryption failed")

        val result = encryptedFileHandler.readFile(mockContext, testFile)

        assertEquals(plaintext, result)
    }

    @Test
    fun `writeFile encrypts data when encrypt is true`() {
        val data = "test_data"
        val encryptedData = "encrypted_data"
        every { mockEncryptionUtil.encrypt(data.toByteArray()) } returns encryptedData

        encryptedFileHandler.writeFile(mockContext, testFile, data, true)

        assertEquals(encryptedData, testFile.readText())
        verify { mockEncryptionUtil.encrypt(data.toByteArray()) }
    }

    @Test
    fun `writeFile writes plaintext when encrypt is false`() {
        val data = "test_data"

        encryptedFileHandler.writeFile(mockContext, testFile, data, false)

        assertEquals(data, testFile.readText())
        verify(exactly = 0) { mockEncryptionUtil.encrypt(any()) }
    }
}
