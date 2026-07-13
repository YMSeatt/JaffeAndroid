package com.example.myapplication.labs.ghost.supernova

import org.junit.Assert.assertEquals
import org.junit.Test

class GhostSupernovaEngineTest {

    @Test
    fun testCriticalityCalculation() {
        // High friction scenario
        val criticality = GhostSupernovaEngine.calculateCriticality(
            studentCount = 20,
            logCount = 5,
            negativeRatio = 0.8f
        )
        // (5 / (20 * 5)) * (1 + 0.8 * 2)
        // (5 / 100) * (1 + 1.6)
        // 0.05 * 2.6 = 0.13
        assertEquals(0.13f, criticality, 0.001f)
    }

    @Test
    fun testStageTransitions() {
        val engine = GhostSupernovaEngine()
        assertEquals(GhostSupernovaEngine.SupernovaStage.IDLE, engine.stage.value)

        engine.triggerSupernova()
        assertEquals(GhostSupernovaEngine.SupernovaStage.CONTRACTION, engine.stage.value)

        engine.nextStage()
        assertEquals(GhostSupernovaEngine.SupernovaStage.EXPLOSION, engine.stage.value)

        engine.nextStage()
        assertEquals(GhostSupernovaEngine.SupernovaStage.NEBULA, engine.stage.value)

        engine.nextStage()
        assertEquals(GhostSupernovaEngine.SupernovaStage.IDLE, engine.stage.value)
    }
}
