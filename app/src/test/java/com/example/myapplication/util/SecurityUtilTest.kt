package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `test password hashing with default algorithm (SHA-256)`() {
        val password = "password123"
        val expectedHash = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"
        val actualHash = SecurityUtil.hashPassword(password)
        assertEquals(expectedHash, actualHash)
    }

    @Test
    fun `test password hashing with SHA-256`() {
        val password = "password123"
        val expectedHash = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"
        val actualHash = SecurityUtil.hashPassword(password, "SHA-256")
        assertEquals(expectedHash, actualHash)
    }

    @Test
    fun `test password hashing with SHA-512`() {
        val password = "password123"
        val expectedHash = "bed4efa1d4fdbd954bd3705d6a2a78270ec9a52ecfbfb010c61862af5c76af1761ffeb1aef6aca1bf5d02b3781aa854fabd2b69c790de74e17ecfec3cb6ac4bf"
        val actualHash = SecurityUtil.hashPassword(password, "SHA-512")
        assertEquals(expectedHash, actualHash)
    }

    @Test
    fun `test encryption and decryption`() {
        val originalText = "This is a secret message"
        val encryptedText = SecurityUtil.encrypt(originalText)
        val decryptedText = SecurityUtil.decrypt(encryptedText)
        assertEquals(originalText, decryptedText)
    }
}
