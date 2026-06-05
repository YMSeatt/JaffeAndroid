package com.example.myapplication.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class FileLockerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var testFile: File

    @Before
    fun setup() {
        testFile = tempFolder.newFile("test_data.txt")
        testFile.writeText("Initial student data.")
    }

    @Test
    fun `test locking file makes it read-only`() {
        assertTrue("File should be writable initially", testFile.canWrite())

        val lockSuccess = FileLocker.lock(testFile)
        assertTrue("Lock operation should be successful", lockSuccess)
        assertFalse("File should not be writable after locking", testFile.canWrite())
    }

    @Test
    fun `test unlocking file makes it writable`() {
        // First lock it
        FileLocker.lock(testFile)
        assertFalse("File should be read-only", testFile.canWrite())

        val unlockSuccess = FileLocker.unlock(testFile)
        assertTrue("Unlock operation should be successful", unlockSuccess)
        assertTrue("File should be writable after unlocking", testFile.canWrite())
    }

    @Test
    fun `test locking non-existent file fails`() {
        val nonExistentFile = File(tempFolder.root, "does_not_exist.txt")
        val lockSuccess = FileLocker.lock(nonExistentFile)
        assertFalse("Locking non-existent file should fail", lockSuccess)
    }

    @Test
    fun `test unlocking non-existent file fails`() {
        val nonExistentFile = File(tempFolder.root, "does_not_exist.txt")
        val unlockSuccess = FileLocker.unlock(nonExistentFile)
        assertFalse("Unlocking non-existent file should fail", unlockSuccess)
    }
}
