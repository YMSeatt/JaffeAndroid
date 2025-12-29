package com.example.myapplication.util

import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import com.macasaet.fernet.Validator
import java.time.Duration
import java.util.function.Function

object SecurityUtil {

    // WARNING: This is a hardcoded key for legacy decryption and is a security risk.
    // Do not use for new encryption.
    private val ENCRYPTION_KEY = Key("7-BH7qsnKyRK0jdAZrjXSIW9VmcdpfHHeZor0ACBkmU=")

    class StringValidator : Validator<String> {
        override fun getTimeToLive(): Duration {
            return Duration.ofDays(366 * 100) // A long TTL
        }

        override fun getTransformer(): Function<ByteArray, String> {
            return Function { bytes -> String(bytes) }
        }
    }

    fun decrypt(data: String): String {
        val token = Token.fromString(data)
        return token.validateAndDecrypt(ENCRYPTION_KEY, StringValidator())
    }
}
