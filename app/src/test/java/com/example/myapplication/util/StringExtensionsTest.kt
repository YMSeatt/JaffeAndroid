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

    @Test
    fun testMaskEmail() {
        assertEquals("j****@gmail.com", "john.doe@gmail.com".maskEmail())
        assertEquals("a****@company.co.uk", "alice@company.co.uk".maskEmail())
        assertEquals("b****@protonmail.com", "bob123@protonmail.com".maskEmail())
        assertEquals("x", "x".maskEmail()) // Too short
        assertEquals("x@y.z", "x@y.z".maskEmail()) // Too short username
        assertEquals("", "".maskEmail())
    }

    @Test
    fun testMaskStudentName() {
        assertEquals("J. DOE", maskStudentName("John Doe"))
        assertEquals("J. DOE", maskStudentName("john doe"))
        assertEquals("J. SMITH", maskStudentName("  Jane   Smith  "))
        assertEquals("J. DOE", maskStudentName("John Quincey Doe")) // Takes first and last
        assertEquals("J****", maskStudentName("John")) // Hardened single name
        assertEquals("A****", maskStudentName("Alice"))
        assertEquals("X", maskStudentName("X")) // Single char name
        assertEquals("", maskStudentName("   "))
    }

    @Test
    fun testGenerateLogInitials() {
        assertEquals("GP", "Great Participation".generateLogInitials())
        assertEquals("OT", "Off Task".generateLogInitials())
        assertEquals("T", "Talking".generateLogInitials())
        assertEquals("MP", "Math Problems".generateLogInitials())
        assertEquals("MWH", "Multiple Words Here".generateLogInitials())
        assertEquals("S", "S".generateLogInitials())
        assertEquals("", "".generateLogInitials())
        assertEquals("", "   ".generateLogInitials())
        assertEquals("ABC", "  a b c  ".generateLogInitials())
    }
}
