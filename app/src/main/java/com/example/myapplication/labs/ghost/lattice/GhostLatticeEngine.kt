package com.example.myapplication.labs.ghost.lattice

import androidx.compose.ui.graphics.Color
import com.example.myapplication.data.BehaviorEvent
import kotlin.math.sqrt
import kotlin.random.Random

class GhostLatticeEngine {
    data class Edge(
        val fromId: Long,
        val toId: Long,
        val strength: Float,
        val type: ConnectionType,
        val color: Color
    )
    enum class ConnectionType(val value: Float) { COLLABORATION(0.0f), FRICTION(1.0f), NEUTRAL(2.0f) }
    data class LatticeNode(val id: Long, val x: Float, val y: Float)

    fun computeLattice(nodes: List<LatticeNode>, events: List<BehaviorEvent>): List<Edge> {
        val edges = mutableListOf<Edge>()
        if (nodes.size < 2) return edges
        val random = Random(42)
        nodes.forEachIndexed { i, nodeA ->
            nodes.forEachIndexed { j, nodeB ->
                if (i < j) {
                    val dist = calculateDistance(nodeA, nodeB)
                    if (dist < 800) {
                        val isPositive = random.nextFloat() > 0.7
                        val isNegative = !isPositive && random.nextFloat() > 0.8
                        val type = when { isPositive -> ConnectionType.COLLABORATION; isNegative -> ConnectionType.FRICTION; else -> ConnectionType.NEUTRAL }
                        val color = when (type) { ConnectionType.COLLABORATION -> Color(0xFF00FFCC); ConnectionType.FRICTION -> Color(0xFFFF3366); else -> Color(0xFF6699FF) }
                        val strength = (1.0f - (dist / 1200f)).coerceIn(0.1f, 1.0f)
                        edges.add(Edge(nodeA.id, nodeB.id, strength, type, color))
                    }
                }
            }
        }
        return edges.take(50)
    }
    private fun calculateDistance(a: LatticeNode, b: LatticeNode): Float {
        val dx = (a.x - b.x); val dy = (a.y - b.y)
        return sqrt(dx * dx + dy * dy)
    }
}
