package com.example.myapplication.util

import android.content.Context
import java.io.File
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

interface KeyProvider {
    fun getKey(): ByteArray
}

@Singleton
class EncryptionKeyProvider @Inject constructor(
    private val context: Context
) : KeyProvider {

    private val _key: ByteArray by lazy {
        retrieveKey()
    }

    companion object {
        private const val KEY_FILE_NAME = "fernet.key"
        private const val KEY_SIZE = 32
    }

    override fun getKey(): ByteArray = _key

    private fun retrieveKey(): ByteArray {
        val keyFile = File(context.filesDir, KEY_FILE_NAME)
        return if (keyFile.exists()) {
            val keyBytes = keyFile.readBytes()
            if (keyBytes.size == KEY_SIZE) keyBytes else generateAndSaveKey(keyFile)
        } else {
            generateAndSaveKey(keyFile)
        }
    }

    private fun generateAndSaveKey(keyFile: File): ByteArray {
        val newKey = ByteArray(KEY_SIZE)
        SecureRandom().nextBytes(newKey)
        keyFile.writeBytes(newKey)
        return newKey
    }
}
