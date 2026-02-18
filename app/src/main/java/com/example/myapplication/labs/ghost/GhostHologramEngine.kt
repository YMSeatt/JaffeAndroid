package com.example.myapplication.labs.ghost

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * GhostHologramEngine: Manages sensor data for the 3D Holographic effect.
 *
 * It listens to the device's rotation vector and converts it into pitch and roll
 * values suitable for driving Compose graphics transformations and AGSL uniforms.
 */
class GhostHologramEngine(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _tilt = MutableStateFlow(TiltState())
    val tilt: StateFlow<TiltState> = _tilt.asStateFlow()

    data class TiltState(
        val pitch: Float = 0f, // X-axis rotation (degrees)
        val roll: Float = 0f,  // Y-axis rotation (degrees)
        val intensity: Float = 0f,
        val flicker: Float = 1f
    )

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    // Pre-allocate to avoid GC pressure in onSensorChanged
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // orientation[1] is pitch, orientation[2] is roll (in radians)
            val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
            val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

            // Normalize intensity based on total tilt
            val intensity = (sqrt(pitch * pitch + roll * roll) / 45f).coerceIn(0f, 1f)

            // Add a slight flicker based on timestamp
            val flicker = 0.95f + (Math.sin(event.timestamp.toDouble() / 100000000.0) * 0.05).toFloat()

            _tilt.value = TiltState(
                pitch = pitch.coerceIn(-30f, 30f),
                roll = roll.coerceIn(-30f, 30f),
                intensity = intensity,
                flicker = flicker
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
