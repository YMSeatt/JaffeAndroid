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

    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        val digest = md.digest(password.toByteArray())
        val saltHex = salt.fold("") { str, it -> str + "%02x".format(it) }
        val digestHex = digest.fold("") { str, it -> str + "%02x".format(it) }
        return "$saltHex:$digestHex"
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val (saltHex, hashHex) = storedHash.split(":")
            val salt = ByteArray(saltHex.length / 2) {
                saltHex.substring(it * 2, it * 2 + 2).toInt(16).toByte()
            }
            val md = MessageDigest.getInstance("SHA-256")
            md.update(salt)
            val newDigest = md.digest(password.toByteArray())
            val newDigestHex = newDigest.fold("") { str, it -> str + "%02x".format(it) }
            newDigestHex == hashHex
        } catch (e: Exception) {
            false
        }
    }
}
