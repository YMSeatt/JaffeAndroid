import com.example.myapplication.labs.ghost.catalyst.GhostCatalystEngine
import com.example.myapplication.data.BehaviorEvent

fun main() {
    println("Testing GhostCatalystEngine optimization...")

    val currentTime = System.currentTimeMillis()
    val student1 = GhostCatalystEngine.StudentPos(1, 100f, 100f)
    val student2 = GhostCatalystEngine.StudentPos(2, 150f, 150f)
    val students = listOf(student1, student2)

    // Test case 1: Basic reaction detection
    val events = listOf(
        BehaviorEvent(studentId = 1, type = "Positive", timestamp = currentTime - 5000L, comment = null),
        BehaviorEvent(studentId = 2, type = "Positive", timestamp = currentTime - 4000L, comment = null)
    )
    val reactions = GhostCatalystEngine.calculateReactions(students, events)
    println("Identified ${reactions.size} reactions")
    if (reactions.size == 1) {
        val r = reactions[0]
        println("Reaction: Catalyst ${r.catalystId} -> Reactant ${r.reactantId}, intensity: ${r.intensity}")
        if (r.catalystId == 1L && r.reactantId == 2L && r.intensity > 0.9f) {
            println("Test case 1 passed: Basic reaction detected.")
        } else {
            throw IllegalStateException("Wrong reaction data: $r")
        }
    } else {
        throw IllegalStateException("Expected 1 reaction, got ${reactions.size}")
    }

    // Test case 2: Temporal pruning
    val distantEvents = listOf(
        BehaviorEvent(studentId = 1, type = "Positive", timestamp = currentTime - 400_000L, comment = null),
        BehaviorEvent(studentId = 2, type = "Positive", timestamp = currentTime, comment = null)
    )
    val reactions2 = GhostCatalystEngine.calculateReactions(students, distantEvents)
    println("Identified ${reactions2.size} reactions with distant events")
    if (reactions2.isEmpty()) {
        println("Test case 2 passed: Distant events ignored.")
    } else {
        throw IllegalStateException("Expected 0 reactions, got ${reactions2.size}")
    }

    // Test case 3: Catalyst outside analysis window
    val oldCatalystEvents = listOf(
        BehaviorEvent(studentId = 1, type = "Positive", timestamp = currentTime - 2_000_000L, comment = null),
        BehaviorEvent(studentId = 2, type = "Positive", timestamp = currentTime - 1_995_000L, comment = null)
    )
    val reactions3 = GhostCatalystEngine.calculateReactions(students, oldCatalystEvents)
    println("Identified ${reactions3.size} reactions with old catalysts")
    if (reactions3.isEmpty()) {
        println("Test case 3 passed: Old catalysts ignored.")
    } else {
        throw IllegalStateException("Expected 0 reactions, got ${reactions3.size}")
    }

    println("All manual verification cases passed!")
}
