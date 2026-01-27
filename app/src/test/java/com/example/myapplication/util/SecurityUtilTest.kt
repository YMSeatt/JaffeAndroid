package com.example.myapplication.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `test salted hash and verification`() {
        val password = "password123"
        val hashedPassword = SecurityUtil.hashPassword(password)
        assertTrue(SecurityUtil.verifyPassword(password, hashedPassword))
    }

    @Test
    fun `test salted hash and verification with incorrect password`() {
        val password = "password123"
        val wrongPassword = "password456"
        val hashedPassword = SecurityUtil.hashPassword(password)
        assertFalse(SecurityUtil.verifyPassword(wrongPassword, hashedPassword))
    }

    @Test
    fun `test legacy unsalted hash verification`() {
        val password = "password"
        val legacyHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        assertTrue(SecurityUtil.verifyPassword(password, legacyHash))
    }

    @Test
    fun `test legacy unsalted hash verification with incorrect password`() {
        val password = "password"
        val wrongPassword = "wrongpassword"
        val legacyHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        assertFalse(SecurityUtil.verifyPassword(wrongPassword, legacyHash))
    }
}
