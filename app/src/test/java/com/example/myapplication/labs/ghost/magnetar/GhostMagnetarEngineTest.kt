package com.example.myapplication.labs.ghost.magnetar

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.ui.model.StudentUiItem
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GhostMagnetarEngineTest {

    private lateinit var engine: GhostMagnetarEngine
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        engine = GhostMagnetarEngine(context)
    }

    @Test
    fun testCalculateDipoles() {
        val students = listOf(
            createMockStudent(1, "Alpha", 1000f, 1000f),
            createMockStudent(2, "Bravo", 3000f, 1000f)
        )
        val logs = listOf(
            BehaviorEvent(1, 1, "Positive Participation", 1000L),
            BehaviorEvent(2, 2, "Negative Disruption", 2000L)
        )

        val dipoles = engine.calculateDipoles(students, logs)

        assertEquals(2, dipoles.size)

        val alphaDipole = dipoles.find { it.studentId == 1L }!!
        assertTrue(alphaDipole.strength > 0) // North

        val bravoDipole = dipoles.find { it.studentId == 2L }!!
        assertTrue(bravoDipole.strength < 0) // South
    }

    @Test
    fun testAnalyzeMagneticField() {
        val dipoles = listOf(
            GhostMagnetarEngine.MagneticDipole(1, 1000f, 1000f, 5f, 100f), // North near Top-Left
            GhostMagnetarEngine.MagneticDipole(2, 3000f, 3000f, -5f, 100f) // South near Bottom-Right
        )

        val analysis = engine.analyzeMagneticField(dipoles)

        assertEquals(4, analysis.quadrantIntensities.size)

        val topLeft = analysis.quadrantIntensities.find { it.quadrantName == "Top-Left" }!!
        val bottomRight = analysis.quadrantIntensities.find { it.quadrantName == "Bottom-Right" }!!
        val topRight = analysis.quadrantIntensities.find { it.quadrantName == "Top-Right" }!!

        // Top-Left should have higher intensity than Top-Right due to proximity to dipole 1
        assertTrue(topLeft.intensity > topRight.intensity)
        // Bottom-Right should have high intensity due to proximity to dipole 2
        assertTrue(bottomRight.intensity > 0)
    }

    @Test
    fun testGenerateMagnetarReport() {
        val dipoles = listOf(
            GhostMagnetarEngine.MagneticDipole(1, 1000f, 1000f, 2f, 100f)
        )
        val studentNames = mapOf(1L to "Alpha")
        val analysis = engine.analyzeMagneticField(dipoles)

        val report = engine.generateMagnetarReport(analysis, studentNames)

        assertTrue(report.contains("# 👻 GHOST MAGNETAR: SOCIAL MAGNETIC ANALYSIS"))
        assertTrue(report.contains("Alpha"))
        assertTrue(report.contains("NORTH (+)"))
        assertTrue(report.contains("Top-Left"))
    }

    private fun createMockStudent(id: Long, name: String, x: Float, y: Float): StudentUiItem {
        return StudentUiItem(
            id = id.toInt(),
            fullName = androidx.compose.runtime.mutableStateOf(name),
            initials = androidx.compose.runtime.mutableStateOf(name.take(1)),
            xPosition = androidx.compose.runtime.mutableStateOf(x),
            yPosition = androidx.compose.runtime.mutableStateOf(y)
        )
    }
}
