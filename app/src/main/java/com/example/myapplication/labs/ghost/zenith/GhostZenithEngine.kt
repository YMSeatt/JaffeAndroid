package com.example.myapplication.labs.ghost.zenith

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GhostZenithEngine: Orchestrates spatial depth and parallax mapping.
 *
 * This engine tracks device orientation to drive a multi-layered 3D seating chart.
 * It also calculates the "Elevation" of each student node based on academic
 * performance and behavioral stability.
 *
 * ### Logic Parity
 * Maintains mathematical alignment with `Python/ghost_zenith_analysis.py` for
 * altitude and parallax vector calculations.
 */
class GhostZenithEngine(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _tiltX = MutableStateFlow(0f)
    private val _tiltY = MutableStateFlow(0f)

    /** Current device pitch (X-axis tilt) in radians. */
    val tiltX = _tiltX.asStateFlow()
    /** Current device roll (Y-axis tilt) in radians. */
    val tiltY = _tiltY.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    /**
     * Starts tracking device orientation.
     */
    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    /**
     * Stops tracking device orientation.
     */
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // orientation[1] is pitch (tilt front/back)
            // orientation[2] is roll (tilt left/right)
            // We apply low-pass filtering to smooth the parallax movement
            _tiltX.value = _tiltX.value * 0.8f + orientation[1] * 0.2f
            _tiltY.value = _tiltY.value * 0.8f + orientation[2] * 0.2f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        /**
         * Calculates the 'Altitude' of a student in the 3D neural space.
         * Students with high academic performance and positive behavior float higher.
         *
         * BOLT: Refactored to accept pre-calculated metrics to eliminate redundant O(L)
         * log traversals in the background update pipeline.
         *
         * @param academicScore Normalized academic score (0.0 to 1.0).
         * @param behaviorScore Normalized behavior stability (0.0 to 1.0).
         * @return Normalized altitude value (0.0 to 1.0).
         */
        fun calculateStudentAltitude(
            academicScore: Float,
            behaviorScore: Float
        ): Float {
            // Weighted average: Academic performance has more "buoyancy" in the 2027 vision
            return (academicScore * 0.7f + behaviorScore * 0.3f).coerceIn(0f, 1f)
        }

        /**
         * Maps a tilt value to a pixel offset for parallax effects.
         *
         * @param tilt Radians of tilt.
         * @param depth Weight factor representing how "far" the object is.
         * @return Pixel offset.
         */
        fun calculateParallaxOffset(tilt: Float, depth: Float): Float {
            // Maximum parallax shift of 100 pixels at depth 1.0
            return tilt * 100f * depth
        }
    }
}
