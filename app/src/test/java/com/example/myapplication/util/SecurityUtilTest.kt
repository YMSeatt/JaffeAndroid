package com.example.myapplication.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `hashPassword returns a salted hash`() {
        val password = "password123"
        val hash = SecurityUtil.hashPassword(password)

        // A basic check to ensure the hash contains a salt and a hash part
        assertTrue(hash.contains(":"))
        val parts = hash.split(":")
        assertTrue(parts.size == 2)
        assertTrue(parts[0].isNotEmpty())
        assertTrue(parts[1].isNotEmpty())
    }

    @Test
    fun `verifyPassword returns true for correct password`() {
        val password = "password123"
        val hash = SecurityUtil.hashPassword(password)
        assertTrue(SecurityUtil.verifyPassword(password, hash))
    }

    @Test
    fun `verifyPassword returns false for incorrect password`() {
        val password = "password123"
        val hash = SecurityUtil.hashPassword(password)
        assertFalse(SecurityUtil.verifyPassword("wrongpassword", hash))
    }

    @Test
    fun `verifyPassword returns false for malformed hash`() {
        val password = "password123"
        assertFalse(SecurityUtil.verifyPassword(password, "invalidhash"))
        assertFalse(SecurityUtil.verifyPassword(password, "invalid:hash:format"))
    }

    @Test
    fun `verifyPassword returns true for old unsalted hash`() {
        val password = "password123"
        // This is the SHA-256 hash of "password123"
        val oldHash = "ef92b778bafe771e89245b89ecea486f46051d716598218a582531e171b024d9"
        assertTrue(SecurityUtil.verifyPassword(password, oldHash))
    }
}
