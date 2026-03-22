import com.example.myapplication.labs.ghost.vortex.GhostVortexEngine
import com.example.myapplication.data.BehaviorEvent
import com.example.myapplication.data.Student

fun main() {
    println("Testing GhostVortexEngine optimization...")

    val currentTime = System.currentTimeMillis()
    val s1 = Student(id = 1, firstName = "A", lastName = "1", xPosition = 100f, yPosition = 100f)
    val s2 = Student(id = 2, firstName = "B", lastName = "2", xPosition = 150f, yPosition = 150f)

    val logs = listOf(
        BehaviorEvent(id=1, studentId=1, type="Negative", timestamp=currentTime, comment=null),
        BehaviorEvent(id=2, studentId=1, type="Negative", timestamp=currentTime - 1000, comment=null),
        BehaviorEvent(id=3, studentId=1, type="Negative", timestamp=currentTime - 2000, comment=null),
        BehaviorEvent(id=4, studentId=1, type="Negative", timestamp=currentTime - 3000, comment=null),
        BehaviorEvent(id=5, studentId=1, type="Negative", timestamp=currentTime - 4000, comment=null),
        BehaviorEvent(id=6, studentId=2, type="Negative", timestamp=currentTime, comment=null),
        BehaviorEvent(id=7, studentId=2, type="Negative", timestamp=currentTime - 1000, comment=null),
        BehaviorEvent(id=8, studentId=2, type="Negative", timestamp=currentTime - 2000, comment=null),
        BehaviorEvent(id=9, studentId=2, type="Negative", timestamp=currentTime - 3000, comment=null),
        BehaviorEvent(id=10, studentId=2, type="Negative", timestamp=currentTime - 4000, comment=null)
    )

    val logsByStudent = logs.groupBy { it.studentId }
    val result = GhostVortexEngine.identifyVortices(listOf(s1, s2), logsByStudent)

    println("Identified ${result.size} vortices")

    if (result.size == 1) {
        val v = result[0]
        println("Vortex at (${v.x}, ${v.y}), momentum: ${v.momentum}, polarity: ${v.polarity}")
        assert(v.x == 100f)
        assert(v.y == 100f)
        assert(v.polarity == -1.0f)
        assert(v.momentum > 0.5f)
        println("Test case 1 passed: High activity cluster identified correctly.")
    } else {
        throw IllegalStateException("Expected 1 vortex, got ${result.size}")
    }

    // Test case 2: Old logs
    val oldTime = currentTime - 700_000L
    val oldLogs = listOf(
        BehaviorEvent(id=11, studentId=1, type="Negative", timestamp=oldTime, comment=null),
        BehaviorEvent(id=12, studentId=2, type="Negative", timestamp=oldTime, comment=null)
    )
    val resultOld = GhostVortexEngine.identifyVortices(listOf(s1, s2), oldLogs.groupBy { it.studentId })
    println("Identified ${resultOld.size} vortices with old logs")
    assert(resultOld.isEmpty())
    println("Test case 2 passed: Old logs ignored.")

    println("All manual verification cases passed!")
}
