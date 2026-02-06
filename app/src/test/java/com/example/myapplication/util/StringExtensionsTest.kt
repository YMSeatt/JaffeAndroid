package com.example.myapplication.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StringExtensionsTest {

    @Test
    fun testToTitleCase() {
        assertEquals("Hello", "hello".toTitleCase())
        assertEquals("World", "WORLD".toTitleCase())
        assertEquals("Test", "tEsT".toTitleCase())
        assertEquals("This Is A Test", "this is a test".toTitleCase())
        assertEquals("Quiz Log", "QUIZ_LOG".toTitleCase())
        assertEquals("John Doe", "john-doe".toTitleCase())
        assertEquals("Multiple Spaces", "multiple  spaces".toTitleCase())
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
