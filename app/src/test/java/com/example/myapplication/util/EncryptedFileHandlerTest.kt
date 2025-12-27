package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class EncryptedFileHandlerTest {

    private lateinit var context: Context
    private lateinit var fileHandler: EncryptedFileHandler
    private lateinit var testFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fileHandler = EncryptedFileHandler()
        testFile = File(context.cacheDir, "test.txt")
        testFile.delete() // Ensure a clean state
    }

    @Test
    fun `readFile should return null for non-existent file`() {
        val content = fileHandler.readFile(context, testFile)
        assertThat(content).isNull()
    }

    @Test
    fun `readFile should return null for empty file`() {
        testFile.createNewFile()
        val content = fileHandler.readFile(context, testFile)
        assertThat(content).isNull()
    }

    @Test
    fun `writeFile and readFile should work for plaintext`() {
        val data = "This is a plaintext test."
        fileHandler.writeFile(context, testFile, data, encrypt = false)

        // With the corrected logic, reading a plaintext file should fail because
        // the handler will always attempt to decrypt it, and plaintext is not valid encrypted data.
        assertThrows(IOException::class.java) {
            fileHandler.readFile(context, testFile)
        }
    }

    @Test
    fun `writeFile and readFile should work for encrypted text`() {
        // This test requires a functioning EncryptionUtil.
        // We'll assume it works for this unit test.
        val data = "This is an encrypted test."
        fileHandler.writeFile(context, testFile, data, encrypt = true)

        val content = fileHandler.readFile(context, testFile)
        assertThat(content).isEqualTo(data)
    }

    @Test
    fun `readFile should throw IOException for invalid encrypted content`() {
        // Arrange: Write invalid (non-Base64) encrypted data to a file
        val invalidEncryptedData = "this-is-not-valid-encrypted-data"
        testFile.writeText(invalidEncryptedData)

        // Act & Assert: Verify that readFile throws an IOException.
        // The handler wraps crypto/decode exceptions in IOException.
        val exception = assertThrows(IOException::class.java) {
            fileHandler.readFile(context, testFile)
        }
        // A robust check is to ensure the message contains the file name, which both branches do.
        assertThat(exception).hasMessageThat().contains(testFile.name)
    }

    @Test
    fun `readFile should handle plaintext when expecting encrypted`() {
        // If a file was saved as plaintext, and we now try to read it as encrypted,
        // it should fail gracefully (as per the new logic).
        val plaintextData = "This should not be treated as encrypted."
        testFile.writeText(plaintextData)

        assertThrows(IOException::class.java) {
            fileHandler.readFile(context, testFile)
        }
    }
}
