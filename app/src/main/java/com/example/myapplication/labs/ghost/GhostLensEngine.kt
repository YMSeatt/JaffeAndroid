package com.example.myapplication.labs.ghost

import androidx.compose.ui.geometry.Offset
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * GhostLensEngine: Manages the spatiotemporal state of the Ghost Lens.
 *
 * It tracks the lens position and identifies students that fall within its
 * "Predictive Field". It bridges to [GhostOracle] to fetch future prophecies.
 */
class GhostLensEngine {
    private val _lensPosition = MutableStateFlow(Offset(500f, 500f))
    val lensPosition = _lensPosition.asStateFlow()

    private val _lensRadius = MutableStateFlow(250f)
    val lensRadius = _lensRadius.asStateFlow()

    fun updatePosition(newPos: Offset) {
        _lensPosition.value = newPos
    }

    /**
     * Identifies students currently under the lens and maps them to prophecies.
     */
    fun getPropheciesForStudentsUnderLens(
        students: List<StudentUiItem>,
        allProphecies: List<GhostOracle.Prophecy>,
        canvasScale: Float,
        canvasOffset: Offset
    ): List<GhostOracle.Prophecy> {
        val lensPos = _lensPosition.value
        val radius = _lensRadius.value

        return students.filter { student ->
            // Convert student logical coordinates to screen coordinates
            val screenX = student.xPosition.value * canvasScale + canvasOffset.x
            val screenY = student.yPosition.value * canvasScale + canvasOffset.y

            val dx = screenX - lensPos.x
            val dy = screenY - lensPos.y
            val dist = sqrt(dx * dx + dy * dy)

            dist < radius
        }.flatMap { student ->
            allProphecies.filter { it.studentId == student.id.toLong() }
        }.distinctBy { it.description }
    }
}
