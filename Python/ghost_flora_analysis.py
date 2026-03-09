import json
import random
from datetime import datetime

class GhostFloraAnalysis:
    """
    GhostFloraAnalysis: Python implementation of the Neural Botanical logic.
    Provides mathematical parity with GhostFloraEngine.kt.
    """

    def __init__(self):
        self.max_growth_logs = 10

    def analyze_student_flora(self, student_id, behavior_logs, quiz_logs):
        """
        Calculates the botanical state for a student based on exported JSON data.

        :param student_id: Long
        :param behavior_logs: List of dicts (BehaviorEvent)
        :param quiz_logs: List of dicts (QuizLog)
        :return: Dict containing growth parameters
        """
        positive_count = 0
        negative_count = 0
        for event in behavior_logs:
            if "Negative" in event.get("type", ""):
                negative_count += 1
            else:
                positive_count += 1

        total_quiz_ratio = 0.0
        valid_quiz_count = 0
        for log in quiz_logs:
            v = log.get("markValue")
            m = log.get("maxMarkValue")
            if v is not None and m is not None and m > 0:
                total_quiz_ratio += (float(v) / float(m))
                valid_quiz_count += 1

        avg_quiz = total_quiz_ratio / valid_quiz_count if valid_quiz_count > 0 else 0.7

        # 1. Growth
        growth = min(1.0, max(0.1, positive_count / self.max_growth_logs))

        # 2. Vitality
        total_behavior = max(1, len(behavior_logs))
        vitality = min(1.0, max(-1.0, (positive_count - negative_count) / total_behavior))

        # 3. Petal Count (Deterministic ID-based)
        petal_count = 3 + (student_id % 6)

        # 4. Complexity
        complexity = min(1.0, max(0.1, avg_quiz))

        # 5. Color Shift
        color_shift = min(1.0, max(0.0, vitality * 0.5 + 0.5))

        return {
            "student_id": student_id,
            "growth": round(growth, 3),
            "vitality": round(vitality, 3),
            "petal_count": int(petal_count),
            "complexity": round(complexity, 3),
            "color_shift": round(color_shift, 3),
            "timestamp": datetime.now().isoformat()
        }

    def generate_botanical_report(self, classroom_data):
        """
        Generates a classroom-wide botanical summary.
        """
        students = classroom_data.get("students", [])
        all_behavior = classroom_data.get("behavior_events", [])
        all_quiz = classroom_data.get("quiz_logs", [])

        report = []
        for student in students:
            s_id = student.get("id")
            s_behavior = [b for b in all_behavior if b.get("studentId") == s_id]
            s_quiz = [q for q in all_quiz if q.get("studentId") == s_id]

            analysis = self.analyze_student_flora(s_id, s_behavior, s_quiz)
            report.append({
                "name": student.get("fullName"),
                "botanical_state": analysis
            })

        return report

if __name__ == "__main__":
    # Sample Test Case
    analyzer = GhostFloraAnalysis()
    sample_behavior = [{"type": "Positive Participation"} for _ in range(5)]
    sample_behavior.append({"type": "Negative behavior"})
    sample_quiz = [{"markValue": 8, "maxMarkValue": 10}]

    result = analyzer.analyze_student_flora(12345, sample_behavior, sample_quiz)
    print("--- Ghost Flora Analysis (Python Parity) ---")
    print(json.dumps(result, indent=4))
