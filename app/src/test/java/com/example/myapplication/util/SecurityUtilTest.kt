package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `test hashPassword`() {
        val password = "password"
        val expectedHash = "b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86"
        assertEquals(expectedHash, SecurityUtil.hashPassword(password))
    }

    @Test
    fun `test hashPasswordSha256`() {
        val password = "password"
        val expectedHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        assertEquals(expectedHash, SecurityUtil.hashPasswordSha256(password))
    }
}
