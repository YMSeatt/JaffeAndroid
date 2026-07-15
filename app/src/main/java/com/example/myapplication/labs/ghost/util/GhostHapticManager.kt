package com.example.myapplication.labs.ghost.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * GhostHapticManager: A high-fidelity haptic feedback wrapper.
 *
 * This manager leverages Android 15 primitives (and API 31+ compositions) to provide
 * tactical, eyes-free feedback for Ghost experiments.
 *
 * It provides a set of "Ghost" patterns that can be triggered from anywhere in the app,
 * automatically handling API level fallbacks.
 */
class GhostHapticManager(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Ghost Haptic Patterns
     */
    enum class Pattern {
        SUCCESS,        // Low-latency confirmation (Double Tick)
        ERROR,          // Harsh alert (Heavy Thud)
        NEURAL_THINKING, // Circular momentum (Spin + Pulse)
        UI_CLICK,       // Sharp, clean interaction
        SPARK_POP,      // Sudden, light burst
        NEURAL_FRICTION, // High-frequency resistance (Low Ticks)
        RESONANCE_POSITIVE, // Uplifting, rising pulse
        RESONANCE_NEGATIVE, // Sharp, descending thud
        RESONANCE_NEUTRAL   // Gentle, balanced ripple
    }

    /**
     * Triggers the specified [Pattern] with an optional [intensity] multiplier.
     */
    fun perform(pattern: Pattern, intensity: Float = 1.0f) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        when (pattern) {
            Pattern.SUCCESS -> playSuccess()
            Pattern.ERROR -> playError()
            Pattern.NEURAL_THINKING -> playNeuralThinking()
            Pattern.UI_CLICK -> playUiClick()
            Pattern.SPARK_POP -> playSparkPop()
            Pattern.NEURAL_FRICTION -> playNeuralFriction()
            Pattern.RESONANCE_POSITIVE -> playResonancePositive(intensity)
            Pattern.RESONANCE_NEGATIVE -> playResonanceNegative(intensity)
            Pattern.RESONANCE_NEUTRAL -> playResonanceNeutral(intensity)
        }
    }

    private fun playSuccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 1.0f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.7f, 50)
                .compose()
            vibrator?.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        }
    }

    private fun playError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 200), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(300)
        }
    }

    private fun playNeuralThinking() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.6f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 0.8f, 100)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SPIN, 1.0f, 100)
                .compose()
            vibrator?.vibrate(effect)
        } else {
            vibrator?.vibrate(VibrationEffect.createOneShot(500, 128))
        }
    }

    private fun playUiClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }

    private fun playSparkPop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.5f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 1.0f, 50)
                .compose()
            vibrator?.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }

    private fun playNeuralFriction() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.4f)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.6f, 30)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.8f, 30)
                .compose()
            vibrator?.vibrate(effect)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(100, 64))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(100)
        }
    }

    private fun playResonancePositive(intensity: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f * intensity)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 1.0f * intensity, 50)
                .compose()
            vibrator?.vibrate(effect)
        } else {
            playUiClick()
        }
    }

    private fun playResonanceNegative(intensity: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f * intensity)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.7f * intensity, 100)
                .compose()
            vibrator?.vibrate(effect)
        } else {
            playError()
        }
    }

    private fun playResonanceNeutral(intensity: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = VibrationEffect.startComposition()
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.6f * intensity)
                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.6f * intensity, 150)
                .compose()
            vibrator?.vibrate(effect)
        } else {
            playUiClick()
        }
    }
}
