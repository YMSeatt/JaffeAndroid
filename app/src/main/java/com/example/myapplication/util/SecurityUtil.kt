package com.example.myapplication.util

import android.content.Context
import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import com.macasaet.fernet.Validator
import java.io.File
import java.nio.ByteBuffer
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.util.function.Function
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * A utility for handling Fernet encryption and decryption within the Android app.
 * It manages the secure storage and retrieval of the encryption key.
 */
class SecurityUtil(context: Context) {

    private val fernetCipher: FernetCipher

    companion object {
        private const val KEY_FILE_NAME = "fernet.key"
        private const val TTL_SECONDS = 60 * 60 * 24 * 365 * 100 // 100 years

        // The old, insecure hardcoded key. Used as a fallback for migrating existing data.
        private val FALLBACK_KEY = Key("7-BH7qsnKyRK0jdAZrjXSIW9VmcdpfHHeZor0ACBkmU=")

        private const val SALT_SIZE = 16
        private const val LEGACY_SHA256_HASH_LENGTH = 64
        private const val LEGACY_SHA512_HASH_LENGTH = 128

        fun hashPassword(password: String): String {
            val salt = ByteArray(SALT_SIZE)
            SecureRandom().nextBytes(salt)
            val saltedPassword = salt + password.toByteArray(Charsets.UTF_8)

            val md = MessageDigest.getInstance("SHA-512")
            val digest = md.digest(saltedPassword)

            return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" + Base64.encodeToString(digest, Base64.NO_WRAP)
        }

        fun verifyPassword(password: String, storedHash: String): Boolean {
            val parts = storedHash.split(":")
            return when {
                parts.size == 2 -> { // New salted SHA-512 format
                    val salt = Base64.decode(parts[0], Base64.NO_WRAP)
                    val originalHash = Base64.decode(parts[1], Base64.NO_WRAP)
                    val saltedPassword = salt + password.toByteArray(Charsets.UTF_8)

                    val md = MessageDigest.getInstance("SHA-512")
                    val newHash = md.digest(saltedPassword)

                    MessageDigest.isEqual(originalHash, newHash)
                }
                storedHash.length == LEGACY_SHA512_HASH_LENGTH -> { // Legacy unsalted SHA-512
                    val md = MessageDigest.getInstance("SHA-512")
                    val newHashBytes = md.digest(password.toByteArray(Charsets.UTF_8))
                    val storedHashBytes = hexStringToByteArray(storedHash)
                    MessageDigest.isEqual(newHashBytes, storedHashBytes)
                }
                storedHash.length == LEGACY_SHA256_HASH_LENGTH -> { // Legacy unsalted SHA-256
                    val md = MessageDigest.getInstance("SHA-256")
                    val newHashBytes = md.digest(password.toByteArray(Charsets.UTF_8))
                    val storedHashBytes = hexStringToByteArray(storedHash)
                    MessageDigest.isEqual(newHashBytes, storedHashBytes)
                }
                else -> false // Unknown format
            }
        }
        private fun hexStringToByteArray(hexString: String): ByteArray {
            val len = hexString.length
            val data = ByteArray(len / 2)
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(hexString[i], 16) shl 4) + Character.digit(hexString[i + 1], 16)).toByte()
                i += 2
            }
            return data
        }
    }

    init {
        val key = getKey(context.applicationContext)
        fernetCipher = FernetCipher(key)
    }

    /**
     * Retrieves the Fernet key from private app storage. If the key file doesn't exist,
     * it generates a new 32-byte key and saves it for future use.
     */
    private fun getKey(context: Context): ByteArray {
        val keyFile = File(context.filesDir, KEY_FILE_NAME)
        return if (keyFile.exists()) {
            val keyBytes = keyFile.readBytes()
            if (keyBytes.size == 32) keyBytes else generateAndSaveKey(keyFile)
        } else {
            generateAndSaveKey(keyFile)
        }
    }

    private fun generateAndSaveKey(keyFile: File): ByteArray {
        val newKey = ByteArray(32)
        SecureRandom().nextBytes(newKey)
        keyFile.writeBytes(newKey)
        return newKey
    }

    /**
     * Encrypts a plaintext string.
     */
    fun encrypt(plaintext: String): String {
        return fernetCipher.encrypt(plaintext.toByteArray())
    }

    /**
     * Decrypts a Fernet token. It first tries to decrypt with the new, secure key.
     * If that fails, it falls back to the old, hardcoded key to support data migration.
     */
    fun decrypt(token: String): String {
        return try {
            String(fernetCipher.decrypt(token, TTL_SECONDS))
        } catch (e: Exception) {
            // Fallback to old key
            val oldToken = Token.fromString(token)
            oldToken.validateAndDecrypt(FALLBACK_KEY, StringValidator())
        }
    }

    class StringValidator : Validator<String> {
        override fun getTimeToLive(): Duration {
            return Duration.ofDays(365 * 100) // A long TTL
        }

        override fun getTransformer(): Function<ByteArray, String> {
            return Function { bytes -> String(bytes) }
        }
    }
}

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

        return Base64.encodeToString(finalBuffer.array(), Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
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
        val decodedToken = Base64.decode(token, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
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
