package com.example.myapplication.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.spec.SecretKeySpec

object SecurityUtil {

    // Key is URL-safe Base64 encoded. Must be decoded to get the raw 32 bytes.
    private const val ENCRYPTION_KEY_STRING = "7-BH7qsnKyRK0jdAZrjXSIW9VmcdpfHHeZor0ACBkmU="
    const val MASTER_RECOVERY_PASSWORD_HASH = "Recovery1Master2Password!1Jaffe3" // This should be a hash, not a plaintext password.

    /**
     * Returns the raw 32-byte key for Fernet encryption.
     */
    fun getEncryptionKey(): ByteArray {
        return Base64.getUrlDecoder().decode(ENCRYPTION_KEY_STRING)
    }

    /**
     * Hashes a password using SHA-256.
     * Note: In a real-world scenario, a stronger, salted hashing algorithm like Argon2 or scrypt should be used.
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
