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
import java.security.SecureRandom
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
        val decryptedContent = encryptedFileHandler.readFile(context, testFile)

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
        val readContent = encryptedFileHandler.readFile(context, testFile)

        // Assert: Check if the content was read correctly
        assertThat(readContent).isEqualTo(testContent)
    }

    @Test
    fun `readFile on plaintext file falls back and reads successfully`() {
        // Arrange: Create a plaintext file manually
        testFile.writeText(testContent)

        // Act: Read the file using the handler
        val readContent = encryptedFileHandler.readFile(context, testFile)

        // Assert: Verify the plaintext fallback worked
        assertThat(readContent).isEqualTo(testContent)
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

    @Test
    fun `readFile with legacy key decrypts successfully`() {
        // Arrange: Create a legacy key and a FernetCipher instance with it
        val legacyKey = ByteArray(32)
        SecureRandom().nextBytes(legacyKey)
        val legacyKeyFile = File(context.filesDir, "fernet.key")
        legacyKeyFile.writeBytes(legacyKey)

        val legacyCipher = FernetCipher(legacyKey)
        val encryptedContent = legacyCipher.encrypt(testContent.toByteArray())
        testFile.writeText(encryptedContent)

        // Reset the singleton instance in EncryptionUtil to ensure it picks up the legacy key file
        val field = EncryptionUtil::class.java.getDeclaredField("legacyCipher")
        field.isAccessible = true
        field.set(EncryptionUtil, null)


        // Act: Read the file using the handler
        val decryptedContent = encryptedFileHandler.readFile(context, testFile)

        // Assert: Verify that the content was decrypted using the legacy key
        assertThat(decryptedContent).isEqualTo(testContent)

        // Clean up the legacy key file
        legacyKeyFile.delete()
    }
}
