package com.example.myapplication.labs.ghost.frost

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import com.example.myapplication.labs.ghost.GhostInsightEngine
import com.example.myapplication.labs.ghost.InsightStatus
import kotlin.math.sqrt

/**
 * GhostFrostEngine: Logic for calculating "Cold Zones" and frost intensity.
 *
 * This engine identifies areas of the classroom where behavioral entropy and
 * academic struggle are concentrated, visualizing them as procedural frost.
 * It synthesizes multiple data streams (behavior, quizzes, homework) to derive
 * a frost intensity value for each student.
 *
 * ### The "Cold Zone" Metaphor:
 * Much like a loss of thermal energy in a physical system, a "Cold Zone" represents
 * a decrease in positive classroom data energy. Areas with high academic friction
 * or behavioral turbulence "freeze" over, signaling a need for intervention.
 *
 * ### Intensity Factors:
 * 1. **Concerning Status**: High weight (0.4) if the student is flagged by [GhostInsightEngine].
 * 2. **Negative Proximity**: Medium weight (0.25 max) based on distance to recent negative logs.
 * 3. **Clustering**: Social weight (0.15 max) based on proximity to other concerning students.
 *
 * BOLT ⚡ Optimizations:
 * 1. **Identity-based grouping**: Pre-groups logs by student ID using [HashMap] to avoid O(N*L) scans.
 * 2. **Manual Indexing**: Replaces functional iterators with manual for-loops to eliminate [Iterator] churn.
 * 3. **Spatial Pruning**: Uses a proximity [radius] threshold to limit O(N^2) complexity.
 */
object GhostFrostEngine {

    /**
     * Represents a calculated frost node in the classroom.
     * @property studentId The ID of the student at the center of the frost.
     * @property x Logical X coordinate on the 4000x4000 canvas.
     * @property y Logical Y coordinate on the 4000x4000 canvas.
     * @property intensity The calculated frost intensity (0.0 to 1.0).
     */
    data class FrostNode(
        val studentId: Long,
        val x: Float,
        val y: Float,
        val intensity: Float
    )

    /**
     * Calculates frost nodes based on student data and positions.
     *
     * @param students List of all students.
     * @param behaviorLogs All behavior events.
     * @param quizLogs All quiz logs.
     * @param homeworkLogs All homework logs.
     * @param radius The radius (in logical units) to consider for "Cold Zone" influence.
     * @return A list of FrostNodes with calculated intensities.
     */
    fun calculateFrost(
        students: List<Student>,
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>,
        radius: Float = 400f
    ): List<FrostNode> {
        if (students.isEmpty()) return emptyList()

        val studentCount = students.size

        // BOLT: Pre-map students by ID for O(1) lookups
        val studentMap = HashMap<Long, Student>(studentCount)
        for (i in 0 until studentCount) {
            val s = students[i]
            studentMap[s.id.toLong()] = s
        }

        // BOLT: Group logs by student ID for O(1) access
        val behaviorGroupMap = HashMap<Long, ArrayList<BehaviorEvent>>()
        for (i in 0 until behaviorLogs.size) {
            val event = behaviorLogs[i]
            behaviorGroupMap.getOrPut(event.studentId) { ArrayList() }.add(event)
        }

        val quizGroupMap = HashMap<Long, ArrayList<QuizLog>>()
        for (i in 0 until quizLogs.size) {
            val log = quizLogs[i]
            quizGroupMap.getOrPut(log.studentId) { ArrayList() }.add(log)
        }

        val homeworkGroupMap = HashMap<Long, ArrayList<HomeworkLog>>()
        for (i in 0 until homeworkLogs.size) {
            val log = homeworkLogs[i]
            homeworkGroupMap.getOrPut(log.studentId) { ArrayList() }.add(log)
        }

        // 1. Identify "CONCERNING" students and Negative Events
        val concerningStudents = HashSet<Long>()
        val negativeEventPositions = ArrayList<Pair<Float, Float>>()

        for (i in 0 until studentCount) {
            val student = students[i]
            val sid = student.id.toLong()
            val logs = behaviorGroupMap[sid] ?: emptyList<BehaviorEvent>()
            val quizzes = quizGroupMap[sid] ?: emptyList<QuizLog>()
            val hws = homeworkGroupMap[sid] ?: emptyList<HomeworkLog>()

            val insight = GhostInsightEngine.generateInsight(student.name, logs, quizzes, hws)
            if (insight.status == InsightStatus.CONCERNING) {
                concerningStudents.add(sid)
            }

            // Also track negative event locations
            for (j in 0 until logs.size) {
                if (logs[j].type.contains("Negative", ignoreCase = true)) {
                    negativeEventPositions.add(student.xPosition.value to student.yPosition.value)
                }
            }
        }

        // 2. Calculate final intensities
        val nodes = ArrayList<FrostNode>()
        for (i in 0 until studentCount) {
            val s1 = students[i]
            val sid1 = s1.id.toLong()
            var totalIntensity = 0f

            // Factor A: Personal "Concerning" Status (Heavy Weight)
            if (concerningStudents.contains(sid1)) {
                totalIntensity += 0.4f
            }

            // Factor B: Proximity to Negative Events
            for (j in 0 until negativeEventPositions.size) {
                val pos = negativeEventPositions[j]
                val dx = s1.xPosition.value - pos.first
                val dy = s1.yPosition.value - pos.second
                val dist = sqrt(dx * dx + dy * dy)

                if (dist < radius) {
                    val weight = (1.0f - (dist / radius)).coerceIn(0f, 1f)
                    totalIntensity += weight * 0.25f
                }
            }

            // Factor C: Proximity to other CONCERNING students (Cold Zone Clustering)
            for (j in 0 until studentCount) {
                val s2 = students[j]
                val sid2 = s2.id.toLong()
                if (sid1 == sid2 || !concerningStudents.contains(sid2)) continue

                val dx = s1.xPosition.value - s2.xPosition.value
                val dy = s1.yPosition.value - s2.yPosition.value
                val dist = sqrt(dx * dx + dy * dy)

                if (dist < radius) {
                    val weight = (1.0f - (dist / radius)).coerceIn(0f, 1f)
                    totalIntensity += weight * 0.15f
                }
            }

            if (totalIntensity > 0.05f) {
                nodes.add(FrostNode(
                    studentId = sid1,
                    x = s1.xPosition.value,
                    y = s1.yPosition.value,
                    intensity = totalIntensity.coerceIn(0f, 1f)
                ))
            }
        }

        return nodes
    }
}
