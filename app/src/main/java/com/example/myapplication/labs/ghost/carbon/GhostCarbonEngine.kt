package com.example.myapplication.labs.ghost.carbon

import com.example.myapplication.data.BehaviorEvent
import kotlin.math.sqrt

/**
 * GhostCarbonEngine: Identifies "Behavioral Twins" in the classroom.
 *
 * This engine analyzes the distribution of behavior log types for each student
 * and uses cosine similarity to find pairs of students with nearly identical
 * behavioral signatures.
 */
object GhostCarbonEngine {

    private const val SIMILARITY_THRESHOLD = 0.95f

    data class CarbonTwin(
        val studentA: Long,
        val studentB: Long,
        val similarity: Float
    )

    /**
     * Identifies pairs of students with matching behavioral patterns.
     *
     * BOLT: Optimized with manual loops and efficient vector math to avoid
     * collection churn during high-frequency updates.
     */
    fun identifyTwins(
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>
    ): List<CarbonTwin> {
        if (behaviorLogsByStudent.size < 2) return emptyList()

        // 1. Build behavioral vectors for each student
        val studentIds = behaviorLogsByStudent.keys.toList()
        val studentVectors = mutableMapOf<Long, Map<String, Int>>()

        for (i in studentIds.indices) {
            val studentId = studentIds[i]
            val logs = behaviorLogsByStudent[studentId] ?: continue
            if (logs.size < 3) continue // Need at least a small sample size

            val vector = mutableMapOf<String, Int>()
            for (j in logs.indices) {
                val type = logs[j].type
                vector[type] = (vector[type] ?: 0) + 1
            }
            studentVectors[studentId] = vector
        }

        val twins = mutableListOf<CarbonTwin>()
        val processedIds = studentVectors.keys.toList()

        // 2. Compare pairs using Cosine Similarity
        for (i in processedIds.indices) {
            val idA = processedIds[i]
            val vecA = studentVectors[idA] ?: continue

            for (j in i + 1 until processedIds.size) {
                val idB = processedIds[j]
                val vecB = studentVectors[idB] ?: continue

                val similarity = calculateCosineSimilarity(vecA, vecB)
                if (similarity >= SIMILARITY_THRESHOLD) {
                    twins.add(CarbonTwin(idA, idB, similarity))
                }
            }
        }

        return twins
    }

    /**
     * Calculates cosine similarity between two frequency vectors.
     *
     * Similarity = (A . B) / (||A|| * ||B||)
     */
    private fun calculateCosineSimilarity(vecA: Map<String, Int>, vecB: Map<String, Int>): Float {
        var dotProduct = 0.0
        var normA = 0.0
        for (entry in vecA) {
            val v = entry.value.toDouble()
            normA += v * v
            val vB = vecB[entry.key]?.toDouble() ?: 0.0
            dotProduct += v * vB
        }

        var normB = 0.0
        for (entry in vecB) {
            val v = entry.value.toDouble()
            normB += v * v
        }

        if (normA == 0.0 || normB == 0.0) return 0f

        return (dotProduct / (sqrt(normA) * sqrt(normB))).toFloat()
    }
}
