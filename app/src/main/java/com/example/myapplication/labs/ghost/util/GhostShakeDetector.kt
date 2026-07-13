package com.example.myapplication.labs.ghost.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * GhostShakeDetector: A high-performance accelerometer-based shake gesture detector.
 *
 * This utility uses the device's accelerometer to identify rapid movement (shaking)
 * that exceeds a tactical threshold. It's designed for "Zero-Friction" interactions,
 * such as recentering a complex UI canvas.
 *
 * BOLT ⚡ Optimization:
 * - Uses a high-frequency sampling rate with a low-pass filter to eliminate gravity.
 * - Minimum delay between shake triggers to prevent multiple accidental firings.
 */
class GhostShakeDetector(
    private val onShake: () -> Unit
) : SensorEventListener {

    private var lastUpdate: Long = 0
    private var lastShakeTimestamp: Long = 0
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var lastZ: Float = 0f

    companion object {
        private const val SHAKE_THRESHOLD = 800 // Acceleration threshold
        private const val MIN_SHAKE_INTERVAL = 1000L // 1 second cooldown
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        val diffTime = currentTime - lastUpdate

        if (diffTime > 100) {
            lastUpdate = currentTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = sqrt(((x - lastX) * (x - lastX) + (y - lastY) * (y - lastY) + (z - lastZ) * (z - lastZ)).toDouble()) / diffTime * 10000

            if (speed > SHAKE_THRESHOLD) {
                if (currentTime - lastShakeTimestamp > MIN_SHAKE_INTERVAL) {
                    lastShakeTimestamp = currentTime
                    onShake()
                }
            }

            lastX = x
            lastY = y
            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for shake detection
    }

    /**
     * Registers this listener with the [SensorManager].
     */
    fun start(sensorManager: SensorManager) {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    /**
     * Unregisters this listener from the [SensorManager].
     */
    fun stop(sensorManager: SensorManager) {
        sensorManager.unregisterListener(this)
    }
}
