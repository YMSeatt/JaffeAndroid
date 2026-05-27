import math
import random

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

class QuizLog:
    def __init__(self, studentId, markValue, maxMarkValue):
        self.studentId = studentId
        self.markValue = markValue
        self.maxMarkValue = maxMarkValue

def pick_beacon_target(students, behaviorLogs, quizLogs, homeworkLogs):
    if not students:
        return None

    now = 1000000 # Simulated now
    negative_counts = {}
    last_positive_log = {}
    academic_scores = {}

    for log in behaviorLogs:
        sid = log.studentId
        if "Negative" in log.type:
            negative_counts[sid] = negative_counts.get(sid, 0) + 1
        else:
            last = last_positive_log.get(sid, 0)
            if log.timestamp > last:
                last_positive_log[sid] = log.timestamp

    for log in quizLogs:
        sid = log.studentId
        percentage = log.markValue / log.maxMarkValue if log.maxMarkValue > 0 else 0.0
        if sid not in academic_scores:
            academic_scores[sid] = []
        academic_scores[sid].append(percentage)

    total_weight = 0
    weighted_students = []

    for student in students:
        sid = student.id
        nfi = 1.0

        negs = negative_counts.get(sid, 0)
        nfi += negs * 2.0

        last_pos = last_positive_log.get(sid, 0)
        hours_since_positive = 24.0 if last_pos == 0 else (now - last_pos) / 3600.0
        nfi += min(hours_since_positive / 2.0, 5.0)

        scores = academic_scores.get(sid, [])
        if scores:
            avg = sum(scores) / len(scores)
            nfi += (1.0 - avg) * 3.0
        else:
            nfi += 1.0

        total_weight += nfi
        weighted_students.append((sid, nfi))

    print(f"Weights: {weighted_students}")

    # Selection simulation
    results = {}
    for _ in range(1000):
        r = random.uniform(0, total_weight)
        current = 0
        for sid, weight in weighted_students:
            current += weight
            if r <= current:
                results[sid] = results.get(sid, 0) + 1
                break

    return results

# Test Data
students = [
    Student(1, 100, 100), # Good student
    Student(2, 200, 200), # Struggling/Negative student
    Student(3, 300, 300)  # Neutral student
]

behaviors = [
    BehaviorEvent(2, "Negative Participation", 999900),
    BehaviorEvent(2, "Negative Behavior", 999800),
    BehaviorEvent(1, "Positive Participation", 999999)
]

quizzes = [
    QuizLog(1, 95, 100),
    QuizLog(2, 40, 100)
]

print("Simulating Beacon selection...")
stats = pick_beacon_target(students, behaviors, quizzes, [])
for sid, count in sorted(stats.items()):
    print(f"Student {sid}: selected {count} times ({(count/10.0):.1f}%)")

# Verify that student 2 (high NFI) is picked more often than student 1
assert stats[2] > stats[1], "Student 2 (high NFI) should be picked more than Student 1"
print("\nSimulation PASSED: Weighted logic favors students in need.")
