package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class EncryptedFileHandlerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var encryptedFileHandler: EncryptedFileHandler
    private lateinit var testFile: File

    @Before
    fun setUp() {
        encryptedFileHandler = EncryptedFileHandler()
        testFile = tempFolder.newFile("test_data.txt")
    }

    @Test
    fun `writeFile with encryption writes encrypted content`() {
        val originalContent = "This is a secret message."
        encryptedFileHandler.writeFile(testFile, originalContent, encrypt = true)

        val fileContent = testFile.readText()
        assertNotEquals(originalContent, fileContent)
    }

    @Test
    fun `readFile decrypts content written with encryption`() {
        val originalContent = "This is a secret message."
        encryptedFileHandler.writeFile(testFile, originalContent, encrypt = true)

        val decryptedContent = encryptedFileHandler.readFile(testFile)
        assertEquals(originalContent, decryptedContent)
    }

    @Test
    fun `writeFile without encryption writes plaintext content`() {
        val originalContent = "This is a public message."
        encryptedFileHandler.writeFile(testFile, originalContent, encrypt = false)

        val fileContent = testFile.readText()
        assertEquals(originalContent, fileContent)
    }

    @Test
    fun `readFile reads plaintext content written without encryption`() {
        val originalContent = "This is a public message."
        encryptedFileHandler.writeFile(testFile, originalContent, encrypt = false)

        val readContent = encryptedFileHandler.readFile(testFile)
        assertEquals(originalContent, readContent)
    }

    @Test
    fun `readFile falls back to plaintext for non-encrypted content`() {
        val nonEncryptedContent = "This is not an encrypted token."
        testFile.writeText(nonEncryptedContent)

        val readContent = encryptedFileHandler.readFile(testFile)
        assertEquals(nonEncryptedContent, readContent)
    }

    @Test
    fun `readFile returns null for non-existent file`() {
        val nonExistentFile = File(tempFolder.root, "non_existent.txt")
        val content = encryptedFileHandler.readFile(nonExistentFile)
        assertNull(content)
    }

    @Test
    fun `readFile returns null for empty file`() {
        val emptyFile = tempFolder.newFile("empty.txt")
        val content = encryptedFileHandler.readFile(emptyFile)
        // An empty file has a length of 0, so readFile should return null.
        assertNull(content)
    }
}
