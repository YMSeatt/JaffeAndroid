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

    private fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt
    }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-512")
        md.update(salt)
        return md.digest(password.toByteArray())
    }

    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hashedPassword = hash(password, salt)
        return "${salt.joinToString("") { "%02x".format(it) }}:${hashedPassword.joinToString("") { "%02x".format(it) }}"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return when {
            storedHash.contains(":") -> {
                val (saltHex, hashHex) = storedHash.split(":")
                val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                val newHash = hash(password, salt)
                val newHashHex = newHash.joinToString("") { "%02x".format(it) }
                newHashHex == hashHex
            }
            storedHash.length == 128 -> { // Legacy SHA-512
                val md = MessageDigest.getInstance("SHA-512")
                val digest = md.digest(password.toByteArray())
                val inputHash = digest.fold("") { str, it -> str + "%02x".format(it) }
                storedHash == inputHash
            }
            else -> { // Legacy SHA-256
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(password.toByteArray())
                val inputHash = digest.fold("") { str, it -> str + "%02x".format(it) }
                storedHash == inputHash
            }
        }
    }
}
