package com.example.myapplication.util

import com.macasaet.fernet.TokenValidationException
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**
 * Handles reading and writing encrypted files, with a graceful fallback to plaintext.
 * This utility uses [SecurityUtil] for cryptographic operations to align with the Python blueprint.
 */
class EncryptedFileHandler @Inject constructor() {

    /**
     * Reads a file, attempts to decrypt it, and returns the content as a string.
     * If decryption fails (e.g., invalid token, not Base64), it assumes the file is
     * plaintext and returns the content directly.
     *
     * @param file The file to read.
     * @return The file content as a string, or null if the file does not exist or is empty.
     * @throws IOException if the file cannot be read.
     */
    @Throws(IOException::class)
    fun readFile(file: File): String? {
        if (!file.exists() || file.length() == 0L) {
            return null
        }

        val fileContent = file.readText(StandardCharsets.UTF_8)

        // Try to decrypt first
        return try {
            SecurityUtil.decrypt(fileContent)
        } catch (e: TokenValidationException) {
            // If decryption fails, assume it's plaintext
            fileContent
        } catch (e: IllegalArgumentException) {
            // Also handle non-Base64 content
            fileContent
        }
    }

    /**
     * Encrypts data (if enabled) and writes it to a file.
     *
     * @param file The file to write to.
     * @param data The string data to write.
     * @param encrypt A boolean indicating whether to encrypt the data.
     * @throws IOException if the file cannot be written.
     */
    @Throws(IOException::class)
    fun writeFile(file: File, data: String, encrypt: Boolean) {
        val fileContent = if (encrypt) {
            SecurityUtil.encrypt(data)
        } else {
            data
        }
        file.writeText(fileContent, StandardCharsets.UTF_8)
    }
}
