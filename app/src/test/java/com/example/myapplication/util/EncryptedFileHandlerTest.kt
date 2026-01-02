package com.example.myapplication.util

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.charset.StandardCharsets

class EncryptedFileHandlerTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var encryptedFileHandler: EncryptedFileHandler
    private val encryptionUtil: EncryptionUtil = mockk()

    private lateinit var testFile: File
    private val testContent = "This is a test string."
    private val encryptedContent = "encrypted_string"

    @Before
    fun setUp() {
        testFile = temporaryFolder.newFile("test_file.txt")
        encryptedFileHandler = EncryptedFileHandler(encryptionUtil)

        every { encryptionUtil.encrypt(any()) } returns encryptedContent
        every { encryptionUtil.decrypt(encryptedContent) } returns testContent.toByteArray(StandardCharsets.UTF_8)
        every { encryptionUtil.decrypt(testContent) } throws SecurityException("Invalid token")
    }

    @Test
    fun `writeFile with encryption then readFile decrypts successfully`() {
        // Act: Write the file with encryption enabled
        encryptedFileHandler.writeFile(testFile, testContent, encrypt = true)

        // Assert: Ensure the file is not plaintext
        val rawContent = testFile.readText()
        assertThat(rawContent).isEqualTo(encryptedContent)

        // Act: Read the file
        val decryptedContent = encryptedFileHandler.readFile(testFile)

        // Assert: Check if the content was decrypted correctly
        assertThat(decryptedContent).isEqualTo(testContent)
    }

    @Test
    fun `writeFile without encryption then readFile reads plaintext`() {
        // Act: Write the file with encryption disabled
        encryptedFileHandler.writeFile(testFile, testContent, encrypt = false)

        // Assert: Ensure the file is plaintext
        val rawContent = testFile.readText()
        assertThat(rawContent).isEqualTo(testContent)

        // Act: Read the file
        val readContent = encryptedFileHandler.readFile(testFile)

        // Assert: Check if the content was read correctly
        assertThat(readContent).isEqualTo(testContent)
    }

    @Test
    fun `readFile on plaintext file falls back and reads successfully`() {
        // Arrange: Create a plaintext file manually
        testFile.writeText(testContent)

        // Act: Read the file using the handler
        val readContent = encryptedFileHandler.readFile(testFile)

        // Assert: Verify the plaintext fallback worked
        assertThat(readContent).isEqualTo(testContent)
    }

    @Test
    fun `readFile on non-existent file returns null`() {
        // Arrange
        val nonExistentFile = File(temporaryFolder.root, "non_existent.txt")

        // Act
        val readContent = encryptedFileHandler.readFile(nonExistentFile)

        // Assert
        assertThat(readContent).isNull()
    }

    @Test
    fun `readFile on empty file returns null`() {
        // Arrange
        val emptyFile = temporaryFolder.newFile("empty.txt")
        // Act
        val readContent = encryptedFileHandler.readFile(emptyFile)

        // Assert
        assertThat(readContent).isNull()
    }
}
