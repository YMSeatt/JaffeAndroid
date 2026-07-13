package com.example.myapplication.labs.ghost.osmosis

import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.data.QuizLog
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.exp

/**
 * GhostOsmosisEngine: Calculates Knowledge Diffusion and Behavioral Concentration.
 *
 * This engine models the classroom as a fluid field where academic performance
 * and behavioral patterns diffuse between students based on proximity.
 */
object GhostOsmosisEngine {
    private const val CANVAS_SIZE = 4000f
    private const val DIFFUSION_RADIUS = 1000f // Effective radius for osmosis
    /** BOLT: Pre-calculated constant for Gaussian weight (2 * 400^2) */
    private const val GAUSSIAN_DENOMINATOR = 320000f

    data class OsmoticNode(
        val id: Long,
        val x: Float,
        val y: Float,
        val knowledgePotential: Float, // 0..1 (Academic strength)
        val behaviorConcentration: Float // -1..1 (Negative to Positive behavior)
    )

    data class DiffusionGradient(
        val x: Float,
        val y: Float,
        val potential: Float,
        val color: Triple<Float, Float, Float>
    )

    /**
     * Categorization of the classroom's osmotic state.
     */
    enum class OsmosisStatus {
        VOID,
        STABLE,
        EQUILIBRIUM,
        HIGH_GRADIENT
    }

    /**
     * Represents the global classroom osmotic balance analysis.
     */
    data class OsmosisAnalysis(
        val status: OsmosisStatus,
        val balanceScore: Float,
        val totalInteractions: Int,
        val avgDiffusionDelta: Float
    )

    /**
     * Analyzes the overall classroom osmotic balance by summing pairwise interactions.
     * Ported from `Python/ghost_osmosis_analyzer.py`.
     *
     * This function performs an $O(N^2)$ analysis of student interactions to determine
     * the "Neural Equilibrium" of the classroom. It calculates the average 'Osmotic Pressure'
     * (difference in potential) weighted by spatial proximity using a Gaussian decay.
     *
     * @param students List of osmotic nodes representing students.
     * @param diffusionRadius The spatial range (in logical units) within which students influence one another.
     * @return An [OsmosisAnalysis] containing global metrics such as balance score and status.
     */
    fun analyzeOsmoticBalance(
        students: List<OsmoticNode>,
        diffusionRadius: Float = 1000f
    ): OsmosisAnalysis {
        if (students.isEmpty()) {
            return OsmosisAnalysis(OsmosisStatus.VOID, 0f, 0, 0f)
        }

        var totalDiffusion = 0.0
        var interactions = 0
        val diffusionRadiusSq = diffusionRadius * diffusionRadius

        for (i in students.indices) {
            for (j in i + 1 until students.size) {
                val s1 = students[i]
                val s2 = students[j]

                val dx = s1.x - s2.x
                val dy = s1.y - s2.y
                val distSq = dx * dx + dy * dy

                if (distSq < diffusionRadiusSq) {
                    // Calculate 'Osmotic Pressure' (difference in potential)
                    val kDiff = abs(s1.knowledgePotential - s2.knowledgePotential)
                    val bDiff = abs(s1.behaviorConcentration - s2.behaviorConcentration)

                    // Weight by proximity (Gaussian)
                    // BOLT: Removed sqrt and used pre-calculated denominator
                    val weight = exp(-distSq / GAUSSIAN_DENOMINATOR)
                    totalDiffusion += (kDiff + bDiff) * weight
                    interactions++
                }
            }
        }

        val avgDiffusion = if (interactions > 0) totalDiffusion / interactions else 0.0

        // Balance Score: Lower diffusion delta indicates a more 'balanced' classroom
        val balanceScore = (1.0 - (avgDiffusion * 2.0)).coerceAtLeast(0.0).toFloat()

        val status = when {
            balanceScore > 0.8f -> OsmosisStatus.EQUILIBRIUM
            balanceScore < 0.4f -> OsmosisStatus.HIGH_GRADIENT
            else -> OsmosisStatus.STABLE
        }

        return OsmosisAnalysis(
            status = status,
            balanceScore = balanceScore,
            totalInteractions = interactions,
            avgDiffusionDelta = avgDiffusion.toFloat()
        )
    }

    /**
     * Generates a Markdown-formatted report of the classroom's osmotic balance.
     * Ported from `Python/ghost_osmosis_analyzer.py`.
     *
     * The report categorizes the classroom climate (VOID, STABLE, EQUILIBRIUM, HIGH_GRADIENT)
     * and provides human-readable interpretations of the neural diffusion deltas.
     *
     * @param analysis The global osmotic analysis metrics calculated by [analyzeOsmoticBalance].
     * @param timestamp Optional fixed timestamp for the report (defaults to current time).
     * @return A formatted Markdown string suitable for display or export.
     */
    fun generateOsmosisReport(
        analysis: OsmosisAnalysis,
        timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    ): String {

        val report = StringBuilder()
        report.append("# 👻 GHOST OSMOSIS: NEURAL DIFFUSION ANALYSIS\n")
        report.append("**Classroom Status:** ${analysis.status.name}\n")
        report.append("**Osmotic Balance Score:** ${String.format(Locale.US, "%.1f", analysis.balanceScore * 100f)}%\n")
        report.append("**Timestamp:** $timestamp\n\n")

        report.append("---\n\n")

        report.append("## [OSMOTIC METRICS]\n")
        report.append("- Active Diffusion Zones: ${analysis.totalInteractions}\n")
        report.append("- Avg Diffusion Delta:   ${String.format(Locale.US, "%.3f", analysis.avgDiffusionDelta)}\n\n")

        report.append("## [INTERPRETATION]\n")
        when (analysis.status) {
            OsmosisStatus.EQUILIBRIUM -> report.append("The classroom has reached a state of neural equilibrium. Knowledge and behavior are evenly distributed.\n")
            OsmosisStatus.HIGH_GRADIENT -> report.append("Warning: High potential gradients detected. Significant disparity in academic or behavioral states between adjacent nodes.\n")
            OsmosisStatus.STABLE -> report.append("Classroom diffusion is stable. Organic knowledge exchange is occurring at nominal rates.\n")
            OsmosisStatus.VOID -> report.append("No student nodes detected for analysis.\n")
        }

        report.append("\n---\n*Generated by Ghost Osmosis Analysis Bridge v1.0 (Experimental)*")

        return report.toString()
    }

    /**
     * Calculates the diffusion gradients for a sampling grid across the logical canvas.
     *
     * This is used by the UI layer ([GhostOsmosisLayer]) to render the fluid field.
     * It maps the complex student potentials into a simplified grid of [DiffusionGradient]s
     * which are then visualized using AGSL shaders.
     *
     * @param students The list of student nodes to process.
     * @param gridSize The resolution of the sampling grid (e.g., 20x20).
     * @return A list of [DiffusionGradient] points for the UI to render.
     */
    fun calculateOsmosis(
        students: List<OsmoticNode>,
        gridSize: Int = 20
    ): List<DiffusionGradient> {
        val gradients = mutableListOf<DiffusionGradient>()
        val step = CANVAS_SIZE / gridSize
        val diffusionRadiusSq = DIFFUSION_RADIUS * DIFFUSION_RADIUS

        for (iy in 0 until gridSize) {
            for (ix in 0 until gridSize) {
                val gx = ix * step + step / 2f
                val gy = iy * step + step / 2f

                var totalKnowledge = 0f
                var totalBehavior = 0f
                var totalWeight = 0f

                // BOLT: Replace forEach with manual index loop.
                for (i in students.indices) {
                    val student = students[i]
                    val dx = gx - student.x
                    val dy = gy - student.y
                    val distSq = dx * dx + dy * dy

                    if (distSq < diffusionRadiusSq) {
                        // Gaussian decay for diffusion weight
                        // BOLT: Removed sqrt and used pre-calculated denominator
                        val weight = exp(-distSq / GAUSSIAN_DENOMINATOR).toFloat()
                        totalKnowledge += student.knowledgePotential * weight
                        totalBehavior += student.behaviorConcentration * weight
                        totalWeight += weight
                    }
                }

                if (totalWeight > 0) {
                    val avgK = (totalKnowledge / totalWeight).coerceIn(0f, 1f)
                    val avgB = (totalBehavior / totalWeight).coerceIn(-1f, 1f)

                    // Color mapping:
                    // Knowledge -> Blue/Cyan intensity
                    // Positive Behavior -> Green
                    // Negative Behavior -> Red
                    val r = if (avgB < 0) -avgB else 0f
                    val g = if (avgB > 0) avgB else 0f
                    val b = avgK

                    gradients.add(
                        DiffusionGradient(
                            x = gx,
                            y = gy,
                            potential = (avgK + (if (avgB > 0) avgB else -avgB)) / 2f,
                            color = Triple(r, g, b)
                        )
                    )
                }
            }
        }
        return gradients
    }

    /**
     * Calculates the 'Academic Potential' and 'Behavioral Concentration' for a student.
     *
     * **Academic Potential (kPotential)**: An average of Quiz scores and Homework completion status.
     * **Behavioral Concentration (bConcentration)**: A normalized ratio of positive vs negative logs.
     *
     * **BOLT Optimization**: Replaced functional operators (filter, map, average, count) with
     * manual loops to avoid redundant object allocations and multiple list traversals
     * per student during the high-frequency seating chart update pipeline.
     *
     * @return A Pair containing (kPotential [0..1], bConcentration [-1..1]).
     */
    fun calculateStudentPotentials(
        behaviorLogs: List<BehaviorEvent>,
        quizLogs: List<QuizLog>,
        homeworkLogs: List<HomeworkLog>
    ): Pair<Float, Float> {
        val kPotential = if (quizLogs.isEmpty() && homeworkLogs.isEmpty()) 0.5f else {
            val qAvg = if (quizLogs.isEmpty()) 0.5f else {
                var totalRatio = 0.0
                var count = 0
                // BOLT: Manual index loop instead of functional chain.
                for (i in quizLogs.indices) {
                    val log = quizLogs[i]
                    val v = log.markValue
                    val m = log.maxMarkValue
                    if (v != null && m != null && m > 0) {
                        totalRatio += (v / m)
                        count++
                    }
                }
                if (count > 0) (totalRatio / count).toFloat() else 0.5f
            }

            val hAvg = if (homeworkLogs.isEmpty()) 0.5f else {
                var doneCount = 0
                // BOLT: Manual index loop instead of filter/count.
                for (i in homeworkLogs.indices) {
                    val log = homeworkLogs[i]
                    if (log.status.contains("Done", ignoreCase = true)) {
                        doneCount++
                    }
                }
                doneCount.toFloat() / homeworkLogs.size
            }

            (qAvg + hAvg) / 2f
        }

        val bConcentration = if (behaviorLogs.isEmpty()) 0f else {
            var pos = 0
            var neg = 0
            // BOLT: Manual index loop instead of filter/count.
            for (i in behaviorLogs.indices) {
                val event = behaviorLogs[i]
                if (event.type.contains("Negative", ignoreCase = true)) {
                    neg++
                } else {
                    pos++
                }
            }
            (pos - neg).toFloat() / behaviorLogs.size
        }

        return Pair(kPotential.coerceIn(0f, 1f), bConcentration.coerceIn(-1f, 1f))
    }
}
