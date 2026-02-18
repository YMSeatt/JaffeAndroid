package com.example.myapplication.labs.ghost

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

/**
 * GhostSingularityEngine: Physics and Haptics for the Singularity experiment.
 *
 * This engine calculates the "Gravitational Pull" on UI elements and manages
 * the tactile feedback sequence as objects approach the event horizon.
 */
class GhostSingularityEngine(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Calculates the attraction force magnitude for an object at a given position.
     * Uses inverse-square law simulated for 2D UI space.
     */
    fun calculatePull(objectPos: Offset, singularityPos: Offset, radius: Float): Float {
        val dx = singularityPos.x - objectPos.x
        val dy = singularityPos.y - objectPos.y
        val dist = sqrt(dx * dx + dy * dy).coerceAtLeast(10f)

        // Normalize: 1.0 at event horizon, 0.0 far away
        return (radius / dist).coerceIn(0f, 2f)
    }

    /**
     * Triggers a "Gravitational Collapse" haptic sequence.
     * Uses Android 12+ Composition APIs to create a multi-stage tactile experience.
     */
    fun triggerCollapseHaptic(intensity: Float) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val composition = VibrationEffect.startComposition()

            // Stage 1: The Pull (Low Ticks)
            if (intensity > 0.3f) {
                composition.addPrimitive(
                    VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
                    (intensity * 0.4f).coerceIn(0f, 1f)
                )
            }

            // Stage 2: Spaghettification (Spin/Clicks)
            if (intensity > 0.7f) {
                composition.addPrimitive(
                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                    0.5f,
                    20
                )
            }

            // Stage 3: Event Horizon Crossing (Thud & Fall)
            if (intensity >= 1.0f) {
                composition.addPrimitive(
                    VibrationEffect.Composition.PRIMITIVE_THUD,
                    0.8f,
                    10
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    composition.addPrimitive(
                        VibrationEffect.Composition.PRIMITIVE_QUICK_FALL,
                        1.0f,
                        40
                    )
                }
            }

            try {
                vibrator.vibrate(composition.compose())
            } catch (e: Exception) {
                // Ignore hardware failures
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate((intensity * 50).toLong().coerceAtLeast(10))
        }
    }
}
