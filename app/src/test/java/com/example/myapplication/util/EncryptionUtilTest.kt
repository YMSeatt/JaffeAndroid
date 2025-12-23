package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EncryptionUtilTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `test encrypt and decrypt`() {
        val originalText = "This is a secret message"
        val encryptedData = EncryptionUtil.encrypt(context, originalText.toByteArray())
        val decryptedData = EncryptionUtil.decrypt(context, encryptedData)
        assertEquals(originalText, String(decryptedData))
    }
}
