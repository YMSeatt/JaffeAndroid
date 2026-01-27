package com.example.myapplication.util

import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import com.macasaet.fernet.Validator
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.function.Function
import java.util.function.Supplier

object SecurityUtil {

    // WARNING: This is a hardcoded key and is a security risk.
    // In a real application, this should be handled more securely.
    private val ENCRYPTION_KEY = Key("7-BH7qsnKyRK0jdAZrjXSIW9VmcdpfHHeZor0ACBkmU=")

    class StringValidator : Validator<String> {
        override fun getTimeToLive(): Duration {
            return Duration.ofDays(365 * 100) // A long TTL
        }

        override fun getTransformer(): Function<ByteArray, String> {
            return Function { bytes -> String(bytes) }
        }
    }

    fun encrypt(data: String): String {
        val token = Token.generate(ENCRYPTION_KEY, data)
        return token.serialise()
    }

    fun decrypt(data: String): String {
        val token = Token.fromString(data)
        return token.validateAndDecrypt(ENCRYPTION_KEY, StringValidator())
    }

    private fun hashPassword(password: String, salt: ByteArray): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-512")
        md.update(salt)
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        val random = SecureRandom()
        random.nextBytes(salt)
        val hash = hashPassword(password, salt)
        return "${salt.joinToString("") { "%02x".format(it) }}:$hash"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return if (storedHash.contains(":")) {
            val parts = storedHash.split(":")
            val salt = parts[0]
            val hash = parts[1]
            val saltBytes = ByteArray(salt.length / 2)
            for (i in saltBytes.indices) {
                val index = i * 2
                val hex = salt.substring(index, index + 2)
                saltBytes[i] = hex.toInt(16).toByte()
            }
            val newHash = hashPassword(password, saltBytes)
            newHash == hash
        } else {
            // Legacy SHA-256 check
            val bytes = password.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val legacyHash = digest.fold("") { str, it -> str + "%02x".format(it) }
            storedHash == legacyHash
        }
    }
}
