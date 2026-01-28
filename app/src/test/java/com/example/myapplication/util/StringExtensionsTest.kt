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
}
