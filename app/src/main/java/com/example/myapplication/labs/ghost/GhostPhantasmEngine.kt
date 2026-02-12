package com.example.myapplication.labs.ghost

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.annotation.RequiresApi
import java.util.concurrent.Executor
import java.util.function.Consumer

/**
 * GhostPhantasmEngine: Hardware-level integration for the Phantasm experiment.
 *
 * Handles:
 * 1. **Neural Heartbeat**: High-fidelity haptic feedback using VibrationEffect.Composition.
 * 2. **Stealth Detection**: Monitoring screen recording status (Android 15+).
 */
class GhostPhantasmEngine(private val context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Triggers a "Neural Heartbeat" haptic pulse.
     * The intensity and pattern depend on the classroom agitation level.
     *
     * @param agitation Level from 0.0 (calm) to 1.0 (agitated).
     */
    fun triggerHeartbeat(agitation: Float) {
        if (vibrator == null || !vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val composition = VibrationEffect.startComposition()

            // Primary pulse
            composition.addPrimitive(
                VibrationEffect.Composition.PRIMITIVE_TICK,
                0.5f + (agitation * 0.5f)
            )

            // Secondary "echo" pulse if agitated
            if (agitation > 0.6f) {
                composition.addPrimitive(
                    VibrationEffect.Composition.PRIMITIVE_LOW_TICK,
                    0.3f,
                    50 // delay
                )
            }

            vibrator.vibrate(composition.compose())
        } else {
            // Fallback for older devices
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    private var recordingCallback: Any? = null

    /**
     * Observes screen recording status using Android 15 APIs.
     *
     * @param executor The executor to run the callback on.
     * @param onStatusChanged Callback receiving true if recording is active.
     */
    fun observeScreenRecording(executor: Executor, onStatusChanged: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= 35) { // Android 15+
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            windowManager?.let { wm ->
                try {
                    val callback = Consumer<Int> { state ->
                        onStatusChanged(state != 0)
                    }

                    val method = wm.javaClass.getMethod("addScreenRecordingCallback", Executor::class.java, Consumer::class.java)
                    method.invoke(wm, executor, callback)
                    recordingCallback = callback
                } catch (e: Exception) {
                    // Fallback or Mock for non-supported environments
                }
            }
        }
    }

    /**
     * Unregisters the screen recording observer.
     */
    fun stopObservingScreenRecording() {
        if (Build.VERSION.SDK_INT >= 35 && recordingCallback != null) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            windowManager?.let { wm ->
                try {
                    val method = wm.javaClass.getMethod("removeScreenRecordingCallback", Consumer::class.java)
                    method.invoke(wm, recordingCallback)
                    recordingCallback = null
                } catch (e: Exception) {
                    // Fallback
                }
            }
        }
    }
}
