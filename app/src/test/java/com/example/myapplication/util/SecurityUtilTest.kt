package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `hashPassword uses SHA-256 explicitly`() {
        val password = "mysecretpassword"
        val expectedHash = "94aefb8be78b2b7c344d11d1ba8a79ef087eceb19150881f69460b8772753263"
        val actualHash = SecurityUtil.hashPassword(password, "SHA-256")
        assertEquals(expectedHash, actualHash)
    }

    @Test
    fun `hashPassword uses SHA-512 by default`() {
        val password = "mysecretpassword"
        val expectedHash = "83bedc699569935a35bb661a352915da70870375b7c70be3124d5a3ecf811a6c6edee113b04308ca4c01a692ce125fa8ba6cdbb04272ebbcab4f366dc806b35a"
        val actualHash = SecurityUtil.hashPassword(password)
        assertEquals(expectedHash, actualHash)
    }
}
