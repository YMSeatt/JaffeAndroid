import com.example.myapplication.labs.ghost.entanglement.GhostEntanglementEngine
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.HomeworkLog
import kotlin.math.abs

fun main() {
    println("⚡ Bolt: Starting manual verification for GhostEntanglementEngine...")

    testCoherenceOptimization()
    testNodeMetricsOptimization()
    testIdentifyEntangledLinks()

    println("✅ All manual verification cases passed!")
}

fun testCoherenceOptimization() {
    println("Testing calculateCoherence with new EntangledNode and squared distance logic...")

    val nodeA = GhostEntanglementEngine.EntangledNode(
        id = 1L, x = 100f, y = 100f, behaviorSync = 0.8f, academicParity = 0.9f, groupId = 10L
    )
    val nodeB = GhostEntanglementEngine.EntangledNode(
        id = 2L, x = 110f, y = 110f, behaviorSync = 0.7f, academicParity = 0.8f, groupId = 10L
    )
    val nodeC = GhostEntanglementEngine.EntangledNode(
        id = 3L, x = 110f, y = 110f, behaviorSync = 0.7f, academicParity = 0.8f, groupId = 20L
    )

    val coherenceAB = GhostEntanglementEngine.calculateCoherence(nodeA, nodeB)
    val coherenceAC = GhostEntanglementEngine.calculateCoherence(nodeA, nodeC)

    println("Coherence (Same Group): $coherenceAB")
    println("Coherence (Diff Group): $coherenceAC")

    // Same group should have 1.5x boost (capped at 1.0)
    if (coherenceAB <= coherenceAC) {
        throw IllegalStateException("Group boost logic failed: $coherenceAB should be > $coherenceAC")
    }

    if (coherenceAB > 1.0f) {
        throw IllegalStateException("Coherence capped incorrectly: $coherenceAB")
    }

    println("Passed: Coherence optimization and group logic verified.")
}

fun testNodeMetricsOptimization() {
    println("Testing calculateNodeMetrics with allocation-free variance logic...")

    val now = System.currentTimeMillis()
    val bLogs = listOf(
        BehaviorEvent(id = 1, studentId = 1, type = "Positive", timestamp = now),
        BehaviorEvent(id = 2, studentId = 1, type = "Positive", timestamp = now + 60000),
        BehaviorEvent(id = 3, studentId = 1, type = "Positive", timestamp = now + 120000)
    )

    val qLogs = listOf(
        QuizLog(id = 1, studentId = 1, quizName = "Q1", markValue = 10.0, maxMarkValue = 10.0, loggedAt = now)
    )

    val hLogs = listOf(
        HomeworkLog(id = 1, studentId = 1, assignmentName = "H1", status = "Done", loggedAt = now)
    )

    val (bSync, aParity) = GhostEntanglementEngine.calculateNodeMetrics(bLogs, qLogs, hLogs)

    println("Behavior Sync (Constant intervals): $bSync")
    println("Academic Parity: $aParity")

    // With constant intervals, bSync should be very high (approaching 1.0)
    if (bSync < 0.9f) {
        throw IllegalStateException("Sync calculation seems off for constant intervals: $bSync")
    }

    if (abs(aParity - 1.0f) > 0.01f) {
        throw IllegalStateException("Academic parity calculation failed: $aParity")
    }

    println("Passed: Node metrics calculation verified.")
}

fun testIdentifyEntangledLinks() {
    println("Testing identifyEntangledLinks with updated signature...")

    val nodes = listOf(
        GhostEntanglementEngine.EntangledNode(1L, 100f, 100f, 0.9f, 0.9f, groupId = 1L),
        GhostEntanglementEngine.EntangledNode(2L, 110f, 110f, 0.8f, 0.8f, groupId = 1L),
        GhostEntanglementEngine.EntangledNode(3L, 3000f, 3000f, 0.1f, 0.1f, groupId = 2L)
    )

    val links = GhostEntanglementEngine.identifyEntangledLinks(nodes, limit = 5)

    println("Found ${links.size} entangled links")
    if (links.size != 1) {
        throw IllegalStateException("Expected 1 link, found ${links.size}")
    }

    val link = links[0]
    if (link.studentA != 1L || link.studentB != 2L) {
        throw IllegalStateException("Wrong students entangled: ${link.studentA} & ${link.studentB}")
    }

    println("Passed: Entangled links identification verified.")
}
