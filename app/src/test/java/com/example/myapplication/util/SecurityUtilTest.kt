package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `hashPassword with SHA-512 produces correct hash`() {
        val password = "mysecretpassword"
        val expectedHash = "83bedc699569935a35bb661a352915da70870375b7c70be3124d5a3ecf811a6c6edee113b04308ca4c01a692ce125fa8ba6cdbb04272ebbcab4f366dc806b35a"

        val actualHash = SecurityUtil.hashPassword(password, "SHA-512")

        assertEquals(expectedHash, actualHash)
    }
}
