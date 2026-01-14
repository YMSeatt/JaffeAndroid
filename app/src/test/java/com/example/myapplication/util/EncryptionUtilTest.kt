package com.example.myapplication.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.security.SecureRandom

class EncryptionUtilTest {

    private fun generateTestKey(): ByteArray {
        // Use a fixed seed for predictable "random" keys in tests
        val secureRandom = SecureRandom.getInstance("SHA1PRNG")
        secureRandom.setSeed(byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8))
        val key = ByteArray(32)
        secureRandom.nextBytes(key)
        return key
    }

    @Test
    fun `encrypt and decrypt returns original plaintext`() {
        val key = generateTestKey()
        val encryptionUtil = EncryptionUtil(key)
        val plaintext = "This is a secret message.".toByteArray()

        val token = encryptionUtil.encrypt(plaintext)
        val decryptedText = encryptionUtil.decrypt(token)

        assertArrayEquals(plaintext, decryptedText)
    }

    @Test
    fun `decryption fails with incorrect key`() {
        val key1 = generateTestKey()
        val encryptionUtil1 = EncryptionUtil(key1)

        val key2 = generateTestKey() // Generate a different key
        key2[0] = (key2[0] + 1).toByte() // Ensure it's different
        val encryptionUtil2 = EncryptionUtil(key2)

        val plaintext = "This will not be decrypted.".toByteArray()
        val token = encryptionUtil1.encrypt(plaintext)

        assertThrows(SecurityException::class.java) {
            encryptionUtil2.decrypt(token)
        }
    }

    @Test
    fun `decryption fails for tampered token`() {
        val key = generateTestKey()
        val encryptionUtil = EncryptionUtil(key)
        val plaintext = "Another secret message.".toByteArray()

        val token = encryptionUtil.encrypt(plaintext)
        val tamperedToken = token.substring(0, token.length - 2) + "AB" // Tamper the HMAC

        assertThrows(SecurityException::class.java) {
            encryptionUtil.decrypt(tamperedToken)
        }
    }

    @Test
    fun `decrypting token from another instance with same key succeeds`() {
        val key = generateTestKey()
        val encryptionUtil1 = EncryptionUtil(key)
        val encryptionUtil2 = EncryptionUtil(key)

        val plaintext = "Cross-instance test".toByteArray()
        val token = encryptionUtil1.encrypt(plaintext)
        val decryptedText = encryptionUtil2.decrypt(token)

        assertArrayEquals(plaintext, decryptedText)
    }

    @Test
    fun `TTL check works correctly`() {
        val key = generateTestKey()
        val encryptionUtil = EncryptionUtil(key)
        val plaintext = "This message has a short life".toByteArray()

        val token = encryptionUtil.encrypt(plaintext)

        // Wait for 2 seconds
        Thread.sleep(2000)

        // Decryption should fail with a TTL of 1 second
        assertThrows(SecurityException::class.java) {
            encryptionUtil.decrypt(token, ttl = 1)
        }

        // Decryption should succeed with a longer TTL
        val decryptedText = encryptionUtil.decrypt(token, ttl = 5)
        assertArrayEquals(plaintext, decryptedText)
    }
}
