package com.example.myapplication.util

import org.junit.Assert.*
import org.junit.Test

class SecurityUtilTest {
    @Test
    fun `verifyPassword with correct legacy SHA-256 password returns true`() {
        val password = "password"
        val legacyHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        assertTrue("Verification should succeed for correct legacy password", SecurityUtil.verifyPassword(password, legacyHash))
    }

    @Test
    fun `verifyPassword with incorrect legacy SHA-256 password returns false`() {
        val password = "password"
        val legacyHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        assertFalse("Verification should fail for incorrect legacy password", SecurityUtil.verifyPassword("wrongpassword", legacyHash))
    }

    @Test
    fun `hashPassword with salt produces a salted hash`() {
        val password = "password"
        val hash = SecurityUtil.hashPassword(password)
        assertTrue("Hash should contain a colon", hash.contains(":"))
        val parts = hash.split(":")
        assertEquals("Hash should have two parts", 2, parts.size)
        assertFalse("Salt should not be empty", parts[0].isEmpty())
        assertFalse("Hash should not be empty", parts[1].isEmpty())
    }

    @Test
    fun `verifyPassword with correct salted password returns true`() {
        val password = "mysecretpassword"
        val hash = SecurityUtil.hashPassword(password)
        assertTrue("Verification should succeed for correct password", SecurityUtil.verifyPassword(password, hash))
    }

    @Test
    fun `verifyPassword with incorrect salted password returns false`() {
        val password = "mysecretpassword"
        val hash = SecurityUtil.hashPassword(password)
        assertFalse("Verification should fail for incorrect password", SecurityUtil.verifyPassword("wrongpassword", hash))
    }

    @Test
    fun `verifyPassword with correct legacy SHA-512 password returns true`() {
        val password = "master_password_123"
        val legacyHash = "7a4f478a8456a599971055745099b22606a2f444f80878e1f133ad888383e5a40878d655f24ca8b3b7e289c8a32a1e8c95a045952d43e5d59b2d2a1a4577f520"
        assertTrue("Verification should succeed for correct legacy SHA-512 password", SecurityUtil.verifyPassword(password, legacyHash))
    }

    @Test
    fun `verifyPassword with incorrect legacy SHA-512 password returns false`() {
        val password = "master_password_123"
        val legacyHash = "7a4f478a8456a599971055745099b22606a2f444f80878e1f133ad888383e5a40878d655f24ca8b3b7e289c8a32a1e8c95a045952d43e5d59b2d2a1a4577f520"
        assertFalse("Verification should fail for incorrect legacy SHA-512 password", SecurityUtil.verifyPassword("wrong_master_password", legacyHash))
    }
}
