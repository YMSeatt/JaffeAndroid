package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class SecurityUtilTest {

    private lateinit var context: Context
    private lateinit var securityUtil: SecurityUtil

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Ensure the key file doesn't exist before each test
        val keyFile = File(context.filesDir, "fernet.key")
        if (keyFile.exists()) {
            keyFile.delete()
        }
        securityUtil = SecurityUtil(context)
    }

    @Test
    fun `test key generation and storage`() {
        val keyFile = File(context.filesDir, "fernet.key")
        assertTrue("Key file should be created", keyFile.exists())
        assertEquals("Key file should be 32 bytes long", 32, keyFile.readBytes().size)
    }

    @Test
    fun `test encrypt and decrypt`() {
        val originalText = "This is a secret message."
        val encryptedText = securityUtil.encrypt(originalText)
        val decryptedText = securityUtil.decrypt(encryptedText)
        assertEquals("Decrypted text should match original text", originalText, decryptedText)
    }

    @Test
    fun `test decryptSafe with valid token`() {
        val originalText = "Secure message"
        val encryptedText = securityUtil.encrypt(originalText)
        val decryptedText = securityUtil.decryptSafe(encryptedText)
        assertEquals("decryptSafe should decrypt valid tokens", originalText, decryptedText)
    }

    @Test
    fun `test decryptSafe with plain text`() {
        val plainText = "Plain text password"
        val decryptedText = securityUtil.decryptSafe(plainText)
        assertEquals("decryptSafe should return plain text if not a valid token", plainText, decryptedText)
    }

    @Test
    fun `test decryptSafe with blank string`() {
        val blankText = "   "
        val decryptedText = securityUtil.decryptSafe(blankText)
        assertEquals("decryptSafe should return blank string if input is blank", blankText, decryptedText)
    }

    @Test
    fun `test fallback decryption`() {
        val originalText = "This is a secret message."
        val oldKey = Key("7-BH7qsnKyRK0jdAZrjXSIW9VmcdpfHHeZor0ACBkmU=")
        val oldToken = Token.generate(oldKey, originalText)
        val decryptedText = securityUtil.decrypt(oldToken.serialise())
        assertEquals("Decrypted text should match original text", originalText, decryptedText)
    }

    @Test
    fun `test password hashing and verification`() {
        val password = "password123"
        val hashedPassword = SecurityUtil.hashPassword(password)
        assertTrue("Hashed password should contain a salt", hashedPassword.contains(":"))
        assertTrue("Verification should succeed with correct password", SecurityUtil.verifyPassword(password, hashedPassword))
        assertFalse("Verification should fail with incorrect password", SecurityUtil.verifyPassword("wrongpassword", hashedPassword))
    }

    @Test
    fun `test legacy password verification`() {
        val pass = "password123"
        // SHA-256 hash of "password123"
        val legacy256 = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"
        // SHA-512 hash of "password123"
        val legacy512 = "bed4efa1d4fdbd954bd3705d6a2a78270ec9a52ecfbfb010c61862af5c76af1761ffeb1aef6aca1bf5d02b3781aa854fabd2b69c790de74e17ecfec3cb6ac4bf"

        val verify256 = SecurityUtil.verifyPassword(pass, legacy256)
        if (!verify256) {
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(pass.toByteArray(Charsets.UTF_8))
            val actual = digest.joinToString("") { "%02x".format(java.util.Locale.US, it) }
            println("Legacy 256 failed. Expected: $legacy256, Actual: $actual")
        }
        assertTrue("Should verify legacy SHA-256 hash", verify256)

        val verify512 = SecurityUtil.verifyPassword(pass, legacy512)
        assertTrue("Should verify legacy SHA-512 hash", verify512)

        val verifyWrong = SecurityUtil.verifyPassword("wrong", legacy256)
        assertFalse("Should fail verification with wrong password for legacy hash", verifyWrong)
    }
}
