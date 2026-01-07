package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `hashPassword with SHA-512 produces a consistent and correct hash`() {
        val password = "mysecretpassword"
        val expectedHash = "83bedc699569935a35bb661a352915da70870375b7c70be3124d5a3ecf811a6c6edee113b04308ca4c01a692ce125fa8ba6cdbb04272ebbcab4f366dc806b35a"
        val actualHash = SecurityUtil.hashPassword(password)

        assertEquals(expectedHash, actualHash)

        // Assert that the hash is consistent
        assertEquals(SecurityUtil.hashPassword(password), actualHash)

        // Assert that the hash is not the same as a different password
        assertNotEquals(SecurityUtil.hashPassword("wrongpassword"), actualHash)
    }

    @Test
    fun `hashPassword with SHA-256 for backward compatibility`() {
        val password = "mysecretpassword"
        val expectedHash = "94aefb8be78b2b7c344d11d1ba8a79ef087eceb19150881f69460b8772753263"
        val actualHash = SecurityUtil.hashPassword(password, "SHA-256")

        assertEquals(expectedHash, actualHash)

        // Assert that the hash is consistent
        assertEquals(SecurityUtil.hashPassword(password, "SHA-256"), actualHash)

        // Assert that the SHA-256 hash is different from the SHA-512 hash
        assertNotEquals(SecurityUtil.hashPassword(password), actualHash)
    }

    @Test
    fun `hashPassword with an empty password`() {
        val password = ""
        val expectedHashSha512 = "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e"
        val expectedHashSha256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

        assertEquals(expectedHashSha512, SecurityUtil.hashPassword(password))
        assertEquals(expectedHashSha256, SecurityUtil.hashPassword(password, "SHA-256"))
    }
}
