
import com.example.myapplication.labs.ghost.entropy.GhostEntropyEngine
import com.example.myapplication.data.BehaviorEvent
import kotlin.math.abs

fun main() {
    println("Testing GhostEntropyEngine optimization...")

    // Test case 1: Empty logs
    val entropy0 = GhostEntropyEngine.calculateBehaviorEntropy(emptyList())
    assert(entropy0 == 0f) { "Expected 0, got $entropy0" }
    println("Test case 1 passed: Empty logs -> 0 entropy")

    // Test case 2: Single type
    val logs1 = listOf(
        BehaviorEvent(id=1, studentId=1, type="A", timestamp=0, comment=null),
        BehaviorEvent(id=2, studentId=1, type="A", timestamp=0, comment=null)
    )
    val entropy1 = GhostEntropyEngine.calculateBehaviorEntropy(logs1)
    assert(entropy1 == 0f) { "Expected 0, got $entropy1" }
    println("Test case 2 passed: Single type -> 0 entropy")

    // Test case 3: Multiple types
    val logs2 = listOf(
        BehaviorEvent(id=1, studentId=1, type="A", timestamp=0, comment=null),
        BehaviorEvent(id=2, studentId=1, type="B", timestamp=0, comment=null)
    )
    val entropy2 = GhostEntropyEngine.calculateBehaviorEntropy(logs2)
    assert(entropy2 > 0f) { "Expected >0, got $entropy2" }
    println("Test case 3 passed: Multiple types -> Positive entropy ($entropy2)")

    println("All manual test cases passed!")
}
