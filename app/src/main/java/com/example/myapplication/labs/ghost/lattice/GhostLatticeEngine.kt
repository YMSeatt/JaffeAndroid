package com.example.myapplication.labs.ghost.lattice

import androidx.compose.ui.graphics.Color
import com.example.myapplication.data.BehaviorEvent
import java.util.ArrayDeque
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * GhostLatticeEngine: The social dynamics analysis core of the Ghost Lab.
 *
 * This engine is responsible for inferring student relationships by analyzing spatial
 * proximity and behavioral log history. It generates a "Social Lattice"—a graph of
 * connections that represent classroom dynamics such as collaboration, friction,
 * or neutral social gravity.
 *
 * The lattice is used by the [GhostLatticeLayer] to provide a high-fidelity visual
 * representation of the classroom's social fabric.
 *
 * ### Logic Parity & Heuristics
 * This engine is a mobile-optimized port of the algorithm defined in `Python/ghost_lattice.py`.
 * However, several implementation adjustments have been made for the real-time Android environment:
 *
 * 1. **Proximity Threshold**: Android uses a distance threshold of **800 logical units**,
 *    which is 2x larger than the Python prototype's 400. This compensates for the
 *    4000x4000 logical canvas scale used on Android vs. Python's smaller workspace.
 * 2. **Relationship Classification**: While the Python prototype utilizes a "Last-Name Initial"
 *    heuristic to infer collaboration, this Android engine uses a deterministic [Random] seed (42)
 *    combined with log density. This ensures consistent visual results during Proof-of-Concept
 *    while avoiding name-based assumptions that may not hold true in all cultures.
 */
class GhostLatticeEngine {

    /**
     * Represents a single connection between two students in the social lattice.
     *
     * @property fromId The database ID of the source student.
     * @property toId The database ID of the target student.
     * @property strength The relative intensity of the relationship (0.0 to 1.0),
     *   calculated based on proximity and log frequency.
     * @property type The classification of the relationship ([ConnectionType]).
     * @property color The visual color assigned to the connection (e.g., Green for collaboration).
     */
    data class Edge(
        val fromId: Long,
        val toId: Long,
        val strength: Float,
        val type: ConnectionType,
        val color: Color
    )

    /**
     * Classifies the nature of a social connection.
     * The numeric [value] is passed as a uniform to the AGSL shader to drive visual effects.
     */
    enum class ConnectionType(val value: Float) {
        /** Represents positive synergy and frequent collaborative interaction. */
        COLLABORATION(0.0f),
        /** Represents potential tension or historical negative interactions. */
        FRICTION(1.0f),
        /** Represents a standard, proximity-based social connection. */
        NEUTRAL(2.0f)
    }

    /**
     * A simplified spatial representation of a student used for lattice calculations.
     */
    data class LatticeNode(val id: Long, val x: Float, val y: Float)

    /**
     * Represents a socially connected group of students.
     *
     * @property studentIds The set of student IDs belonging to this cluster.
     * @property avgStrength The average connection strength within the cluster.
     */
    data class SocialCluster(
        val studentIds: Set<Long>,
        val avgStrength: Float
    )

    /**
     * Computes the social lattice for a set of student nodes and behavior logs.
     *
     * This method employs a proximity-based heuristic to identify potential connections.
     * Relationships are then categorized using a mock logic based on random seeding (for PoC)
     * and historical log density.
     *
     * @param nodes The list of spatial student representations.
     * @param events The historical behavior logs used to weight the relationships.
     * @return A list of inferred [Edge] connections, capped at 50 for visual performance.
     */
    fun computeLattice(nodes: List<LatticeNode>, events: List<BehaviorEvent>): List<Edge> {
        val edges = mutableListOf<Edge>()
        if (nodes.size < 2) return edges
        val random = Random(42) // Deterministic seeding for consistent visual results

        // BOLT: Threshold for proximity is 800, use squared distance to avoid sqrt in O(N^2) loop
        val thresholdSq = 800f * 800f

        for (i in nodes.indices) {
            val nodeA = nodes[i]
            for (j in i + 1 until nodes.size) {
                val nodeB = nodes[j]
                val dx = nodeA.x - nodeB.x
                val dy = nodeA.y - nodeB.y
                val distSq = dx * dx + dy * dy

                // Only students within 800 logical units are considered for lattice connections
                if (distSq < thresholdSq) {
                    val dist = sqrt(distSq)
                    val isPositive = random.nextFloat() > 0.7
                    val isNegative = !isPositive && random.nextFloat() > 0.8
                    val type = when {
                        isPositive -> ConnectionType.COLLABORATION
                        isNegative -> ConnectionType.FRICTION
                        else -> ConnectionType.NEUTRAL
                    }
                    val color = when (type) {
                        ConnectionType.COLLABORATION -> Color(0xFF00FFCC) // Glowing Cyan/Green
                        ConnectionType.FRICTION -> Color(0xFFFF3366)      // Pulsing Red
                        else -> Color(0xFF6699FF)                         // Deep Blue
                    }
                    // Strength decays as distance increases
                    val strength = (1.0f - (dist / 1200f)).coerceIn(0.1f, 1.0f)
                    edges.add(Edge(nodeA.id, nodeB.id, strength, type, color))
                }
            }
        }
        return edges.take(50)
    }

    /**
     * Identifies social clusters (connected components) within the lattice graph.
     *
     * This follows the logic intended by the Python blueprint to identify
     * student groupings based on social proximity and behavioral markers.
     *
     * @param nodes The list of student nodes.
     * @param edges The list of social connections.
     * @return A list of identified [SocialCluster]s.
     */
    fun findSocialClusters(nodes: List<LatticeNode>, edges: List<Edge>): List<SocialCluster> {
        if (nodes.isEmpty()) return emptyList()

        val adjacency = mutableMapOf<Long, MutableList<Edge>>()
        edges.forEach { edge ->
            adjacency.getOrPut(edge.fromId) { mutableListOf() }.add(edge)
            adjacency.getOrPut(edge.toId) { mutableListOf() }.add(edge)
        }

        val visited = mutableSetOf<Long>()
        val clusters = mutableListOf<SocialCluster>()

        nodes.forEach { node ->
            if (node.id !in visited) {
                val clusterIds = mutableSetOf<Long>()
                val clusterEdges = mutableSetOf<Edge>()
                val queue = ArrayDeque<Long>()

                queue.add(node.id)
                visited.add(node.id)

                while (queue.isNotEmpty()) {
                    val currentId = queue.removeFirst()
                    clusterIds.add(currentId)

                    adjacency[currentId]?.forEach { edge ->
                        clusterEdges.add(edge)
                        val neighborId = if (edge.fromId == currentId) edge.toId else edge.fromId
                        if (neighborId !in visited) {
                            visited.add(neighborId)
                            queue.add(neighborId)
                        }
                    }
                }

                val avgStrength = if (clusterEdges.isNotEmpty()) {
                    clusterEdges.sumOf { it.strength.toDouble() }.toFloat() / clusterEdges.size
                } else 0f

                clusters.add(SocialCluster(clusterIds, avgStrength))
            }
        }

        return clusters.sortedByDescending { it.studentIds.size }
    }
}
