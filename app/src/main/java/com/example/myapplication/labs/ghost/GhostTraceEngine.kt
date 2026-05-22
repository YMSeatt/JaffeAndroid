package com.example.myapplication.labs.ghost

import com.example.myapplication.data.LayoutData
import com.example.myapplication.data.LayoutTemplate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import androidx.compose.ui.geometry.Offset

/**
 * GhostTraceEngine: Spatiotemporal path visualization for student seat migrations.
 *
 * This engine aggregates student positions from historical [LayoutTemplate] snapshots
 * to reconstruct the "Trace" of where each student has been seated over time.
 */
object GhostTraceEngine {

    /**
     * Represents a single point in a student's spatiotemporal trace.
     */
    data class TracePoint(
        val position: Offset,
        val timestamp: Long // Using LayoutTemplate ID as a proxy for time
    )

    /**
     * Aggregates student positions from historical layout templates.
     *
     * BOLT ⚡ Optimization: Uses manual index loops and identity-preserving maps
     * to reconstruct traces with $O(T \times S)$ efficiency.
     *
     * @val templates The list of historical layout templates.
     * @return A map of student IDs to their respective list of [TracePoint]s.
     */
    fun calculateTraces(templates: List<LayoutTemplate>): Map<Long, List<TracePoint>> {
        val traces = mutableMapOf<Long, MutableList<TracePoint>>()
        val json = Json { ignoreUnknownKeys = true }

        // Sort templates by ID to approximate chronological order
        val sortedTemplates = templates.sortedBy { it.id }

        for (i in sortedTemplates.indices) {
            val template = sortedTemplates[i]
            try {
                val layoutData = json.decodeFromString<LayoutData>(template.layoutDataJson)
                val students = layoutData.students

                for (j in students.indices) {
                    val student = students[j]
                    val points = traces.getOrPut(student.id) { mutableListOf() }

                    val newPoint = Offset(student.x, student.y)

                    // Only add if the position has changed significantly from the last point
                    if (points.isEmpty() || (points.last().position - newPoint).getDistance() > 10f) {
                        points.add(TracePoint(newPoint, template.id))
                    }
                }
            } catch (e: Exception) {
                // Skip malformed templates
            }
        }

        return traces
    }
}
