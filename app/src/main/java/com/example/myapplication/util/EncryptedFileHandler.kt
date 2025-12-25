package com.example.myapplication.util

import android.content.Context
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * Handles reading and writing encrypted files.
 * This utility uses the [EncryptionUtil] for cryptographic operations.
 */
class EncryptedFileHandler @Inject constructor() {

    /**
     * Reads a file and attempts to decrypt it.
     *
     * @param context The application context.
     * @param file The file to read.
     * @return The decrypted file content as a string, or null if the file does not exist or is empty.
     * @throws IOException if the file cannot be read or if decryption fails, wrapping the original
     *         [SecurityException] or [IllegalArgumentException] to prevent data corruption.
     */
    @Throws(IOException::class)
    fun readFile(context: Context, file: File): String? {
        if (!file.exists() || file.length() == 0L) {
            return null
        }

        val fileContentBytes = file.readBytes()
        val fileContentString = String(fileContentBytes, StandardCharsets.UTF_8)

        // Attempt to decrypt. If it fails, it's a critical error.
        return try {
            val decryptedBytes = EncryptionUtil.decrypt(context, fileContentString)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: SecurityException) {
            throw IOException("Failed to decrypt file: invalid signature or format.", e)
        } catch (e: IllegalArgumentException) {
            throw IOException("Failed to decrypt file: invalid Base64 encoding.", e)
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
            EncryptionUtil.encrypt(context, data.toByteArray(StandardCharsets.UTF_8)).toByteArray(StandardCharsets.UTF_8)
        } else {
            data.toByteArray(StandardCharsets.UTF_8)
        }
        file.writeBytes(dataBytes)
    }
}
