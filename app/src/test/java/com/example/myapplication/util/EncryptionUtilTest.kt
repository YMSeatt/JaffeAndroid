package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class EncryptionUtilTest {

    private lateinit var context: Context
    private lateinit var key: ByteArray

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Ensure we have a key for testing
        key = EncryptionUtil.getOrCreateKey(context)
    }

    @Test
    fun testKeyCreationAndRetrieval() {
        val keyFile = File(context.filesDir, "fernet.key")
        assert(keyFile.exists())
        val retrievedKey = EncryptionUtil.getOrCreateKey(context)
        assertNotNull(retrievedKey)
        assertArrayEquals(key, retrievedKey)
    }

    @Test
    fun testEncryptDecryptRoundtrip() {
        val originalText = "This is a secret message."
        val encrypted = EncryptionUtil.encrypt(originalText.toByteArray(), key)
        val decrypted = EncryptionUtil.decrypt(encrypted, key)
        assertArrayEquals(originalText.toByteArray(), decrypted)
    }

    @Test(expected = SecurityException::class)
    fun testDecryptWithInvalidHmac() {
        val originalText = "This is another secret message."
        val encrypted = EncryptionUtil.encrypt(originalText.toByteArray(), key)

        // To properly test the HMAC, we decode, tamper a byte of the ciphertext, and re-encode.
        // The HMAC is the last 32 bytes, so we tamper a byte just before it.
        val decoded = android.util.Base64.decode(encrypted, android.util.Base64.URL_SAFE)
        decoded[decoded.size - 33] = (decoded[decoded.size - 33] + 1).toByte()
        val tamperedEncrypted = android.util.Base64.encodeToString(decoded, android.util.Base64.URL_SAFE)

        EncryptionUtil.decrypt(tamperedEncrypted, key)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDecryptWithInvalidVersion() {
        val originalText = "This is a third secret message."
        val encrypted = EncryptionUtil.encrypt(originalText.toByteArray(), key)

        // To tamper with the version, we need to decode, modify, and re-encode
        val decoded = android.util.Base64.decode(encrypted, android.util.Base64.URL_SAFE)
        decoded[0] = 0x90.toByte() // Invalid version
        val tamperedEncrypted = android.util.Base64.encodeToString(decoded, android.util.Base64.URL_SAFE)

        EncryptionUtil.decrypt(tamperedEncrypted, key)
    }

    @Test(expected = SecurityException::class)
    fun testTokenExpiration() {
        val originalText = "This message should expire."
        val encrypted = EncryptionUtil.encrypt(originalText.toByteArray(), key)

        // Wait for 2 seconds to ensure the token expires
        Thread.sleep(2000)

        // Attempt to decrypt with a TTL of 1 second
        EncryptionUtil.decrypt(encrypted, key, ttl = 1)
    }
}
