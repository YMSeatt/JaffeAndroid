package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class IsolatedEncryptedFileHandlerTest {

    private lateinit var encryptedFileHandler: EncryptedFileHandler
    private lateinit var context: Context
    private lateinit var testFile: File
    private val testContent = "This is a test string."

    @Before
    fun setUp() {
        encryptedFileHandler = EncryptedFileHandler()
        context = ApplicationProvider.getApplicationContext()
        testFile = File(context.filesDir, "test_file.txt")
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    @Test
    fun `writeFile with encryption then readFile decrypts successfully`() {
        // Act: Write the file with encryption enabled
        encryptedFileHandler.writeFile(context, testFile, testContent, encrypt = true)

        // Assert: Ensure the file is not plaintext
        val rawContent = testFile.readText()
        assertThat(rawContent).isNotEqualTo(testContent)

        // Act: Read the file
        val decryptedContent = encryptedFileHandler.readFile(context, testFile)

        // Assert: Check if the content was decrypted correctly
        assertThat(decryptedContent).isEqualTo(testContent)
    }

    @Test
    fun `writeFile without encryption then readFile throws IOException`() {
        // Act: Write the file with encryption disabled
        encryptedFileHandler.writeFile(context, testFile, testContent, encrypt = false)

        // Assert: Ensure the file is plaintext
        val rawContent = testFile.readText()
        assertThat(rawContent).isEqualTo(testContent)

        // Act & Assert: Reading the plaintext file should now fail
        try {
            encryptedFileHandler.readFile(context, testFile)
            assert(false) { "Expected an IOException to be thrown for non-encrypted file." }
        } catch (e: IOException) {
            // Success
            assertThat(e).hasMessageThat().contains("Failed to decrypt file")
        }
    }

    @Test
    fun `readFile on corrupted file throws IOException`() {
        // Arrange: Create a corrupted (non-Base64) file manually
        testFile.writeText("this is not a valid encrypted string")

        // Act & Assert
        try {
            encryptedFileHandler.readFile(context, testFile)
            assert(false) { "Expected an IOException to be thrown." }
        } catch (e: IOException) {
            // Success
            assertThat(e).hasMessageThat().contains("Failed to decrypt file")
        }
    }

    @Test
    fun `readFile on non-existent file returns null`() {
        // Act
        val readContent = encryptedFileHandler.readFile(context, testFile)

        // Assert
        assertThat(readContent).isNull()
    }

    @Test
    fun `readFile on empty file returns null`() {
        // Arrange
        testFile.createNewFile()

        // Act
        val readContent = encryptedFileHandler.readFile(context, testFile)

        // Assert
        assertThat(readContent).isNull()
    }
}
