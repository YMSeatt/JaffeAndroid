package com.example.myapplication.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.security.SecureRandom
import java.util.Base64

class FernetCipherTest {

    private val testKey = ByteArray(32).apply { SecureRandom().nextBytes(this) }
    private val cipher = FernetCipher(testKey)

    @Test
    fun `test successful round-trip`() {
        val originalText = "This is a secret message."
        val plaintext = originalText.toByteArray(Charsets.UTF_8)
        val token = cipher.encrypt(plaintext)
        val decryptedText = cipher.decrypt(token, ttl = 60)
        assertArrayEquals(plaintext, decryptedText)
    }

    @Test
    fun `test decryption fails with tampered token`() {
        val originalText = "This is a secret message."
        val plaintext = originalText.toByteArray(Charsets.UTF_8)
        val token = cipher.encrypt(plaintext)

        // Tamper with the token by changing a byte
        val decodedToken = Base64.getUrlDecoder().decode(token)
        decodedToken[decodedToken.size - 1] = (decodedToken[decodedToken.size - 1] + 1).toByte()
        val tamperedToken = Base64.getUrlEncoder().withoutPadding().encodeToString(decodedToken)

        val exception = assertThrows(SecurityException::class.java) {
            cipher.decrypt(tamperedToken, ttl = 60)
        }
        assertEquals("Invalid token signature", exception.message)
    }

    @Test
    fun `test decryption fails with expired token`() {
        val originalText = "This is a secret message."
        val plaintext = originalText.toByteArray(Charsets.UTF_8)
        val token = cipher.encrypt(plaintext)

        // Wait for the token to expire
        Thread.sleep(2000)

        val exception = assertThrows(SecurityException::class.java) {
            cipher.decrypt(token, ttl = 1) // 1-second TTL
        }
        assertEquals("Token has expired", exception.message)
    }
}
