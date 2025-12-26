package com.example.myapplication.util

import android.content.Context
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer
import java.util.Arrays

object EncryptionUtil {

    private const val VERSION = 0x80.toByte()
    private const val AES_BLOCK_SIZE = 16 // 128 bits
    private const val KEY_FILE_NAME = "fernet.key"

    fun getOrCreateKey(context: Context): ByteArray {
        val keyFile = File(context.filesDir, KEY_FILE_NAME)
        return if (keyFile.exists()) {
            val key = ByteArray(32)
            FileInputStream(keyFile).use { it.read(key) }
            key
        } else {
            val newKey = ByteArray(32)
            SecureRandom().nextBytes(newKey)
            FileOutputStream(keyFile).use { it.write(newKey) }
            newKey
        }
    }

    fun encrypt(data: ByteArray, key: ByteArray): String {
        require(key.size == 32) { "Key must be 32 bytes." }

        val signingKey = key.sliceArray(0..15)
        val encryptionKey = key.sliceArray(16..31)

        val iv = ByteArray(AES_BLOCK_SIZE)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(encryptionKey, "AES"), IvParameterSpec(iv))
        val ciphertext = cipher.doFinal(data)

        val timestamp = System.currentTimeMillis() / 1000
        val hmac = createHmac(signingKey, VERSION, timestamp, iv, ciphertext)

        val buffer = ByteBuffer.allocate(1 + 8 + iv.size + ciphertext.size + hmac.size)
        buffer.put(VERSION)
        buffer.putLong(timestamp)
        buffer.put(iv)
        buffer.put(ciphertext)
        buffer.put(hmac)

        return Base64.encodeToString(buffer.array(), Base64.URL_SAFE)
    }

    fun decrypt(token: String, key: ByteArray, ttl: Long? = null): ByteArray {
        require(key.size == 32) { "Key must be 32 bytes." }

        val decodedToken = Base64.decode(token, Base64.URL_SAFE)
        val buffer = ByteBuffer.wrap(decodedToken)

        val version = buffer.get()
        if (version != VERSION) {
            throw IllegalArgumentException("Invalid token version.")
        }

        val timestamp = buffer.long
        if (ttl != null) {
            val currentTime = System.currentTimeMillis() / 1000
            if (timestamp + ttl < currentTime) {
                throw SecurityException("Token has expired.")
            }
        }

        val iv = ByteArray(AES_BLOCK_SIZE)
        buffer.get(iv)

        val hmac = ByteArray(32)
        val ciphertext = ByteArray(buffer.remaining() - hmac.size)
        buffer.get(ciphertext)
        buffer.get(hmac)

        val signingKey = key.sliceArray(0..15)
        val encryptionKey = key.sliceArray(16..31)

        val expectedHmac = createHmac(signingKey, version, timestamp, iv, ciphertext)

        if (!MessageDigest.isEqual(hmac, expectedHmac)) {
            throw SecurityException("Invalid HMAC signature.")
        }

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(encryptionKey, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(ciphertext)
    }

    private fun createHmac(signingKey: ByteArray, version: Byte, timestamp: Long, iv: ByteArray, ciphertext: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(signingKey, "HmacSHA256"))

        val buffer = ByteBuffer.allocate(1 + 8 + iv.size + ciphertext.size)
        buffer.put(version)
        buffer.putLong(timestamp)
        buffer.put(iv)
        buffer.put(ciphertext)

        return mac.doFinal(buffer.array())
    }
}
