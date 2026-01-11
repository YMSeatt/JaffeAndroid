package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `hashPassword with SHA-256 produces correct hash`() {
        val password = "password"
        val expectedHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8"
        // This test will fail until we modify the hashPassword function
        assertEquals(expectedHash, SecurityUtil.hashPassword(password, "SHA-256"))
    }

    @Test
    fun `hashPassword with SHA-512 produces correct hash`() {
        val password = "password"
        val expectedHash = "b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86"
        // This test will fail until we modify the hashPassword function
        assertEquals(expectedHash, SecurityUtil.hashPassword(password, "SHA-512"))
    }

    @Test
    fun `hashPassword with different algorithms produce different hashes`() {
        val password = "password"
        val sha256 = SecurityUtil.hashPassword(password, "SHA-256")
        val sha512 = SecurityUtil.hashPassword(password, "SHA-512")
        assertNotEquals(sha256, sha512)
    }
}
