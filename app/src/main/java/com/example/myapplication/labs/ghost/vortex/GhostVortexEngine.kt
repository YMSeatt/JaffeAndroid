package com.example.myapplication.labs.ghost.vortex

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.sqrt

/**
 * GhostVortexEngine: Identifies rotational behavior clusters in the classroom.
 *
 * This engine detects "Vortex" centers — areas where multiple students with
 * high behavioral activity are clustered together. It calculates the "Angular Momentum"
 * (intensity) of these vortices to drive spatial distortion effects.
 *
 * ### Physics Model:
 * The engine implements a "Social Whirlpool" model where student density and log
 * frequency act as localized pressure points. When multiple high-activity nodes
 * are in close proximity, their collective "Energy" is summed and normalized
 * to create a rotational field.
 */
object GhostVortexEngine {

    /**
     * Represents a detected behavioral vortex.
     */
    data class VortexPoint(
        val x: Float,
        val y: Float,
        val momentum: Float, // Rotational intensity (0.0 to 1.0)
        val radius: Float,   // Influence radius
        val polarity: Float, // 1.0 for positive, -1.0 for negative
        val centerStudentName: String = "Unknown"
    )

    /**
     * Identifies behavioral vortices based on student proximity and log density.
     *
     * BOLT: Optimized overload for Student entities and pre-grouped logs.
     * Replaces expensive functional operators with manual loops and uses squared
     * distance checks to avoid unnecessary sqrt calls.
     *
     * @param students List of students and their current positions.
     * @param behaviorLogsByStudent Historical behavior events grouped by studentId.
     * @param windowMillis Time window for analysis (default 10 minutes).
     * @return A list of detected [VortexPoint]s.
     */
    /** BOLT: Internal data structure to avoid Map lookups in nested loops. */
    private data class VortexNode(
        val x: Float,
        val y: Float,
        val intensity: Float,
        val polarity: Float,
        val name: String
    )

    /**
     * Identifies behavioral vortices based on student proximity and log density.
     *
     * ### Algorithm Steps:
     * 1. **Activity Filtering**: Scans all students and filters those with behavior logs
     *    within the [windowMillis] (default: 10 minutes).
     * 2. **Energy Calculation**: Computes 'Intensity' based on log frequency (capped at 5 logs)
     *    and 'Polarity' based on the ratio of positive vs negative logs.
     * 3. **Spatial Clustering**: Groups high-intensity students (intensity > 0.4) that are
     *    within the **800-logical-unit** cluster threshold.
     * 4. **Momentum Synthesis**: Calculates the average momentum and polarity of each cluster,
     *    identifying the "Vortex" center.
     *
     * @param students List of raw student entities.
     * @param behaviorLogsByStudent Pre-grouped behavior logs for O(1) student lookup.
     * @param windowMillis The temporal window for defining "recent" activity (default 10m).
     * @return A list of the top 5 most intense vortices.
     */
    fun identifyVortices(
        students: List<com.example.myapplication.data.Student>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>,
        windowMillis: Long = 600_000L
    ): List<VortexPoint> {
        if (students.isEmpty()) return emptyList()

        val currentTime = System.currentTimeMillis()
        val startTime = currentTime - windowMillis

        // BOLT: Collect only active students to transform O(N^2) into O(A^2)
        val activeNodes = mutableListOf<VortexNode>()

        for (student in students) {
            val logs = behaviorLogsByStudent[student.id] ?: continue
            var posCount = 0
            var negCount = 0
            var recentCount = 0

            // BOLT: Manual loop with early break assuming logs are DESC sorted by timestamp
            for (log in logs) {
                if (log.timestamp < startTime) break
                recentCount++
                if (log.type.contains("Negative", ignoreCase = true)) {
                    negCount++
                } else {
                    posCount++
                }
            }

            if (recentCount > 0) {
                val netPolarity = if (negCount > posCount) -1.0f else 1.0f
                val intensity = (recentCount.toFloat() / 5.0f).coerceIn(0f, 1.0f)
                val name = if (student.firstName.isNotBlank() || student.lastName.isNotBlank()) {
                    "${student.firstName} ${student.lastName}".trim()
                } else {
                    "Student ${student.id}"
                }
                activeNodes.add(VortexNode(student.xPosition, student.yPosition, intensity, netPolarity, name))
            }
        }

        if (activeNodes.isEmpty()) return emptyList()

        val vortices = mutableListOf<VortexPoint>()
        /**
         * The clustering threshold (800 units) defines the maximum distance between
         * students for them to be considered part of the same social vortex.
         * BOLT: Pre-calculated square avoids sqrt in the O(A^2) loop.
         */
        val clusterThresholdSq = 800f * 800f

        // BOLT: Optimized clustering using only active nodes
        for (i in activeNodes.indices) {
            val node = activeNodes[i]

            if (node.intensity > 0.4f) {
                // Check if this student is already near an identified vortex
                var nearExisting = false
                for (v in vortices) {
                    val dx = v.x - node.x
                    val dy = v.y - node.y
                    if (dx * dx + dy * dy < clusterThresholdSq) {
                        nearExisting = true
                        break
                    }
                }

                if (!nearExisting) {
                    // Find neighbors to calculate cluster momentum
                    var neighborEnergySum = 0f
                    var neighborPolaritySum = 0f
                    var neighborCount = 0

                    for (j in activeNodes.indices) {
                        if (i == j) continue
                        val other = activeNodes[j]
                        val dx = other.x - node.x
                        val dy = other.y - node.y
                        if (dx * dx + dy * dy < clusterThresholdSq) {
                            neighborEnergySum += other.intensity
                            neighborPolaritySum += other.polarity
                            neighborCount++
                        }
                    }

                    if (neighborCount > 0) {
                        val avgPolarity = (neighborPolaritySum + node.polarity) / (neighborCount + 1)
                        val momentum = ((node.intensity + neighborEnergySum) / (neighborCount + 1)).coerceIn(0.1f, 1.0f)

                        vortices.add(
                            VortexPoint(
                                x = node.x,
                                y = node.y,
                                momentum = momentum,
                                radius = 400f + (momentum * 600f),
                                polarity = if (avgPolarity >= 0) 1.0f else -1.0f,
                                centerStudentName = node.name
                            )
                        )
                    }
                }
            }
        }

        return vortices.sortedByDescending { it.momentum }.take(5)
    }

    /**
     * Compatibility overload for UI layers and legacy callers.
     * BOLT: Internally wraps students to avoid redundant mapping if possible.
     */
    fun identifyVortices(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        windowMillis: Long = 600_000L
    ): List<VortexPoint> {
        if (students.isEmpty()) return emptyList()

        val behaviorLogsByStudent = behaviorLogs.groupBy { it.studentId }
        val rawStudents = students.map { ui ->
            com.example.myapplication.data.Student(
                id = ui.id.toLong(),
                firstName = ui.fullName.value.split(" ").firstOrNull() ?: "",
                lastName = ui.fullName.value.split(" ").getOrNull(1) ?: "",
                gender = "",
                xPosition = ui.xPosition.value,
                yPosition = ui.yPosition.value
            )
        }
        return identifyVortices(rawStudents, behaviorLogsByStudent, windowMillis)
    }

    /**
     * Generates a Markdown-formatted report of the detected classroom vortices.
     * Parity-matched with `Python/ghost_vortex_analysis.py`.
     */
    fun generateVortexReport(
        vortices: List<VortexPoint>,
        totalStudentCount: Int
    ): String {
        val timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )

        val momentumIndex = if (totalStudentCount > 0) {
            vortices.sumOf { it.momentum.toDouble() }.toFloat() / totalStudentCount
        } else 0f

        val report = StringBuilder()
        report.append("# \uD83C\uDF00 GHOST VORTEX: ROTATIONAL SOCIAL ANALYSIS\n")
        report.append("**Classroom Momentum Index:** ${String.format(java.util.Locale.US, "%.2f", momentumIndex)}\n")
        report.append("**Detected Vortices:** ${vortices.size}\n")
        report.append("**Timestamp:** $timestamp\n\n")

        report.append("---\n\n")

        report.append("## \uD83C\uDF2A Behavioral Cyclones\n")
        report.append("Localized spirals of social energy detected in the classroom grid.\n\n")
        report.append("| Center Node | Angular Momentum | Social Polarity | Status |\n")
        report.append("| :--- | :--- | :--- | :--- |\n")

        if (vortices.isEmpty()) {
            report.append("| N/A | 0.00 | NEUTRAL | FLAT |\n")
        } else {
            vortices.forEach { v ->
                val polarityStr = if (v.polarity > 0) "POSITIVE (SYNERGY)" else "NEGATIVE (DISTRACTION)"
                val status = if (v.momentum > 0.7f) "HIGH ENERGY" else "STABLE ROTATION"
                report.append("| ${v.centerStudentName} | ${String.format(java.util.Locale.US, "%.2f", v.momentum)} | $polarityStr | $status |\n")
            }
        }

        report.append("\n---\n*Generated by Ghost Vortex Analysis Bridge v1.0 (Experimental)*")
        return report.toString()
    }
}
