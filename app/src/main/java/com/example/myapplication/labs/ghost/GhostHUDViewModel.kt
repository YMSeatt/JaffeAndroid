package com.example.myapplication.labs.ghost

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.model.StudentUiItem
import com.example.myapplication.labs.ghost.GhostOracle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.atan2

/**
 * GhostHUDViewModel: Manages sensors and coordinate mapping for the Tactical HUD.
 */
class GhostHUDViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading.asStateFlow()

    private val _targetAngles = MutableStateFlow<List<Float>>(emptyList())
    val targetAngles: StateFlow<List<Float>> = _targetAngles.asStateFlow()

    private val _targetScores = MutableStateFlow<List<Float>>(emptyList())
    val targetScores: StateFlow<List<Float>> = _targetScores.asStateFlow()

    private var isListening = false

    fun startTracking() {
        if (!isListening && rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
            isListening = true
        }
    }

    fun stopTracking() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTracking()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)
            _heading.value = orientation[0]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun updateTargets(students: List<StudentUiItem>, prophecies: List<GhostOracle.Prophecy>) {
        viewModelScope.launch {
            val frictionStudents = prophecies.filter {
                it.type == GhostOracle.ProphecyType.SOCIAL_FRICTION ||
                it.type == GhostOracle.ProphecyType.ENGAGEMENT_DROP
            }.map { it.studentId }.toSet()

            val angles = mutableListOf<Float>()
            val scores = mutableListOf<Float>()

            students.filter { frictionStudents.contains(it.id.toLong()) }.take(10).forEach { student ->
                val dx = student.xPosition.value - 2000f
                val dy = student.yPosition.value - 4000f
                val angle = atan2(dy, dx)
                angles.add(angle)

                val score = prophecies.filter { it.studentId == student.id.toLong() }
                    .maxOfOrNull { it.confidence } ?: 0.5f
                scores.add(score)
            }

            _targetAngles.value = angles
            _targetScores.value = scores
        }
    }
}
