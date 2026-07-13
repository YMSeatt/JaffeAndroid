package com.example.myapplication.labs.ghost

import com.example.myapplication.data.LayoutData
import com.example.myapplication.data.LayoutTemplate
import com.example.myapplication.data.StudentLayout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class GhostTraceEngineTest {

    @Test
    fun testCalculateTraces() {
        val student1 = StudentLayout(1, 100f, 100f)
        val student2 = StudentLayout(2, 200f, 200f)

        val layout1 = LayoutData(listOf(student1, student2), emptyList())
        val template1 = LayoutTemplate(1, "L1", Json.encodeToString(layout1))

        val student1_v2 = StudentLayout(1, 150f, 150f)
        val layout2 = LayoutData(listOf(student1_v2, student2), emptyList())
        val template2 = LayoutTemplate(2, "L2", Json.encodeToString(layout2))

        val templates = listOf(template1, template2)
        val traces = GhostTraceEngine.calculateTraces(templates)

        assertEquals(2, traces[1L]?.size ?: 0)
        assertEquals(1, traces[2L]?.size ?: 0)

        assertEquals(100f, traces[1L]!![0].position.x)
        assertEquals(150f, traces[1L]!![1].position.x)
    }

    @Test
    fun testCalculateTracesIgnoreSmallMovement() {
        val student1 = StudentLayout(1, 100f, 100f)
        val layout1 = LayoutData(listOf(student1), emptyList())
        val template1 = LayoutTemplate(1, "L1", Json.encodeToString(layout1))

        val student1_v2 = StudentLayout(1, 105f, 105f) // Distance ~7.07 < 10.0
        val layout2 = LayoutData(listOf(student1_v2), emptyList())
        val template2 = LayoutTemplate(2, "L2", Json.encodeToString(layout2))

        val templates = listOf(template1, template2)
        val traces = GhostTraceEngine.calculateTraces(templates)

        assertEquals(1, traces[1L]?.size ?: 0)
    }
}
