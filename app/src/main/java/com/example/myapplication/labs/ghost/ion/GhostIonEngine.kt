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
 * This engine maps behavioral logs and hardware signals (Battery Temperature)
 * to a futuristic ionization metaphor.
 */
object GhostIonEngine {

    /**
     * Represents a point of ionization in the classroom.
     */
    data class IonPoint(
        val x: Float,
        val y: Float,
        val charge: Float, // -1.0 to 1.0 (Negative to Positive)
        val density: Float // 0.0 to 1.0
    )

    /**
     * Analyzes the current state to generate ionization metrics for students.
     *
     * BOLT: Optimized to accept pre-grouped logs, enabling execution in the
     * background pipeline and eliminating redundant O(L) grouping.
     *
     * @param studentIds The list of students to analyze.
     * @param behaviorLogsByStudent Pre-grouped behavior logs.
     * @return A map of studentId to Pair(Charge, DensityBase).
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
     */
    fun getBatteryTemperature(context: Context): Float {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        return temp / 10f // Convert to Celsius
    }

    /**
     * Calculates the global "Ion Balance" of the classroom.
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
