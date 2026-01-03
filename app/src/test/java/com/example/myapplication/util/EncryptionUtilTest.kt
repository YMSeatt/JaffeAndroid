package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@RunWith(AndroidJUnit4::class)
@Config(manifest=Config.NONE)
class EncryptionUtilTest {

    private lateinit var context: Context
    private lateinit var encryptionUtil: EncryptionUtil

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        encryptionUtil = EncryptionUtil(context)
    }

    @Test
    fun `encrypt and decrypt returns original plaintext`() {
        val plaintext = "This is a secret message."
        val plaintextBytes = plaintext.toByteArray()

        val encryptedToken = encryptionUtil.encrypt(plaintextBytes)
        val decryptedBytes = encryptionUtil.decrypt(encryptedToken)

        assertEquals(plaintext, String(decryptedBytes))
    }

    @Test
    fun `encrypted token is not equal to plaintext`() {
        val plaintext = "This is another secret message."
        val plaintextBytes = plaintext.toByteArray()

        val encryptedToken = encryptionUtil.encrypt(plaintextBytes)

        assertNotEquals(plaintext, encryptedToken)
    }
}
