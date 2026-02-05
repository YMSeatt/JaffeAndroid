package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionsTest {

    @Test
    fun testToTitleCase() {
        assertEquals("Hello", "hello".toTitleCase())
        assertEquals("World", "WORLD".toTitleCase())
        assertEquals("Test", "tEsT".toTitleCase())
        assertEquals("This is a test", "this is a test".toTitleCase())
    }

    @Test
    fun testToHex() {
        assertEquals("00", byteArrayOf(0).toHex())
        assertEquals("ff", byteArrayOf(0xFF.toByte()).toHex())
        assertEquals("010203", byteArrayOf(1, 2, 3).toHex())
        assertEquals("deadbeef", byteArrayOf(0xde.toByte(), 0xad.toByte(), 0xbe.toByte(), 0xef.toByte()).toHex())
        assertEquals("", byteArrayOf().toHex())
    }
}
