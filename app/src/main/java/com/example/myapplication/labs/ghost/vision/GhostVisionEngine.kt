package com.example.myapplication.labs.ghost.vision

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * GhostVisionEngine: Neural AR Sensor Fusion Engine.
 *
 * This engine tracks the device's 3D orientation using the rotation vector sensor
 * and projects 2D seating chart coordinates into a virtual 360-degree AR viewport.
 *
 * Teachers can physically "look around" the classroom by moving the device,
 * revealing student data points projected onto a virtual cylinder or sphere.
 */
class GhostVisionEngine(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // Current rotation matrix derived from sensors
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Observable states for UI binding
    val azimuth = mutableStateOf(0f)   // Yaw (Rotation around Z)
    val pitch = mutableStateOf(0f)     // Pitch (Rotation around X)
    val roll = mutableStateOf(0f)      // Roll (Rotation around Y)

    /**
     * Projects a 2D seating chart coordinate (0..4000) into screen-space
     * based on the current AR orientation.
     *
     * @param x Seating chart X coordinate.
     * @param y Seating chart Y coordinate.
     * @param viewportWidth Width of the screen viewport.
     * @param viewportHeight Height of the screen viewport.
     * @return The projected Offset, or null if outside the current Field of View (FoV).
     */
    fun project(
        x: Float,
        y: Float,
        viewportWidth: Float,
        viewportHeight: Float
    ): Offset? {
        // Map 4000x4000 logical canvas to angular space (-PI..PI)
        val targetAzimuth = ((x - 2000f) / 2000f) * Math.PI.toFloat()
        val targetPitch = ((y - 2000f) / 2000f) * (Math.PI.toFloat() / 2f)

        // Calculate angular delta relative to current device orientation
        var deltaAz = targetAzimuth - azimuth.value
        // Normalize azimuth delta to -PI..PI
        while (deltaAz > Math.PI) deltaAz -= (2 * Math.PI).toFloat()
        while (deltaAz < -Math.PI) deltaAz += (2 * Math.PI).toFloat()

        val deltaPitch = targetPitch - pitch.value

        // Field of View constraints (approx 60 degrees)
        val fov = Math.toRadians(60.0).toFloat()

        if (Math.abs(deltaAz) < fov && Math.abs(deltaPitch) < fov) {
            // Project delta angles to screen pixels
            val px = (viewportWidth / 2f) + (deltaAz / fov) * (viewportWidth / 2f)
            val py = (viewportHeight / 2f) + (deltaPitch / fov) * (viewportHeight / 2f)
            return Offset(px, py)
        }

        return null
    }

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // Update observable states with low-pass filtering for smoothness
            val alpha = 0.1f
            azimuth.value = azimuth.value * (1 - alpha) + orientationAngles[0] * alpha
            pitch.value = pitch.value * (1 - alpha) + orientationAngles[1] * alpha
            roll.value = roll.value * (1 - alpha) + orientationAngles[2] * alpha
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        const val VERSION = "1.0.0-GHOST"
    }
}
