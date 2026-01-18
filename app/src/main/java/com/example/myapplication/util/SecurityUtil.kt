package com.example.myapplication.util

import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import com.macasaet.fernet.Validator
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Locale
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

    fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        val random = SecureRandom()
        random.nextBytes(salt)

        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val hashedPassword = md.digest(password.toByteArray(Charsets.UTF_8))

        val saltHex = salt.fold("") { str, it -> str + String.format(Locale.US, "%02x", it) }
        val hashedPasswordHex = hashedPassword.fold("") { str, it -> str + String.format(Locale.US, "%02x", it) }

        return "$saltHex:$hashedPasswordHex"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        try {
            if (storedHash.contains(":")) {
                // New salted hash format
                val parts = storedHash.split(":")
                if (parts.size != 2) return false

                val salt = parts[0].chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                val originalHash = parts[1]

                val md = MessageDigest.getInstance("SHA-256")
                md.update(salt)
                val newHash = md.digest(password.toByteArray(Charsets.UTF_8))
                    .fold("") { str, it -> str + String.format(Locale.US, "%02x", it) }

                return newHash == originalHash
            } else {
                // Old unsalted hash format for backward compatibility
                val bytes = password.toByteArray(Charsets.UTF_8)
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hashedInput = digest.fold("") { str, it -> str + String.format(Locale.US, "%02x", it) }
                return hashedInput == storedHash
            }
        } catch (e: Exception) {
            return false
        }
    }
}
