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
         * Calculates the "Neural Flow" intensity of a classroom based on behavioral events.
         * Ported from Python blueprint (ghost_flux_simulator.py).
         *
         * @param studentCount Number of students in the classroom.
         * @param logCount Number of behavioral events recorded.
         * @return A normalized intensity value between 0.1 and 1.0.
         */
        fun calculateFlowIntensity(studentCount: Int, logCount: Int): Float {
            if (logCount == 0) return 0.1f

            // student_count = len(students) if students else 1
            val effectiveStudentCount = if (studentCount > 0) studentCount else 1

            // Calculate base intensity (Density of interaction)
            // intensity = (log_count / (student_count * 5.0))
            val baseIntensity = logCount.toFloat() / (effectiveStudentCount * 5.0f)

            // Apply sinusoidal 'Classroom Tempo' factor to simulate organic waves of engagement
            // tempo = 1.0 + 0.2 * math.sin(log_count / 5.0)
            val tempo = 1.0f + 0.2f * sin(logCount.toFloat() / 5.0f)

            return (baseIntensity * tempo).coerceIn(0.1f, 1.0f)
        }
    }
}
