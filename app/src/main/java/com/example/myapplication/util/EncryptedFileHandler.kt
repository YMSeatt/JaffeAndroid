package com.example.myapplication.util

import android.content.Context
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * Handles reading and writing encrypted files, with a graceful fallback to plaintext.
 * This utility uses the existing [EncryptionUtil] for cryptographic operations.
 */
class EncryptedFileHandler @Inject constructor() {

    /**
     * Reads a file, attempts to decrypt it, and returns the content as a string.
     * If decryption fails, it assumes the file is plaintext and returns the content directly.
     *
     * @param context The application context.
     * @param file The file to read.
     * @return The file content as a string, or null if the file does not exist or is empty.
     * @throws IOException if the file cannot be read.
     */
    @Throws(IOException::class)
    fun readFile(context: Context, file: File): String? {
        if (!file.exists() || file.length() == 0L) {
            return null
        }

        val fileContentBytes = file.readBytes()
        val key = EncryptionUtil.getOrCreateKey(context)

        // Try to decrypt first
        return try {
            val token = String(fileContentBytes, StandardCharsets.UTF_8)
            val decryptedBytes = EncryptionUtil.decrypt(token, key, null) // No TTL for file decryption
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: SecurityException) {
            // If decryption fails, assume it's plaintext
            String(fileContentBytes, StandardCharsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            // Also handle Base64 decoding errors or invalid versions
            String(fileContentBytes, StandardCharsets.UTF_8)
        }
    }

    /**
     * Encrypts data (if enabled) and writes it to a file.
     *
     * @param context The application context.
     * @param file The file to write to.
     * @param data The string data to write.
     * @param encrypt A boolean indicating whether to encrypt the data.
     * @throws IOException if the file cannot be written.
     */
    @Throws(IOException::class)
    fun writeFile(context: Context, file: File, data: String, encrypt: Boolean) {
        val dataBytes = if (encrypt) {
            val key = EncryptionUtil.getOrCreateKey(context)
            val encryptedData = EncryptionUtil.encrypt(data.toByteArray(StandardCharsets.UTF_8), key)
            encryptedData.toByteArray(StandardCharsets.UTF_8)
        } else {
            data.toByteArray(StandardCharsets.UTF_8)
        }
        file.writeBytes(dataBytes)
    }
}
