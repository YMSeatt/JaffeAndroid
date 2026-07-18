package com.example.myapplication.labs.ghost

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * GhostNebulaEngine: Upgraded sensor-fusion & atmospheric analysis engine for "Ghost Nebula 3D".
 *
 * It tracks gravity/accelerometer sensors to compute real-time smoothed device tilt (pitch and roll),
 * and uses the ambient light sensor to adapt the volumetric cloud depth factor. It also projects
 * student behavior logs and logical layout coordinates into a unified 3D virtual coordinate space.
 */
class GhostNebulaEngine(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _tiltX = MutableStateFlow(0f)
    private val _tiltY = MutableStateFlow(0f)
    private val _depthFactor = MutableStateFlow(1.0f)

    /** Real-time smoothed device tilt values (pitch & roll) for volumetric 3D camera controls. */
    val tiltX = _tiltX.asStateFlow()
    val tiltY = _tiltY.asStateFlow()

    /** Real-time context-aware depth factor driven by ambient lighting. */
    val depthFactor = _depthFactor.asStateFlow()

    /**
     * Registers sensor event listeners for gravity, accelerometer, and light sensors.
     */
    fun start() {
        val manager = sensorManager ?: return
        if (gravitySensor != null) {
            manager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelSensor?.let {
                manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
        lightSensor?.let {
            manager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * Unregisters all active sensor listeners to preserve battery.
     */
    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_GRAVITY -> {
                val x = event.values[0]
                val y = event.values[1]
                // Low-pass filter to eliminate high-frequency hand tremors (92% historical, 8% new)
                _tiltX.value = _tiltX.value * 0.92f - (x / 9.81f) * 0.08f
                _tiltY.value = _tiltY.value * 0.92f + (y / 9.81f) * 0.08f
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // Fallback tilt calculation if hardware gravity sensor is unavailable
                if (gravitySensor == null) {
                    val x = event.values[0]
                    val y = event.values[1]
                    _tiltX.value = _tiltX.value * 0.92f - (x / 9.81f) * 0.08f
                    _tiltY.value = _tiltY.value * 0.92f + (y / 9.81f) * 0.08f
                }
            }
            Sensor.TYPE_LIGHT -> {
                // Map light levels (Lux) to volumetric fog density depth factor
                // High lux (bright light) thins the fog; low lux (dark environments) deepens the clouds
                val lux = event.values[0]
                val targetDepth = (1.5f - (lux / 500f)).coerceIn(0.3f, 1.6f)
                _depthFactor.value = _depthFactor.value * 0.94f + targetDepth * 0.06f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Represents an activity cluster projected into the nebula.
     */
    data class NebulaCluster(
        val x: Float,
        val y: Float,
        val density: Float,
        val colorIndex: Float // 0: Positive, 1: Negative, 2: Neutral
    )

    /**
     * Analyzes the classroom state to generate nebula parameters.
     */
    fun calculateNebula(
        students: List<StudentUiItem>,
        behaviorLogs: List<BehaviorEvent>,
        timeWindowMs: Long = 15 * 60 * 1000L // 15 minutes window
    ): Pair<Float, List<NebulaCluster>> {
        if (students.isEmpty()) return 0.2f to emptyList()

        val currentTime = System.currentTimeMillis()
        val recentLogs = behaviorLogs.filter { it.timestamp > currentTime - timeWindowMs }

        if (recentLogs.isEmpty()) return 0.3f to emptyList()

        val studentMap = students.associateBy { it.id.toLong() }
        val clusters = mutableListOf<NebulaCluster>()

        // Group logs by student to identify hotspots
        val logsByStudent = recentLogs.groupBy { it.studentId }

        val sortedStudents = logsByStudent.entries.sortedByDescending { it.value.size }
            .take(10) // Render up to 10 high-energy volumetric clusters

        sortedStudents.forEach { (studentId, logs) ->
            val student = studentMap[studentId] ?: return@forEach

            val positiveCount = logs.count { it.type.contains("Positive", ignoreCase = true) }
            val negativeCount = logs.count { it.type.contains("Negative", ignoreCase = true) }

            val colorIndex = when {
                positiveCount > negativeCount -> 0f
                negativeCount > positiveCount -> 1f
                else -> 2f
            }

            val density = (logs.size.toFloat() / 5.0f).coerceIn(0.1f, 1.0f)

            clusters.add(
                NebulaCluster(
                    x = student.xPosition.value,
                    y = student.yPosition.value,
                    density = density,
                    colorIndex = colorIndex
                )
            )
        }

        val globalIntensity = (recentLogs.size.toFloat() / 20f + 0.3f).coerceIn(0.3f, 1.0f)

        return globalIntensity to clusters
    }
}
