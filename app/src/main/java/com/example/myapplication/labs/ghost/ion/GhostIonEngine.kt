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
     */
    fun calculateIonization(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        batteryTemp: Float = 30.0f
    ): List<IonPoint> {
        return students.map { student ->
            val logs = behaviorLogs.filter { it.studentId == student.id.toLong() }
            val recentLogs = logs.takeLast(5)

            // Calculate charge based on behavior balance
            val positives = recentLogs.count { it.type.contains("Positive", ignoreCase = true) || it.type.contains("Participating", ignoreCase = true) }
            val negatives = recentLogs.count { it.type.contains("Negative", ignoreCase = true) || it.type.contains("Disruptive", ignoreCase = true) }

            val charge = if (recentLogs.isEmpty()) 0f else (positives - negatives).toFloat() / recentLogs.size.toFloat()

            // Density scales with activity and "heat" (battery temp)
            // Baseline temp assumed 30°C. Higher temp = higher ion density.
            val tempFactor = (batteryTemp - 25f).coerceIn(0f, 20f) / 20f
            val density = (recentLogs.size.toFloat() / 5f * 0.7f + tempFactor * 0.3f).coerceIn(0f, 1.0f)

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
     */
    fun calculateGlobalBalance(points: List<IonPoint>): Float {
        if (points.isEmpty()) return 0f
        return points.map { it.charge }.average().toFloat()
    }
}
