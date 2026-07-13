package com.example.myapplication.labs.ghost.stellar

import com.example.myapplication.data.QuizLog
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.sqrt

/**
 * GhostStellarEngine: Neural Constellation Mapping Logic.
 *
 * This engine maps student data to stellar parameters:
 * - **Magnitude (Brightness)**: Driven by academic performance (Quiz scores).
 * - **Spectral Type (Color)**: Driven by behavioral balance (Positive vs Negative).
 * - **Constellation Links**: Groups of students are connected as constellations.
 */
object GhostStellarEngine {

    /**
     * Represents the calculated stellar state of a student.
     */
    data class StarNode(
        val studentId: Long,
        val x: Float,
        val y: Float,
        val magnitude: Float, // Brightness (0.0 to 1.0)
        val spectralColor: Long, // Hex color based on behavior
        val groupId: Int?
    )

    /**
     * BOLT: Synthesizes star nodes from student and quiz data in a single pass.
     */
    fun calculateStars(
        students: List<StudentUiItem>,
        quizLogsByStudent: Map<Long, List<QuizLog>>
    ): List<StarNode> {
        val stars = ArrayList<StarNode>(students.size)

        for (i in students.indices) {
            val student = students[i]
            val studentId = student.id.toLong()
            val logs = quizLogsByStudent[studentId] ?: emptyList()

            // Calculate magnitude based on average quiz score
            val avgScore = if (logs.isEmpty()) 0.7f else {
                var sum = 0f
                for (j in logs.indices) {
                    val log = logs[j]
                    sum += log.markValue?.toFloat()?.div(log.maxMarkValue?.toFloat() ?: 1f) ?: 0.7f
                }
                sum / logs.size
            }
            // Higher scores = brighter stars
            val magnitude = (avgScore).coerceIn(0.2f, 1.0f)

            // Spectral color based on behavior (Metaphor: Hot Blue to Cold Red)
            // Note: In this version, we'll use a simplified mapping for PoC
            val color = if (magnitude > 0.8f) 0xFF80D8FF else 0xFFFFA000 // Cyan for high, Amber for mid

            stars.add(StarNode(
                studentId = studentId,
                x = student.xPosition.value,
                y = student.yPosition.value,
                magnitude = magnitude,
                spectralColor = color,
                groupId = student.groupId.value
            ))
        }

        return stars
    }

    /**
     * Identifies constellation links within student groups.
     * Uses a star-topology (connect all to the first) to reduce clutter.
     */
    fun calculateConstellations(stars: List<StarNode>): List<Pair<StarNode, StarNode>> {
        val groups = stars.filter { it.groupId != null }.groupBy { it.groupId }
        val links = ArrayList<Pair<StarNode, StarNode>>()

        for (groupStars in groups.values) {
            if (groupStars.size < 2) continue
            val root = groupStars[0]
            for (j in 1 until groupStars.size) {
                links.add(root to groupStars[j])
            }
        }
        return links
    }
}
