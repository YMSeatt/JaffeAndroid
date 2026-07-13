package com.example.myapplication.labs.ghost

import androidx.compose.ui.geometry.Offset
import com.example.myapplication.ui.model.StudentUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
     *
     * BOLT: Optimized to eliminate functional operator overhead and redundant math.
     * Uses squared distance comparison and single-pass manual loops to avoid O(N) list churn.
     *
     * @param students Current student UI items.
     * @param propheciesByStudent Pre-grouped prophecies for O(1) lookup.
     * @param lensPos Current screen coordinates of the lens.
     * @param lensRadius Radius of the predictive field.
     * @param canvasScale Current chart zoom level.
     * @param canvasOffset Current chart pan offset.
     */
    fun getPropheciesForStudentsUnderLens(
        students: List<StudentUiItem>,
        propheciesByStudent: Map<Long, List<GhostOracle.Prophecy>>,
        lensPos: Offset,
        lensRadius: Float,
        canvasScale: Float,
        canvasOffset: Offset
    ): List<GhostOracle.Prophecy> {
        val radiusSq = lensRadius * lensRadius
        val result = mutableListOf<GhostOracle.Prophecy>()
        val seenDescriptions = mutableSetOf<String>()

        // BOLT: Use manual index-based loop to avoid iterator allocation.
        for (i in students.indices) {
            val student = students[i]

            // Convert student logical coordinates to screen coordinates
            val screenX = student.xPosition.value * canvasScale + canvasOffset.x
            val screenY = student.yPosition.value * canvasScale + canvasOffset.y

            val dx = screenX - lensPos.x
            val dy = screenY - lensPos.y
            val distSq = dx * dx + dy * dy

            // BOLT: Use squared distance to avoid expensive sqrt() call.
            if (distSq < radiusSq) {
                val studentProphecies = propheciesByStudent[student.id.toLong()]
                if (studentProphecies != null) {
                    for (j in studentProphecies.indices) {
                        val prophecy = studentProphecies[j]
                        // BOLT: Inline deduplication using a set to avoid .distinctBy() overhead.
                        if (seenDescriptions.add(prophecy.description)) {
                            result.add(prophecy)
                        }
                    }
                }
            }
        }
        return result
    }
}
