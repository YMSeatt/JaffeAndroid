package com.example.myapplication.util

import android.content.Context
import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import com.macasaet.fernet.Validator
import java.io.File
import java.security.SecureRandom
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.function.Function
import java.util.function.Supplier

class EncryptionUtil(private val context: Context) {

    private val keyFile = File(context.filesDir, "fernet.key")

    private val key: Key by lazy {
        if (keyFile.exists()) {
            Key(keyFile.readText())
        } else {
            val newKey = Key.generateKey(SecureRandom())
            keyFile.writeText(newKey.serialise())
            newKey
        }
    }

    class StringValidator : Validator<String> {
        override fun getTimeToLive(): Duration {
            return Duration.ofDays(365 * 100) // A long TTL
        }

        override fun getTransformer(): Function<ByteArray, String> {
            return Function { bytes -> String(bytes, Charsets.UTF_8) }
        }

        override fun getClock(): Clock {
            return Clock.systemUTC()
        }
    }

    fun encrypt(data: String): String {
        val token = Token.generate(key, data)
        return token.serialise()
    }

    fun decrypt(data: String): String {
        val token = Token.fromString(data)
        return token.validateAndDecrypt(key, StringValidator())
    }
}