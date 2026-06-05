import math

class Student:
    def __init__(self, id, x, y):
        self.id = id
        self.x = x
        self.y = y

class BehaviorEvent:
    def __init__(self, studentId, type, timestamp):
        self.studentId = studentId
        self.type = type
        self.timestamp = timestamp

def calculate_local_resonance(targetX, targetY, students, behaviorLogsByStudent, timeWindowMs=24 * 60 * 60 * 1000):
    RADAR_RADIUS = 500.0
    now = 1000000000  # Simulated now
    cutoff = now - timeWindowMs
    total_intensity = 0.0
    node_count = 0

    for student in students:
        dx = student.x - targetX
        dy = student.y - targetY
        distance = math.sqrt(dx * dx + dy * dy)

        if distance <= RADAR_RADIUS:
            node_count += 1
            logs = behaviorLogsByStudent.get(student.id, [])

            distance_factor = 1.0 - (distance / RADAR_RADIUS)

            for event in logs:
                if event.timestamp < cutoff:
                    break

                event_weight = 0.1
                if "Negative" in event.type:
                    event_weight = 0.4
                elif "Positive" in event.type:
                    event_weight = 0.2

                time_factor = (event.timestamp - cutoff) / float(timeWindowMs)
                total_intensity += event_weight * distance_factor * time_factor

    if node_count == 0:
        return 0.0

    return min(max(total_intensity / 2.0, 0.0), 1.0)

# Test Data
students = [
    Student(1, 1000, 1000),
    Student(2, 1100, 1100),
    Student(3, 3000, 3000)
]

now = 1000000000
behavior_logs = {
    1: [BehaviorEvent(1, "Negative Behavior", now - 1000)],
    2: [BehaviorEvent(2, "Positive Behavior", now - 500)],
    3: [BehaviorEvent(3, "Negative Behavior", now - 100)]
}

print("Simulating Ghost Radar Resonance...")

# Test 1: Near students 1 and 2
res1 = calculate_local_resonance(1050, 1050, students, behavior_logs)
print(f"Resonance near cluster (1, 2): {res1:.4f}")

# Test 2: Near student 3
res2 = calculate_local_resonance(3000, 3000, students, behavior_logs)
print(f"Resonance near student 3: {res2:.4f}")

# Test 3: Far from everyone
res3 = calculate_local_resonance(0, 0, students, behavior_logs)
print(f"Resonance in void: {res3:.4f}")

# Verification
assert res1 > 0, "Resonance near cluster should be > 0"
assert res2 > 0, "Resonance near student 3 should be > 0"
assert res3 == 0, "Resonance in void should be 0"
assert res1 != res2, "Resonances should differ based on local activity"

print("\nSimulation PASSED: Localized resonance logic is correct.")
