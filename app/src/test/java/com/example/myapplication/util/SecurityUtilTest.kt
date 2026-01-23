package com.example.myapplication.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class SecurityUtilTest {

    @Test
    fun `test new salted SHA-512 password verification succeeds`() {
        val password = "mySecurePassword"
        val hashedPassword = SecurityUtil.hashPassword(password)
        assertTrue(SecurityUtil.verifyPassword(password, hashedPassword))
    }

    @Test
    fun `test new salted SHA-512 password verification fails for incorrect password`() {
        val password = "mySecurePassword"
        val wrongPassword = "wrongPassword"
        val hashedPassword = SecurityUtil.hashPassword(password)
        assertFalse(SecurityUtil.verifyPassword(wrongPassword, hashedPassword))
    }

    @Test
    fun `test legacy unsalted SHA-256 password verification succeeds`() {
        val password = "legacyPassword"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        val legacyHash = digest.fold("") { str, it -> str + "%02x".format(it) }
        assertTrue(SecurityUtil.verifyPassword(password, legacyHash))
    }

    @Test
    fun `test legacy unsalted SHA-256 password verification fails for incorrect password`() {
        val password = "legacyPassword"
        val wrongPassword = "wrongPassword"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(password.toByteArray())
        val legacyHash = digest.fold("") { str, it -> str + "%02x".format(it) }
        assertFalse(SecurityUtil.verifyPassword(wrongPassword, legacyHash))
    }

    @Test
    fun `test master recovery password verification succeeds`() {
        val masterPassword = "new_master_password"
        val md = MessageDigest.getInstance("SHA-512")
        val digest = md.digest(masterPassword.toByteArray())
        val masterHash = digest.fold("") { str, it -> str + "%02x".format(it) }
        assertTrue(SecurityUtil.verifyPassword(masterPassword, masterHash))
    }
}
