package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.nio.charset.StandardCharsets

class EncryptionUtilTest {

    @Test
    fun `encrypt and decrypt roundtrip should return original plaintext`() {
        val plaintext = "This is a secret message."
        val plaintextBytes = plaintext.toByteArray(StandardCharsets.UTF_8)

        // Encrypt the data
        val encryptedToken = EncryptionUtil.encrypt(plaintextBytes)

        // Ensure the token is not the same as the plaintext
        assertNotEquals(plaintext, encryptedToken)

        // Decrypt the data
        val decryptedBytes = EncryptionUtil.decrypt(encryptedToken)
        val decryptedText = String(decryptedBytes, StandardCharsets.UTF_8)

        // Ensure the decrypted text matches the original plaintext
        assertEquals(plaintext, decryptedText)
    }
}
