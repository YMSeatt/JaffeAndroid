package com.example.myapplication.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `test salted SHA-512 hashing and verification`() {
        val password = "mysecretpassword"
        val hashedPassword = SecurityUtil.hashPassword(password)

        assertTrue("Verification of a correct password should succeed", SecurityUtil.verifyPassword(password, hashedPassword))
        assertFalse("Verification of an incorrect password should fail", SecurityUtil.verifyPassword("wrongpassword", hashedPassword))
    }

    @Test
    fun `test backward compatibility with unsalted SHA-256 hashes`() {
        val password = "legacy_password"

        // Manually hash with SHA-256 to get a valid hash for testing
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        val validOldHash = digest.fold("") { str, it -> str + "%02x".format(it) }

        assertTrue("Verification of a correct legacy password should succeed", SecurityUtil.verifyPassword(password, validOldHash))
        assertFalse("Verification of an incorrect legacy password should fail", SecurityUtil.verifyPassword("wrongpassword", validOldHash))
    }

    @Test
    fun `test backward compatibility with unsalted SHA-512 hashes`() {
        val password = "another_legacy_password"

        // Manually hash with SHA-512 to get a valid hash for testing
        val md = java.security.MessageDigest.getInstance("SHA-512")
        val digest = md.digest(password.toByteArray())
        val validOldHash = digest.fold("") { str, it -> str + "%02x".format(it) }

        assertTrue("Verification of a correct legacy password should succeed", SecurityUtil.verifyPassword(password, validOldHash))
        assertFalse("Verification of an incorrect legacy password should fail", SecurityUtil.verifyPassword("wrongpassword", validOldHash))
    }
}
