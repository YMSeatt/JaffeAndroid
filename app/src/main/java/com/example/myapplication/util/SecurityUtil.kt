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

    fun hashPassword(password: String): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val md = MessageDigest.getInstance("SHA-512")
        md.update(salt)
        val hashedPassword = md.digest(password.toByteArray())
        val saltString = salt.fold("") { str, it -> str + "%02x".format(it) }
        val hashString = hashedPassword.fold("") { str, it -> str + "%02x".format(it) }
        return "$saltString:$hashString"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return when {
            storedHash.contains(":") -> {
                // New salted SHA-512 format
                val parts = storedHash.split(":")
                if (parts.size != 2) return false
                val salt = parts[0]
                val hash = parts[1]
                val saltBytes = ByteArray(salt.length / 2) {
                    salt.substring(it * 2, it * 2 + 2).toInt(16).toByte()
                }
                val md = MessageDigest.getInstance("SHA-512")
                md.update(saltBytes)
                val hashedPassword = md.digest(password.toByteArray())
                val hashString = hashedPassword.fold("") { str, it -> str + "%02x".format(it) }
                hashString == hash
            }
            storedHash.length == 128 -> {
                // Unsalted SHA-512 (Master recovery password)
                val bytes = password.toByteArray()
                val md = MessageDigest.getInstance("SHA-512")
                val digest = md.digest(bytes)
                val inputHash = digest.fold("") { str, it -> str + "%02x".format(it) }
                inputHash == storedHash
            }
            else -> {
                // Legacy unsalted SHA-256 format
                val bytes = password.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val inputHash = digest.fold("") { str, it -> str + "%02x".format(it) }
                inputHash == storedHash
            }
        }
    }
}
