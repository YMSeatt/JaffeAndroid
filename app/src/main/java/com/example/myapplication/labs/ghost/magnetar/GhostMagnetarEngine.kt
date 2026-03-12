package com.example.myapplication.labs.ghost.magnetar

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * GhostMagnetarEngine: Calculates classroom "Social Polarity" and "Magnetic Field Vectors".
 *
 * This engine maps behavioral logs to magnetic dipoles (Positive = North, Negative = South)
 * and integrates the device's physical Magnetometer to skew the field orientation.
 */
class GhostMagnetarEngine(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _magneticHeading = MutableStateFlow(0f)
    val magneticHeading = _magneticHeading.asStateFlow()

    data class MagneticDipole(
        val x: Float,
        val y: Float,
        val strength: Float, // Positive = North (+), Negative = South (-)
        val radius: Float
    )

    fun start() {
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            // Calculate a simple 2D heading from the magnetic field (X and Y components)
            val heading = Math.atan2(event.values[1].toDouble(), event.values[0].toDouble()).toFloat()
            _magneticHeading.value = heading
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Calculates the magnetic dipoles for each student based on their behavioral history.
     */
    fun calculateDipoles(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>
    ): List<MagneticDipole> {
        val groupedLogs = behaviorLogs.groupBy { it.studentId }

        return students.map { student ->
            val sid = student.id.toLong()
            val logs = groupedLogs[sid] ?: emptyList()

            // Calculate polarity based on log counts
            var northWeight = 0f
            var southWeight = 0f

            logs.forEach { log ->
                if (log.type.contains("Positive", ignoreCase = true)) {
                    northWeight += 1.0f
                } else if (log.type.contains("Negative", ignoreCase = true)) {
                    southWeight += 1.5f // Negative behaviors exert stronger "Social Gravity"
                }
            }

            val strength = (northWeight - southWeight).coerceIn(-10f, 10f)
            val radius = (abs(strength) * 50f + 100f).coerceIn(100f, 600f)

            MagneticDipole(
                x = student.xPosition.value,
                y = student.yPosition.value,
                strength = strength,
                radius = radius
            )
        }
    }
}
