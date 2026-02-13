package com.example.myapplication.util

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext
import com.macasaet.fernet.Validator
import android.util.Base64
import java.io.File
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.util.function.Function
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * A utility for handling Fernet encryption and decryption within the Android app.
 * It manages the secure storage and retrieval of the encryption key.
 */
@Singleton
class SecurityUtil @Inject constructor(@ApplicationContext context: Context) {

    private val fernetCipher: FernetCipher

    companion object {
        private const val KEY_FILE_NAME = "fernet.key"
        private const val KEY_FILE_NAME_V2 = "fernet.key.v2"
        private const val TTL_SECONDS = 60L * 60 * 24 * 365 * 100 // 100 years

        // PBKDF2 Configuration
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 100000
        private const val PBKDF2_KEY_LENGTH = 256
        private const val PBKDF2_PREFIX = "pbkdf2"

        // The old, insecure hardcoded key. Used as a fallback for migrating existing data.
        private val FALLBACK_KEY = Key("7-BH7qsnKyRK0jdAZrjXSIW9VmcdpfHHeZor0ACBkmU=")

        /**
         * Hashes a password using PBKDF2 with HMAC-SHA256.
         * The result is in the format "pbkdf2:iterations:saltHex:hashHex".
         */
        fun hashPassword(password: String): String {
            val salt = ByteArray(16)
            SecureRandom().nextBytes(salt)
            val saltHex = salt.toHex()

            val spec = PBEKeySpec(
                password.toCharArray(),
                salt,
                PBKDF2_ITERATIONS,
                PBKDF2_KEY_LENGTH
            )
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val hash = factory.generateSecret(spec).encoded
            val hashHex = hash.toHex()

            return "$PBKDF2_PREFIX:$PBKDF2_ITERATIONS:$saltHex:$hashHex"
        }

        /**
         * Verifies a password against a stored hash.
         * Supports PBKDF2, salted SHA-512, and legacy unsalted SHA-256/SHA-512 hashes.
         */
        fun verifyPassword(password: String, storedHash: String): Boolean {
            if (storedHash.startsWith("$PBKDF2_PREFIX:")) {
                val parts = storedHash.split(":")
                if (parts.size != 4) return false
                val iterations = parts[1].toIntOrNull() ?: return false
                val saltHex = parts[2]
                val expectedHashHex = parts[3]

                val salt = saltHex.hexToByteArray()
                val spec = PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    iterations,
                    PBKDF2_KEY_LENGTH
                )
                val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
                val actualHash = factory.generateSecret(spec).encoded
                val expectedHash = expectedHashHex.hexToByteArray()

                return MessageDigest.isEqual(actualHash, expectedHash)
            }

            return if (storedHash.contains(":")) {
                val parts = storedHash.split(":")
                if (parts.size != 2) return false
                val salt = parts[0]
                val expectedHash = parts[1]

                val md = MessageDigest.getInstance("SHA-512")
                val digest = md.digest((salt + password).toByteArray(Charsets.UTF_8))
                val actualHash = digest.toHex()
                MessageDigest.isEqual(actualHash.toByteArray(Charsets.UTF_8), expectedHash.toByteArray(Charsets.UTF_8))
            } else {
                when (storedHash.length) {
                    128 -> { // Legacy SHA-512 or SHA3-512 (Python compatibility)
                        val sha512 = hashPasswordUnsalted(password, "SHA-512")
                        if (sha512 != null && MessageDigest.isEqual(sha512.toByteArray(Charsets.UTF_8), storedHash.toByteArray(Charsets.UTF_8))) {
                            return true
                        }
                        val sha3 = hashPasswordUnsalted(password, "SHA3-512")
                        if (sha3 != null && MessageDigest.isEqual(sha3.toByteArray(Charsets.UTF_8), storedHash.toByteArray(Charsets.UTF_8))) {
                            return true
                        }
                        false
                    }
                    64 -> { // Legacy SHA-256
                        val actualHash = hashPasswordUnsalted(password, "SHA-256")
                        actualHash != null && MessageDigest.isEqual(actualHash.toByteArray(Charsets.UTF_8), storedHash.toByteArray(Charsets.UTF_8))
                    }
                    else -> false
                }
            }
        }

        private fun hashPasswordUnsalted(password: String, algorithm: String): String? {
            return try {
                val md = MessageDigest.getInstance(algorithm)
                val digest = md.digest(password.toByteArray(Charsets.UTF_8))
                digest.toHex()
            } catch (e: Exception) {
                null
            }
        }

        @Deprecated("Use hashPassword(String) for new passwords and verifyPassword for checking.")
        fun hashPassword(password: String, algorithm: String): String {
            return hashPasswordUnsalted(password, algorithm) ?: ""
        }
    }

    init {
        val key = getKey(context.applicationContext)
        fernetCipher = FernetCipher(key)
    }

    /**
     * Retrieves the Fernet key from private app storage. If the key file doesn't exist,
     * it generates a new 32-byte key and saves it for future use.
     * Hardened to use Android KeyStore (MasterKey) for key wrapping.
     */
    private fun getKey(context: Context): ByteArray {
        val masterKey = try {
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
        } catch (e: Exception) {
            null // KeyStore unavailable (e.g., unit tests)
        }

        val keyFileV2 = File(context.filesDir, KEY_FILE_NAME_V2)
        val legacyKeyFile = File(context.filesDir, KEY_FILE_NAME)

        return when {
            keyFileV2.exists() -> {
                loadKeyV2(context, keyFileV2, masterKey)
            }
            legacyKeyFile.exists() -> {
                val legacyKey = legacyKeyFile.readBytes()
                if (legacyKey.size == 32) {
                    saveKeyV2(context, keyFileV2, legacyKey, masterKey)
                    legacyKeyFile.delete()
                    legacyKey
                } else {
                    legacyKeyFile.delete()
                    generateAndSaveKeyV2(context, keyFileV2, masterKey)
                }
            }
            else -> {
                generateAndSaveKeyV2(context, keyFileV2, masterKey)
            }
        }
    }

    private fun generateAndSaveKeyV2(context: Context, file: File, masterKey: MasterKey?): ByteArray {
        val newKey = ByteArray(32)
        SecureRandom().nextBytes(newKey)
        saveKeyV2(context, file, newKey, masterKey)
        return newKey
    }

    private fun saveKeyV2(context: Context, file: File, key: ByteArray, masterKey: MasterKey?) {
        if (masterKey != null) {
            if (file.exists()) file.delete()
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            encryptedFile.openFileOutput().use { it.write(key) }
        } else {
            // Fallback for non-KeyStore environments (unit tests)
            if (isTestEnvironment()) {
                file.writeBytes(key)
            } else {
                throw SecurityException("KeyStore unavailable in production")
            }
        }
    }

    private fun loadKeyV2(context: Context, file: File, masterKey: MasterKey?): ByteArray {
        return if (masterKey != null) {
            val encryptedFile = EncryptedFile.Builder(
                context,
                file,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            encryptedFile.openFileInput().use { it.readBytes() }
        } else {
            if (isTestEnvironment()) {
                file.readBytes()
            } else {
                throw SecurityException("KeyStore unavailable in production")
            }
        }
    }

    /**
     * Detects if the application is running in a unit test environment (e.g., Robolectric).
     */
    private fun isTestEnvironment(): Boolean {
        return try {
            System.getProperty("robolectric.enabled") == "true" ||
                    Class.forName("org.robolectric.Robolectric") != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Encrypts a plaintext string.
     */
    fun encrypt(plaintext: String): String {
        return encrypt(plaintext.toByteArray(Charsets.UTF_8))
    }

    /**
     * Encrypts a plaintext byte array into a URL-safe Base64 Fernet token.
     */
    fun encrypt(plaintext: ByteArray): String {
        return fernetCipher.encrypt(plaintext)
    }

    /**
     * Decrypts a Fernet token into a UTF-8 string. It first tries to decrypt with the new, secure key.
     * If that fails, it falls back to the old, hardcoded key to support data migration.
     */
    fun decrypt(token: String): String {
        return String(decryptToByteArray(token), Charsets.UTF_8)
    }

    /**
     * Decrypts a Fernet token into a byte array. It first tries to decrypt with the new, secure key.
     * If that fails, it falls back to the old, hardcoded key to support data migration.
     */
    fun decryptToByteArray(token: String): ByteArray {
        return try {
            fernetCipher.decrypt(token, TTL_SECONDS)
        } catch (e: Exception) {
            // Fallback to old key
            val oldToken = Token.fromString(token)
            oldToken.validateAndDecrypt(FALLBACK_KEY, ByteArrayValidator())
        }
    }

    /**
     * Attempts to decrypt the given token. If decryption fails, it returns the original string.
     * This is useful for migrating plain-text data to encrypted data.
     */
    fun decryptSafe(token: String): String {
        if (token.isBlank()) return token
        return try {
            decrypt(token)
        } catch (e: Exception) {
            token
        }
    }

    class StringValidator : Validator<String> {
        override fun getTimeToLive(): Duration {
            return Duration.ofDays(365 * 100) // A long TTL
        }

        override fun getTransformer(): Function<ByteArray, String> {
            return Function { bytes -> String(bytes, Charsets.UTF_8) }
        }
    }

    class ByteArrayValidator : Validator<ByteArray> {
        override fun getTimeToLive(): Duration {
            return Duration.ofDays(365 * 100) // A long TTL
        }

        override fun getTransformer(): Function<ByteArray, ByteArray> {
            return Function { bytes -> bytes }
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

        return Base64.encodeToString(finalBuffer.array(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    /**
     * Decrypts a Fernet token and verifies its integrity and validity.
     *
     * @param token The URL-safe Base64 Fernet token.
     * @param ttl The time-to-live in seconds. If the token is older than this, decryption fails.
     * @return The original plaintext as a byte array.
     * @throws SecurityException if the token is invalid, tampered with, or expired.
     */
    fun decrypt(token: String, ttl: Long): ByteArray {
        val decodedToken = Base64.decode(token, Base64.URL_SAFE or Base64.NO_WRAP)
        val minLength = 1 + 8 + IV_SIZE + 1 + HMAC_SIZE // version + ts + iv + min-cipher-block + hmac
        if (decodedToken.size < minLength) {
            throw SecurityException("Invalid token length")
        }

        val hmacFromToken = decodedToken.takeLast(HMAC_SIZE).toByteArray()
        val messageToVerify = decodedToken.dropLast(HMAC_SIZE).toByteArray()

        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val calculatedHmac = mac.doFinal(messageToVerify)

        if (!MessageDigest.isEqual(hmacFromToken, calculatedHmac)) {
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
