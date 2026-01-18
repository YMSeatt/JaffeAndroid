package com.example.myapplication.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun fromStringMap() {
        val json = """{"key1":1,"key2":2}"""
        val expectedMap = mapOf("key1" to 1, "key2" to 2)
        assertEquals(expectedMap, converters.fromStringMap(json))
    }

    @Test
    fun fromMap() {
        val map = mapOf("key1" to 1, "key2" to 2)
        val expectedJson = """{"key1":1,"key2":2}"""
        assertEquals(expectedJson, converters.fromMap(map))
    }
}
