import json
import math
import sys
from collections import Counter

def calculate_shannon_entropy(logs):
    """
    Calculates the Shannon Entropy of behavior types.
    Matches the logic in GhostEntropyEngine.kt.
    """
    if not logs:
        return 0.0

    # Extract behavioral types from logs
    types = [log.get('type') for log in logs if 'type' in log]
    if not types:
        return 0.0

    counts = Counter(types)
    total = len(types)

    entropy = 0.0
    for count in counts.values():
        p = count / total
        entropy -= (p * math.log(p))

    # Normalize: Using log(5) as max possible types for normalization
    max_possible = math.log(5.0)
    normalized_entropy = entropy / max_possible
    return min(max(normalized_entropy, 0.0), 1.0)

def analyze_classroom_entropy(data):
    """
    Analyzes student behavioral logs to calculate individual and global entropy.
    """
    students = data.get('students', [])
    behavior_logs = data.get('behavior_events', [])

    # Group logs by student
    student_logs = {}
    for log in behavior_logs:
        s_id = log.get('studentId')
        if s_id not in student_logs:
            student_logs[s_id] = []
        student_logs[s_id].append(log)

    entropy_report = []
    global_total_entropy = 0.0

    for student in students:
        s_id = student.get('id')
        name = student.get('fullName', f"Student {s_id}")
        logs = student_logs.get(s_id, [])

        entropy = calculate_shannon_entropy(logs)
        global_total_entropy += entropy

        entropy_report.append({
            "name": name,
            "entropy": entropy,
            "status": "TURBULENT" if entropy > 0.7 else "STABLE"
        })

    avg_entropy = global_total_entropy / len(students) if students else 0.0

    return {
        "global_entropy_index": avg_entropy,
        "student_reports": entropy_report
    }

def main():
    print("--- 👻 GHOST ENTROPY: PYTHON ANALYSIS BRIDGE ---")

    # Mock data for demonstration
    mock_data = {
        "students": [
            {"id": 1, "fullName": "John Doe"},
            {"id": 2, "fullName": "Jane Smith"}
        ],
        "behavior_events": [
            {"studentId": 1, "type": "Positive Participation"},
            {"studentId": 1, "type": "Negative behavior"},
            {"studentId": 1, "type": "Asked Question"},
            {"studentId": 2, "type": "Positive Participation"},
            {"studentId": 2, "type": "Positive Participation"}
        ]
    }

    results = analyze_classroom_entropy(mock_data)

    print(f"Global Entropy Index: {results['global_entropy_index']:.2%}")
    print("\nDetailed Student Reports:")
    for report in results['student_reports']:
        print(f"- {report['name']}: {report['entropy']:.2%} ({report['status']})")

if __name__ == "__main__":
    main()
