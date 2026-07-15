package com.example.myapplication.labs.ghost.meteor

import com.example.myapplication.ui.model.StudentUiItem
import java.util.ArrayDeque
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * GhostMeteorEngine: High-momentum neural projectile physics and impact logic.
 *
 * This engine manages "Ghost Meteors" that represent high-impact classroom events.
 * Meteors are triggered by academic breakthroughs or significant behavioral shifts.
 *
 * BOLT Performance:
 * - Object pooling for Meteors and Impacts.
 * - Single-pass O(N) updates.
 * - Primitive array buffers for trail data.
 */
class GhostMeteorEngine {
    companion object {
        const val MAX_TRAIL_POINTS = 8
        const val MAX_METEORS = 15
        const val MAX_IMPACTS = 10
    }

    class Meteor(
        var x: Float = 0f,
        var y: Float = 0f,
        var vx: Float = 0f,
        var vy: Float = 0f,
        var life: Float = 0f,
        var colorType: Int = 0, // 0: Academic (Purple), 1: Positive (Cyan), 2: Negative (Magenta)
        val trailX: FloatArray = FloatArray(MAX_TRAIL_POINTS),
        val trailY: FloatArray = FloatArray(MAX_TRAIL_POINTS),
        var trailIndex: Int = 0,
        var targetStudentId: Long = -1L
    ) {
        fun reset(x: Float, y: Float, vx: Float, vy: Float, colorType: Int, targetId: Long = -1L) {
            this.x = x
            this.y = y
            this.vx = vx
            this.vy = vy
            this.life = 1.0f
            this.colorType = colorType
            this.targetStudentId = targetId
            this.trailIndex = 0
            for (i in 0 until MAX_TRAIL_POINTS) {
                trailX[i] = x
                trailY[i] = y
            }
        }

        fun updateTrail() {
            trailX[trailIndex] = x
            trailY[trailIndex] = y
            trailIndex = (trailIndex + 1) % MAX_TRAIL_POINTS
        }
    }

    class Impact(
        var x: Float = 0f,
        var y: Float = 0f,
        var radius: Float = 0f,
        var life: Float = 0f,
        var colorType: Int = 0
    ) {
        fun reset(x: Float, y: Float, colorType: Int) {
            this.x = x
            this.y = y
            this.radius = 0f
            this.life = 1.0f
            this.colorType = colorType
        }
    }

    private val _meteors = ArrayList<Meteor>(MAX_METEORS)
    val meteors: List<Meteor> get() = _meteors

    private val _impacts = ArrayList<Impact>(MAX_IMPACTS)
    val impacts: List<Impact> get() = _impacts

    private val meteorPool = ArrayDeque<Meteor>(MAX_METEORS)
    private val impactPool = ArrayDeque<Impact>(MAX_IMPACTS)

    /**
     * Emits a meteor targeting a specific student or random position.
     */
    fun emit(startX: Float, startY: Float, targetX: Float, targetY: Float, type: Int, targetId: Long = -1L) {
        if (_meteors.size >= MAX_METEORS) return

        val dx = targetX - startX
        val dy = targetY - startY
        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
        if (dist == 0f) return

        val speed = 30f + Random.nextFloat() * 20f
        val vx = (dx / dist) * speed
        val vy = (dy / dist) * speed

        val meteor = meteorPool.poll() ?: Meteor()
        meteor.reset(startX, startY, vx, vy, type, targetId)
        _meteors.add(meteor)
    }

    /**
     * Updates physics, trails, and impact states.
     */
    fun update(students: List<StudentUiItem>, deltaTime: Float = 1.0f) {
        // Update Meteors
        val mIterator = _meteors.iterator()
        while (mIterator.hasNext()) {
            val meteor = mIterator.next()
            meteor.updateTrail()

            meteor.x += meteor.vx * deltaTime
            meteor.y += meteor.vy * deltaTime

            // Check for "collision" with target student or screen boundaries
            var hit = false
            if (meteor.targetStudentId != -1L) {
                val target = students.find { it.id.toLong() == meteor.targetStudentId }
                if (target != null) {
                    val dx = target.xPosition.value - meteor.x
                    val dy = target.yPosition.value - meteor.y
                    if (dx * dx + dy * dy < 1600f) { // 40px radius
                        hit = true
                    }
                }
            }

            // Life decay if no target or missed
            meteor.life -= 0.005f * deltaTime
            if (meteor.life <= 0f) hit = true

            if (hit) {
                createImpact(meteor.x, meteor.y, meteor.colorType)
                mIterator.remove()
                meteorPool.offer(meteor)
            }
        }

        // Update Impacts
        val iIterator = _impacts.iterator()
        while (iIterator.hasNext()) {
            val impact = iIterator.next()
            impact.radius += 5f * deltaTime
            impact.life -= 0.02f * deltaTime
            if (impact.life <= 0f) {
                iIterator.remove()
                impactPool.offer(impact)
            }
        }
    }

    private fun createImpact(x: Float, y: Float, type: Int) {
        if (_impacts.size >= MAX_IMPACTS) return
        val impact = impactPool.poll() ?: Impact()
        impact.reset(x, y, type)
        _impacts.add(impact)
    }

    fun reset() {
        meteorPool.addAll(_meteors)
        _meteors.clear()
        impactPool.addAll(_impacts)
        _impacts.clear()
    }
}
