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
        // Ensure key files don't exist before each test
        File(context.filesDir, "fernet.key").delete()
        File(context.filesDir, "fernet.key.v2").delete()
        securityUtil = SecurityUtil(context)
    }

    @Test
    fun `test key generation and storage`() {
        val keyFileV2 = File(context.filesDir, "fernet.key.v2")
        assertTrue("Hardened key file should be created", keyFileV2.exists())
        val size = keyFileV2.readBytes().size
        println("Key file size: $size")
        assertTrue("Key file should have content", size >= 32)

        // Verify that we can actually use the key
        val text = "Test message"
        val encrypted = securityUtil.encrypt(text)
        assertEquals(text, securityUtil.decrypt(encrypted))
    }

    @Test
    fun `test migration from legacy key`() {
        // 1. Create a legacy plain-text key
        val legacyKeyFile = File(context.filesDir, "fernet.key")
        val legacyKey = ByteArray(32) { it.toByte() }
        legacyKeyFile.writeBytes(legacyKey)
        File(context.filesDir, "fernet.key.v2").delete()

        // 2. Initialize SecurityUtil - should trigger migration
        val newSecurityUtil = SecurityUtil(context)

        // 3. Verify legacy file is deleted and new file exists
        assertFalse("Legacy key file should be deleted", legacyKeyFile.exists())
        assertTrue("New key file should exist", File(context.filesDir, "fernet.key.v2").exists())

        // 4. Verify we can still encrypt/decrypt (meaning the key was migrated correctly)
        val text = "Migration test"
        val encrypted = newSecurityUtil.encrypt(text)
        assertEquals(text, newSecurityUtil.decrypt(encrypted))
    }

    @Test
    fun `test encrypt and decrypt`() {
        val originalText = "This is a secret message."
        val encryptedText = securityUtil.encrypt(originalText)
        val decryptedText = securityUtil.decrypt(encryptedText)
        assertEquals("Decrypted text should match original text", originalText, decryptedText)
    }

    @Test
    fun `test binary encrypt and decrypt`() {
        val originalData = byteArrayOf(0, 1, 2, 3, 4, 5, -1, -2, -3)
        val encryptedText = securityUtil.encrypt(originalData)
        val decryptedData = securityUtil.decryptToByteArray(encryptedText)
        assertTrue("Decrypted data should match original data", originalData.contentEquals(decryptedData))
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
        assertTrue("Hashed password should start with pbkdf2 prefix", hashedPassword.startsWith("pbkdf2:"))
        assertTrue("Verification should succeed with correct password", SecurityUtil.verifyPassword(password, hashedPassword))
        assertFalse("Verification should fail with incorrect password", SecurityUtil.verifyPassword("wrongpassword", hashedPassword))
    }

    @Test
    fun `test old salted password verification`() {
        val password = "password123"
        // Generate an old-style salted SHA-512 hash manually
        val salt = "1234567890abcdef1234567890abcdef"
        val md = java.security.MessageDigest.getInstance("SHA-512")
        val digest = md.digest((salt + password).toByteArray(Charsets.UTF_8))
        val hashHex = digest.joinToString("") { "%02x".format(it) }
        val oldStyleHash = "$salt:$hashHex"

        assertTrue("Should verify old salted SHA-512 hash", SecurityUtil.verifyPassword(password, oldStyleHash))
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

    @Test
    fun `test sha3-512 legacy verification from python`() {
        val pass = "password123"
        // SHA3-512 hash of "password123" from Python
        val legacySha3 = "4ad2c01fc6007f58720b00fc99b978c2a17c577859d31fdbba4b3a749de9383ac4b0738aeaf0b13337db8bfeaf9d8f87faa236fc3c8a68fbf23eb6862fadb86e"

        assertTrue("Should verify legacy SHA3-512 hash from Python", SecurityUtil.verifyPassword(pass, legacySha3))
        assertFalse("Should fail for wrong password with SHA3-512", SecurityUtil.verifyPassword("wrong", legacySha3))
    }
}
