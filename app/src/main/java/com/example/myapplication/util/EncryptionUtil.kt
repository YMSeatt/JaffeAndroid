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

        if (!java.security.MessageDigest.isEqual(hmacFromToken, calculatedHmac)) {
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
 * It manages the secure storage and retrieval of the encryption key.
 */
object EncryptionUtil {
    private const val KEY_FILE_NAME = "fernet.key"
    private const val TTL_SECONDS = 60 * 60 // 1 hour TTL for decryption

    private var fernetCipher: FernetCipher? = null

    @Synchronized
    private fun getInstance(context: Context): FernetCipher {
        if (fernetCipher == null) {
            val key = getKey(context.applicationContext)
            fernetCipher = FernetCipher(key)
        }
        return fernetCipher!!
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
     * Encrypts a plaintext byte array.
     */
    fun encrypt(context: Context, plaintext: ByteArray): String {
        return getInstance(context).encrypt(plaintext)
    }

    /**
     * Decrypts a Fernet token.
     */
    fun decrypt(context: Context, token: String, ttl: Int = TTL_SECONDS): ByteArray {
        return getInstance(context).decrypt(token, ttl)
    }
}
