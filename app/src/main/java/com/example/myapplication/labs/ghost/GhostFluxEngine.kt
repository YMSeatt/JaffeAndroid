package com.example.myapplication.labs.ghost

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.sin

/**
 * GhostFluxEngine: Hardware-level integration for the Flux experiment.
 *
 * Implements "Neural Haptics" using Android 15's VibrationEffect.Composition
 * to provide tactile feedback that matches the fluid flow visualization.
 */
class GhostFluxEngine(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Triggers a "Flow Pulse" haptic effect.
     *
     * @param intensity The current flow intensity (0.0 to 1.0).
     */
    fun triggerFlowPulse(intensity: Float) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val composition = VibrationEffect.startComposition()

            // Base flow tick: simulating the rhythmic flow of data
            composition.addPrimitive(
                VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
                (0.2f + (intensity * 0.5f)).coerceIn(0f, 1f)
            )

            // Flux Surge: High-intensity engagement triggers a rising haptic wave
            if (intensity > 0.8f) {
                composition.addPrimitive(
                    VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
                    1.0f, // Ported from Python blueprint: 100% amplitude
                    40 // ms delay for the surge effect
                )
            }

            try {
                vibrator.vibrate(composition.compose())
            } catch (e: Exception) {
                // Handle rare cases where composition might fail on specific hardware
            }
        } else {
            // Fallback for older legacy devices
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
    }

    companion object {
        /**
         * Calculates the "Neural Flow" intensity of a classroom based on behavioral events
         * and spatial density.
         *
         * @param studentCount Number of students in the classroom.
         * @param logCount Number of behavioral events recorded.
         * @param spatialDensity Normalized spatial clustering factor (0.0 to 1.0).
         * @return A normalized intensity value between 0.1 and 1.0.
         */
        fun calculateFlowIntensity(studentCount: Int, logCount: Int, spatialDensity: Float = 0f): Float {
            if (logCount == 0 && spatialDensity == 0f) return 0.1f

            // student_count = len(students) if students else 1
            val effectiveStudentCount = if (studentCount > 0) studentCount else 1

            // Calculate base intensity (Density of interaction)
            // intensity = (log_count / (student_count * 5.0))
            val baseIntensity = logCount.toFloat() / (effectiveStudentCount * 5.0f)

            // Apply sinusoidal 'Classroom Tempo' factor to simulate organic waves of engagement
            // tempo = 1.0 + 0.2 * math.sin(log_count / 5.0)
            val tempo = 1.0f + 0.2f * sin(logCount.toFloat() / 5.0f)

            // Spatial modulation: higher density increases flow turbulence
            val flowIntensity = (baseIntensity * tempo) + (spatialDensity * 0.3f)

            return flowIntensity.coerceIn(0.1f, 1.0f)
        }

        /**
         * Calculates the spatial density (clustering) of students on the logical canvas.
         * Returns a value between 0.0 (perfectly sparse) and 1.0 (highly clustered).
         *
         * BOLT Optimization: Uses primitive FloatArrays and manual loops for performance.
         */
        fun calculateSpatialDensity(studentX: FloatArray, studentY: FloatArray, count: Int = studentX.size): Float {
            if (count < 2) return 0f

            var totalClustering = 0f
            val threshold = 800f // Radius of "local influence" in logical units (4000x4000)

            // O(N^2) density check
            val actualCount = count.coerceAtMost(studentX.size)
            for (i in 0 until actualCount) {
                var localCount = 0
                for (j in 0 until actualCount) {
                    if (i == j) continue
                    val dx = studentX[i] - studentX[j]
                    val dy = studentY[i] - studentY[j]
                    val distSq = dx * dx + dy * dy
                    if (distSq < threshold * threshold) {
                        localCount++
                    }
                }
                totalClustering += localCount.toFloat() / (actualCount - 1).coerceAtLeast(1)
            }

            return (totalClustering / actualCount).coerceIn(0f, 1f)
        }
    }
}
