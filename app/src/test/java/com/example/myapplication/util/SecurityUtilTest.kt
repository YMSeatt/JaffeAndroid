package com.example.myapplication.util

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `test password hashing returns different hashes for the same password`() {
        val password = "password123"
        val hashedPassword1 = SecurityUtil.hashPassword(password)
        val hashedPassword2 = SecurityUtil.hashPassword(password)
        assertNotEquals(hashedPassword1, hashedPassword2)
    }

    @Test
    fun `test password verification`() {
        val password = "password123"
        val hashedPassword = SecurityUtil.hashPassword(password)
        assertTrue(SecurityUtil.verifyPassword(password, hashedPassword))
    }
}
