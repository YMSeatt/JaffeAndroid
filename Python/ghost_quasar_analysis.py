import json
import time

def analyze_quasars(json_data, window_minutes=30):
    """
    Identifies high-energy 'Quasar' nodes from exported classroom data.
    Matches the logic in GhostQuasarEngine.kt.
    """
    data = json.loads(json_data)
    students = data.get('students', [])
    behavior_logs = data.get('behavior_logs', [])

    current_time_ms = int(time.time() * 1000)
    window_ms = window_minutes * 60 * 1000

    recent_logs = [log for log in behavior_logs if current_time_ms - log['timestamp'] < window_ms]

    logs_by_student = {}
    for log in recent_logs:
        sid = log['studentId']
        if sid not in logs_by_student:
            logs_by_student[sid] = []
        logs_by_student[sid].append(log)

    quasars = []
    for student in students:
        sid = student['id']
        student_logs = logs_by_student.get(sid, [])

        if len(student_logs) >= 3:
            energy = min(len(student_logs) / 10.0, 1.0)

            positive_count = sum(1 for log in student_logs if "Positive" in log['type'])
            negative_count = sum(1 for log in student_logs if "Negative" in log['type'])
            total = max(positive_count + negative_count, 1)

            behavior_polarity = (positive_count - negative_count) / float(total)
            luminosity = 0.5 + (energy * 0.5)

            quasars.append({
                "student_id": sid,
                "name": student.get('name', 'Unknown'),
                "energy": energy,
                "luminosity": luminosity,
                "polarity": behavior_polarity,
                "x": student.get('x', 0),
                "y": student.get('y', 0)
            })

    return quasars

if __name__ == "__main__":
    # Mock data for demonstration
    mock_data = {
        "students": [{"id": 1, "name": "Alice", "x": 100, "y": 200}],
        "behavior_logs": [
            {"studentId": 1, "type": "Positive Behavior", "timestamp": int(time.time() * 1000) - 1000},
            {"studentId": 1, "type": "Positive Behavior", "timestamp": int(time.time() * 1000) - 2000},
            {"studentId": 1, "type": "Negative Behavior", "timestamp": int(time.time() * 1000) - 3000}
        ]
    }

    results = analyze_quasars(json.dumps(mock_data))
    print(f"Quasars identified: {len(results)}")
    for q in results:
        print(f" - {q['name']}: Energy={q['energy']:.2f}, Polarity={q['polarity']:.2f}")
