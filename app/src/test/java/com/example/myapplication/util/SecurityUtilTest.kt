package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun `test fallback decryption`() {
        val originalText = "This is a secret message."
        val oldKey = Key("7-BH7qsnKyRK0jdAZrjXSIW9VmcdpfHHeZor0ACBkmU=")
        val oldToken = Token.generate(oldKey, originalText)
        val decryptedText = securityUtil.decrypt(oldToken.serialise())
        assertEquals("Decrypted text should match original text", originalText, decryptedText)
    }

    @Test
    fun `test salted SHA-512 password hashing and verification`() {
        val password = "password123"
        val hashedPassword = SecurityUtil.hashPassword(password)
        assertTrue("New hash should be in the salted format", hashedPassword.contains(":"))
        assertTrue("Password verification should succeed for new format", SecurityUtil.verifyPassword(password, hashedPassword))
    }

    @Test
    fun `test legacy SHA-512 password verification`() {
        val password = "master_password"
        val legacyHash = "a4b834d3e543e95f512833a6206f4772186981cb64047432f6f40c7468200c2423719b8830f653066316279930e4e5f7e8a937ab949b806d203f562e8a719c36"
        assertTrue("Password verification should succeed for legacy SHA-512 format", SecurityUtil.verifyPassword(password, legacyHash))
    }

    @Test
    fun `test legacy SHA-256 password verification`() {
        val password = "old_password"
        val legacyHash = "a33a04e58319f39396342898b5840c822e153b827e873b56f87a87e0586e969d"
        assertTrue("Password verification should succeed for legacy SHA-256 format", SecurityUtil.verifyPassword(password, legacyHash))
    }
}
