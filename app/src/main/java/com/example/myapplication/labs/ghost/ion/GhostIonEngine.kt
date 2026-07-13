package com.example.myapplication.labs.ghost.ion

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlin.math.abs
import kotlin.math.sin

/**
 * GhostIonEngine: Calculates classroom "Ionic Potential" and "Charge Density".
 *
 * This engine models students as energy points in a high-fidelity plasma field. It identifies
 * behavioral patterns and hardware signals (Battery Temperature) to calculate "Neural Ionization,"
 * a visual metaphor for classroom engagement and volatility.
 *
 * The logic is primarily used by the [GhostIonLayer] to drive AGSL shader effects on the
 * seating chart.
 */
object GhostIonEngine {

    /**
     * Represents a point of ionization in the classroom.
     *
     * @property x Logical X coordinate on the 4000x4000 canvas.
     * @property y Logical Y coordinate on the 4000x4000 canvas.
     * @property charge The behavioral polarity (-1.0 for negative/disruptive, 1.0 for positive).
     * @property density The concentration of activity (0.0 to 1.0), influenced by log
     *   frequency and hardware temperature.
     */
    data class IonPoint(
        val x: Float,
        val y: Float,
        val charge: Float,
        val density: Float
    )

    /**
     * Analyzes the current state to generate ionization metrics for students.
     *
     * This method performs a single-pass analysis over the most recent logs for each student
     * to determine their current "Charge" and "Density Base."
     *
     * BOLT: Optimized to accept pre-grouped logs, enabling execution in the background
     * pipeline (`updateStudentsForDisplay`) and eliminating redundant $O(L)$ grouping.
     *
     * @param studentIds The list of student IDs to analyze.
     * @param behaviorLogsByStudent Pre-grouped behavior logs, sorted by timestamp DESC.
     * @return A map of studentId to Pair(Charge, DensityBase).
     *   - Charge: -1.0 (Negative) to 1.0 (Positive).
     *   - DensityBase: 0.0 to 0.7 (Based on log frequency).
     */
    fun calculateIonMetrics(
        studentIds: List<Long>,
        behaviorLogsByStudent: Map<Long, List<BehaviorEvent>>
    ): Map<Long, Pair<Float, Float>> {
        val results = mutableMapOf<Long, Pair<Float, Float>>()

        for (sid in studentIds) {
            val logs = behaviorLogsByStudent[sid]
            // BOLT: Use subList instead of take for allocation-free view (if original is List).
            val recentLogs = if (logs != null) {
                if (logs.size > 5) logs.subList(0, 5) else logs
            } else emptyList()

            var positives = 0
            var negatives = 0
            for (log in recentLogs) {
                val type = log.type
                if (type.contains("Positive", ignoreCase = true) || type.contains("Participating", ignoreCase = true)) {
                    positives++
                } else if (type.contains("Negative", ignoreCase = true) || type.contains("Disruptive", ignoreCase = true)) {
                    negatives++
                }
            }

            val logCount = recentLogs.size
            val charge = if (logCount == 0) 0f else (positives - negatives).toFloat() / logCount.toFloat()

            // Base density based on log count (0.0 to 0.7).
            // Battery temp factor (0.3) is applied in the UI layer.
            val densityBase = (logCount.toFloat() / 5f * 0.7f).coerceIn(0f, 0.7f)

            results[sid] = charge to densityBase
        }

        return results
    }

    /**
     * Legacy structure for compatibility (to be removed once UI is refactored).
     *
     * This method handles the grouping and coordinate mapping in a single pass.
     * Prefer [calculateIonMetrics] for modern background-offloaded pipelines.
     *
     * @param students List of UI items containing positions.
     * @param behaviorLogs All relevant behavioral events.
     * @param batteryTemp Current device temperature in Celsius.
     */
    fun calculateIonization(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        batteryTemp: Float = 30.0f
    ): List<IonPoint> {
        val groupedLogs = behaviorLogs.groupBy { it.studentId }
        val studentIds = students.map { it.id.toLong() }
        val metrics = calculateIonMetrics(studentIds, groupedLogs)
        val tempFactor = (batteryTemp - 25f).coerceIn(0f, 20f) / 20f

        return students.map { student ->
            val m = metrics[student.id.toLong()] ?: (0f to 0f)
            IonPoint(
                x = student.xPosition.value,
                y = student.yPosition.value,
                charge = m.first,
                density = (m.second + tempFactor * 0.3f).coerceIn(0f, 1.0f)
            )
        }
    }

    /**
     * Utility to get battery temperature from the system.
     *
     * This uses the [Intent.ACTION_BATTERY_CHANGED] sticky intent.
     *
     * @return Temperature in Celsius.
     */
    fun getBatteryTemperature(context: Context): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temp / 10f // Convert to Celsius
    }

    /**
     * Calculates the global "Ion Balance" of the classroom.
     *
     * This averages the individual charges to determine the overall "Atmospheric Charge."
     *
     * BOLT: Use manual loop to avoid intermediate list allocation from map().
     */
    fun calculateGlobalBalance(points: List<IonPoint>): Float {
        if (points.isEmpty()) return 0f
        var sum = 0f
        for (p in points) {
            sum += p.charge
        }
        return sum / points.size
    }

    /**
     * Calculates the global "Ion Balance" from a metrics map.
     */
    fun calculateGlobalBalanceFromMetrics(metrics: Map<Long, Pair<Float, Float>>): Float {
        if (metrics.isEmpty()) return 0f
        var sum = 0f
        for (m in metrics.values) {
            sum += m.first
        }
        return sum / metrics.size
    }
}
