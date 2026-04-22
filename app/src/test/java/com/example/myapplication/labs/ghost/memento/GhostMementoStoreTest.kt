package com.example.myapplication.labs.ghost.memento

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.util.SecurityUtil
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GhostMementoStoreTest {

    private lateinit var context: Context
    private lateinit var securityUtil: SecurityUtil
    private lateinit var store: GhostMementoStore

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        securityUtil = mockk()
        store = GhostMementoStore(context, securityUtil)
    }

    @Test
    fun `test saveHistory encrypts data`() = runTest {
        val history = MementoHistory(undoStack = emptyList(), redoStack = emptyList())
        val json = "{\"undoStack\":[],\"redoStack\":[]}"
        val encryptedJson = "encrypted_json"

        every { securityUtil.encrypt(any<String>()) } returns encryptedJson

        store.saveHistory(history)

        // Since we cannot easily inspect the internal DataStore,
        // we verify that the encryption was called.
        verify { securityUtil.encrypt(any<String>()) }
    }

    @Test
    fun `test commandHistoryFlow decrypts data`() = runTest {
        // This test is harder because it relies on DataStore's internal state.
        // However, we can verify that when we call saveHistory, it uses encryption.
        // And we can trust that commandHistoryFlow uses decryption as implemented.

        val history = MementoHistory(undoStack = emptyList(), redoStack = emptyList())
        val encryptedJson = "encrypted_json"
        val decryptedJson = "{\"undoStack\":[],\"redoStack\":[]}"

        every { securityUtil.encrypt(any<String>()) } returns encryptedJson
        every { securityUtil.decryptSafe(encryptedJson) } returns decryptedJson

        store.saveHistory(history)

        val result = store.commandHistoryFlow.first()

        verify { securityUtil.decryptSafe(encryptedJson) }
        assertEquals(history, result)
    }
}
