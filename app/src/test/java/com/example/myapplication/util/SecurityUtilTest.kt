package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SecurityUtilTest {

    @Test
    fun `hashPassword with SHA-512 produces correct hash`() {
        val password = "password123"
        val expectedHash = "b109f3bbbc244eb82441917ed06d618b9008dd09b3befd1b5e07394c706a8bb980b1d7785e5976ec049b46df5f1326af5a2ea6d103fd07c95385ffab0cacbc86"
        val actualHash = SecurityUtil.hashPassword(password, "SHA-512")
        assertEquals(expectedHash, actualHash)
    }

    @Test
    fun `hashPassword with SHA-256 produces correct hash for backward compatibility`() {
        val password = "password123"
        val expectedHash = "ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f"
        val actualHash = SecurityUtil.hashPassword(password, "SHA-256")
        assertEquals(expectedHash, actualHash)
    }
}
