package com.example.myapplication.util

import android.content.Context
import java.io.File
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * A stateless class that implements the Fernet specification for symmetric encryption.
 * This class operates purely on byte arrays and has no Android dependencies, making it easily testable.
 *
 * @param key A 32-byte key used for both signing (first 16 bytes) and encryption (last 16 bytes).
 */
class FernetCipher(private val key: ByteArray) {

    private val signingKey: SecretKeySpec
    private val encryptionKey: SecretKeySpec

    companion object {
        private const val VERSION: Byte = 0x80.toByte()
        private const val HMAC_SIZE = 32
        private const val IV_SIZE = 16
    }

    init {
        if (key.size != 32) {
            throw IllegalArgumentException("Fernet key must be 32 bytes long.")
        }
        signingKey = SecretKeySpec(key, 0, 16, "HmacSHA256")
        encryptionKey = SecretKeySpec(key, 16, 16, "AES")
    }

    /**
     * Encrypts a plaintext byte array into a URL-safe Base64 Fernet token.
     */
    fun encrypt(plaintext: ByteArray): String {
        val iv = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, IvParameterSpec(iv))
        val ciphertext = cipher.doFinal(plaintext)

        val timestamp = System.currentTimeMillis() / 1000

        val buffer = ByteBuffer.allocate(1 + 8 + iv.size + ciphertext.size)
        buffer.put(VERSION)
        buffer.putLong(timestamp)
        buffer.put(iv)
        buffer.put(ciphertext)
        val messageToSign = buffer.array()

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val hmac = mac.doFinal(messageToSign)

        val finalBuffer = ByteBuffer.allocate(messageToSign.size + hmac.size)
        finalBuffer.put(messageToSign)
        finalBuffer.put(hmac)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(finalBuffer.array())
    }

    /**
     * Decrypts a Fernet token and verifies its integrity and validity.
     *
     * @param token The URL-safe Base64 Fernet token.
     * @param ttl The time-to-live in seconds. If the token is older than this, decryption fails.
     * @return The original plaintext as a byte array.
     * @throws SecurityException if the token is invalid, tampered with, or expired.
     */
    fun decrypt(token: String, ttl: Int): ByteArray {
        val decodedToken = Base64.getUrlDecoder().decode(token)
        val minLength = 1 + 8 + IV_SIZE + 1 + HMAC_SIZE // version + ts + iv + min-cipher-block + hmac
        if (decodedToken.size < minLength) {
            throw SecurityException("Invalid token length")
        }

        val hmacFromToken = decodedToken.takeLast(HMAC_SIZE).toByteArray()
        val messageToVerify = decodedToken.dropLast(HMAC_SIZE).toByteArray()

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val calculatedHmac = mac.doFinal(messageToVerify)

        if (!hmacFromToken.contentEquals(calculatedHmac)) {
            throw SecurityException("Invalid token signature")
        }

        val buffer = ByteBuffer.wrap(messageToVerify)
        val version = buffer.get()
        if (version != VERSION) {
            throw SecurityException("Invalid token version")
        }

        val timestamp = buffer.long
        val currentTime = System.currentTimeMillis() / 1000
        if (ttl > 0 && currentTime > timestamp + ttl) {
            throw SecurityException("Token has expired")
        }

        val iv = ByteArray(IV_SIZE)
        buffer.get(iv)

        val ciphertext = ByteArray(buffer.remaining())
        buffer.get(ciphertext)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, IvParameterSpec(iv))
        return cipher.doFinal(ciphertext)
    }
}

/**
 * A singleton utility for handling Fernet encryption and decryption within the Android app.
 *
 * This utility provides a backward-compatible encryption scheme. All new data is encrypted
 * with a primary, hardcoded key to ensure interoperability with the Python blueprint.
 *
 * For decryption, it first attempts to use the primary key. If that fails, it falls back
 * to a legacy, device-specific key (if one exists in `fernet.key`), ensuring that existing
 * users do not lose access to their data.
 */
object EncryptionUtil {
    private const val KEY_FILE_NAME = "fernet.key"
    private const val TTL_SECONDS = 0 // Disable TTL for interoperability

    // Hardcoded primary key from the Python blueprint.
    private const val PRIMARY_KEY_B64 = "7-BH7qsnKyRK0jdAZrjXSIW9VmcdpfHHeZor0ACBkmU="

    // Lazily initialized Fernet cipher for the primary, hardcoded key.
    private val primaryFernetCipher: FernetCipher by lazy {
        try {
            val key = Base64.getUrlDecoder().decode(PRIMARY_KEY_B64)
            FernetCipher(key)
        } catch (e: IllegalArgumentException) {
            // This should never happen if the key is valid Base64.
            throw RuntimeException("Failed to decode the primary Fernet key", e)
        }
    }

    // A cache for the legacy cipher to avoid repeated file I/O.
    private var legacyFernetCipher: FernetCipher? = null
    private var legacyKeyChecked = false

    /**
     * Retrieves the legacy, device-specific Fernet cipher if the key file exists.
     * This is used for backward compatibility to decrypt data for existing users.
     */
    @Synchronized
    private fun getLegacyCipher(context: Context): FernetCipher? {
        if (!legacyKeyChecked) {
            val keyFile = File(context.filesDir, KEY_FILE_NAME)
            if (keyFile.exists()) {
                val keyBytes = keyFile.readBytes()
                if (keyBytes.size == 32) {
                    legacyFernetCipher = FernetCipher(keyBytes)
                }
            }
            legacyKeyChecked = true
        }
        return legacyFernetCipher
    }

    /**
     * Encrypts a plaintext byte array using the primary, hardcoded key.
     * This method is context-independent.
     */
    fun encrypt(plaintext: ByteArray): String {
        return primaryFernetCipher.encrypt(plaintext)
    }

    /**
     * Decrypts a Fernet token with backward compatibility.
     *
     * It first attempts to decrypt with the primary key. If that fails with a
     * [SecurityException], it falls back to the legacy device-specific key, if available.
     *
     * @throws SecurityException if decryption fails with all available keys.
     */
    fun decrypt(context: Context, token: String, ttl: Int = TTL_SECONDS): ByteArray {
        try {
            // 1. Try decrypting with the primary, interoperable key.
            return primaryFernetCipher.decrypt(token, ttl)
        } catch (e: SecurityException) {
            // 2. If it fails, try the legacy, device-specific key as a fallback.
            getLegacyCipher(context.applicationContext)?.let {
                try {
                    return it.decrypt(token, ttl)
                } catch (legacyException: SecurityException) {
                    // If the legacy key also fails, throw the original exception.
                    throw e
                }
            }
            // 3. If there is no legacy key, re-throw the original exception.
            throw e
        }
    }
}
