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
     * Analyzes the current state to generate ionization points.
     *
     * BOLT: Optimized using pre-grouped logs and manual loops to reduce complexity
     * from O(N*L) to O(N + L) and minimize allocations.
     */
    fun calculateIonization(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        batteryTemp: Float = 30.0f
    ): List<IonPoint> {
        val groupedLogs = behaviorLogs.groupBy { it.studentId }
        val tempFactor = (batteryTemp - 25f).coerceIn(0f, 20f) / 20f

        return students.map { student ->
            val sid = student.id.toLong()
            val logs = groupedLogs[sid]
            // BOLT: Use take(5) instead of takeLast(5) because logs are sorted DESC (newest first).
            val recentLogs = if (logs != null) {
                if (logs.size > 5) logs.subList(0, 5) else logs
            } else emptyList()

            // Calculate charge based on behavior balance
            // BOLT: Single-pass manual loop to avoid multiple traversals and allocations
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

            // Density scales with activity and "heat" (battery temp)
            // Baseline temp assumed 30°C. Higher temp = higher ion density.
            val density = (logCount.toFloat() / 5f * 0.7f + tempFactor * 0.3f).coerceIn(0f, 1.0f)

            IonPoint(
                x = student.xPosition.value,
                y = student.yPosition.value,
                charge = charge,
                density = density
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
}
