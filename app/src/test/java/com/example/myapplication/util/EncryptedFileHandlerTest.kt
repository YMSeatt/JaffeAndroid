package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class EncryptedFileHandlerTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var encryptedFileHandler: EncryptedFileHandler

    private lateinit var context: Context
    private lateinit var testFile: File
    private val testContent = "This is a test string."

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
        testFile = File(context.filesDir, "test_file.txt")
        if (testFile.exists()) {
            testFile.delete()
        }
    }

    @Test
    fun `writeFile with encryption then readFile decrypts successfully`() {
        // Act: Write the file with encryption enabled
        encryptedFileHandler.writeFile(testFile, testContent, encrypt = true)

        // Assert: Ensure the file is not plaintext
        val rawContent = testFile.readText()
        assertThat(rawContent).isNotEqualTo(testContent)

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
        // Act
        val readContent = encryptedFileHandler.readFile(testFile)

        // Assert
        assertThat(readContent).isNull()
    }

    @Test
    fun `readFile on empty file returns null`() {
        // Arrange
        testFile.createNewFile()

        // Act
        val readContent = encryptedFileHandler.readFile(testFile)

        // Assert
        assertThat(readContent).isNull()
    }
}
