package com.example.myapplication.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File
import java.security.SecureRandom

@RunWith(AndroidJUnit4::class)
@Config(manifest=Config.NONE)
class EncryptionUtilTest {

    private lateinit var context: Context
    private lateinit var legacyKeyFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        legacyKeyFile = File(context.filesDir, "fernet.key")
        // Clean up any old files before each test
        if (legacyKeyFile.exists()) {
            legacyKeyFile.delete()
        }
    }

    @After
    fun tearDown() {
        // Clean up the key file after each test
        if (legacyKeyFile.exists()) {
            legacyKeyFile.delete()
        }
        // Reset the singleton's state for test isolation
        val legacyCipherField = EncryptionUtil::class.java.getDeclaredField("legacyFernetCipher")
        legacyCipherField.isAccessible = true
        legacyCipherField.set(EncryptionUtil, null)

        val legacyKeyCheckedField = EncryptionUtil::class.java.getDeclaredField("legacyKeyChecked")
        legacyKeyCheckedField.isAccessible = true
        legacyKeyCheckedField.set(EncryptionUtil, false)
    }

    @Test
    fun `encrypt and decrypt with primary key works`() {
        val plaintext = "This is a test message for the primary key."
        val plaintextBytes = plaintext.toByteArray()

        val encryptedToken = EncryptionUtil.encrypt(plaintextBytes)
        val decryptedBytes = EncryptionUtil.decrypt(context, encryptedToken)

        assertThat(decryptedBytes).isEqualTo(plaintextBytes)
    }

    @Test
    fun `decrypt with backward compatibility for legacy key works`() {
        // 1. Generate and save a legacy key
        val legacyKey = ByteArray(32)
        SecureRandom().nextBytes(legacyKey)
        legacyKeyFile.writeBytes(legacyKey)

        // 2. Encrypt data using the legacy cipher directly
        val legacyCipher = FernetCipher(legacyKey)
        val plaintext = "This data was encrypted with a legacy key."
        val plaintextBytes = plaintext.toByteArray()
        val encryptedToken = legacyCipher.encrypt(plaintextBytes)

        // 3. Decrypt using the main EncryptionUtil, which should fall back to the legacy key
        val decryptedBytes = EncryptionUtil.decrypt(context, encryptedToken)

        assertThat(decryptedBytes).isEqualTo(plaintextBytes)
    }

    @Test
    fun `decrypt fails with tampered token`() {
        val plaintext = "Some data"
        val encryptedToken = EncryptionUtil.encrypt(plaintext.toByteArray())
        val tamperedToken = encryptedToken.dropLast(1) + "a" // Tamper the HMAC

        val exception = org.junit.jupiter.api.assertThrows<SecurityException> {
            EncryptionUtil.decrypt(context, tamperedToken)
        }
        assertThat(exception).hasMessageThat().contains("Invalid token signature")
    }

    @Test
    fun `decrypt with legacy key fails if token is tampered`() {
        val legacyKey = ByteArray(32)
        SecureRandom().nextBytes(legacyKey)
        legacyKeyFile.writeBytes(legacyKey)
        val legacyCipher = FernetCipher(legacyKey)
        val encryptedToken = legacyCipher.encrypt("legacy data".toByteArray())
        val tamperedToken = encryptedToken.dropLast(1) + "X"

        val exception = org.junit.jupiter.api.assertThrows<SecurityException> {
            EncryptionUtil.decrypt(context, tamperedToken)
        }
        assertThat(exception).hasMessageThat().contains("Invalid token signature")
    }

    @Test
    fun `decrypt prefers primary key when both could work`() {
        // Data encrypted with the primary key
        val plaintext = "primary key data"
        val plaintextBytes = plaintext.toByteArray()
        val primaryEncryptedToken = EncryptionUtil.encrypt(plaintextBytes)

        // A legacy key also exists
        val legacyKey = ByteArray(32)
        SecureRandom().nextBytes(legacyKey)
        legacyKeyFile.writeBytes(legacyKey)

        // Decryption should succeed using the primary key
        val decryptedBytes = EncryptionUtil.decrypt(context, primaryEncryptedToken)
        assertThat(decryptedBytes).isEqualTo(plaintextBytes)
    }
}
