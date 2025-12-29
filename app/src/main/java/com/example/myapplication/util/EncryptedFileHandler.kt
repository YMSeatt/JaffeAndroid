package com.example.myapplication.util

import android.content.Context
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class EncryptedFileHandler @Inject constructor(
    private val encryptionUtil: EncryptionUtil
) {
    @Throws(IOException::class)
    fun readFile(file: File): String? {
        if (!file.exists() || file.length() == 0L) {
            return null
        }

        val fileContent = file.readText(StandardCharsets.UTF_8)

        return try {
            encryptionUtil.decrypt(fileContent)
        } catch (e: Exception) {
            // If decryption fails, assume it's plaintext
            fileContent
        }
    }

    @Throws(IOException::class)
    fun writeFile(file: File, data: String, encrypt: Boolean) {
        val contentToWrite = if (encrypt) {
            encryptionUtil.encrypt(data)
        } else {
            data
        }
        file.writeText(contentToWrite, StandardCharsets.UTF_8)
    }
}
