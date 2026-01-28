package com.example.myapplication.util

import com.macasaet.fernet.Key
import com.macasaet.fernet.Token
import com.macasaet.fernet.Validator
import java.security.MessageDigest
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
        java.security.SecureRandom().nextBytes(salt)
        val md = MessageDigest.getInstance("SHA-512")
        md.update(salt)
        val hashedPassword = md.digest(password.toByteArray())
        val saltString = java.util.Base64.getEncoder().encodeToString(salt)
        val hashString = java.util.Base64.getEncoder().encodeToString(hashedPassword)
        return "$saltString:$hashString"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            if (storedHash.contains(":")) {
                // New salted SHA-512 hash
                val (saltString, hashString) = storedHash.split(":", limit = 2)
                val salt = java.util.Base64.getDecoder().decode(saltString)
                val md = MessageDigest.getInstance("SHA-512")
                md.update(salt)
                val hashedPassword = md.digest(password.toByteArray())
                val computedHashString = java.util.Base64.getEncoder().encodeToString(hashedPassword)
                hashString == computedHashString
            } else {
                // Legacy unsalted hash
                val algorithm = if (storedHash.length == 128) "SHA-512" else "SHA-256"
                val md = MessageDigest.getInstance(algorithm)
                val digest = md.digest(password.toByteArray())
                val computedHash = digest.fold("") { str, it -> str + "%02x".format(it) }
                storedHash.equals(computedHash, ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
    }
}
