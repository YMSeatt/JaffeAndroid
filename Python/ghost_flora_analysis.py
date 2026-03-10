import json
import os

def calculate_flora_state(student_id, behavior_logs, quiz_logs, homework_logs):
    """
    Implements the Ghost Flora logic in Python for logical parity with the Android engine.
    Calculates Growth (Academic), Vitality (Behavioral), and Complexity (Activity).
    """

    # 1. Growth: Driven by academic average (Quiz + Homework)
    quiz_marks = [log.get('markValue', 0) / log.get('maxMarkValue', 1) for log in quiz_logs if log.get('maxMarkValue', 0) > 0]
    quiz_avg = sum(quiz_marks) / len(quiz_marks) if quiz_marks else 0.7

    homework_done = [log for log in homework_logs if "Done" in log.get('status', '')]
    homework_rate = len(homework_done) / len(homework_logs) if homework_logs else 0.8

    growth = (quiz_avg + homework_rate) / 2.0

    # 2. Vitality: Driven by behavioral balance (Positive vs Negative)
    total_behaviors = len(behavior_logs)
    positive_count = len([log for log in behavior_logs if "Negative" not in log.get('type', '')])
    vitality = positive_count / total_behaviors if total_behaviors > 0 else 0.9

    # 3. Complexity: Driven by total activity frequency
    total_logs = len(behavior_logs) + len(quiz_logs) + len(homework_logs)
    complexity = min(max(total_logs / 10.0, 0.1), 1.0)

    return {
        "student_id": student_id,
        "growth": min(max(growth, 0.2), 1.2),
        "vitality": min(max(vitality, 0.0), 1.0),
        "complexity": complexity
    }

def analyze_classroom_flora(data_path):
    """
    Analyzes an exported classroom JSON file and generates a macroscopic Flora Report.
    """
    if not os.path.exists(data_path):
        print(f"Error: File {data_path} not found.")
        return

    with open(data_path, 'r') as f:
        data = json.load(f)

    students = data.get('students', [])
    behavior_logs = data.get('behavior_events', [])
    quiz_logs = data.get('quiz_logs', [])
    homework_logs = data.get('homework_logs', [])

    print("--- 👻 Ghost Flora: Macroscopic Analysis ---")

    total_growth = 0
    total_vitality = 0

    for student in students:
        s_id = student.get('id')
        s_name = student.get('firstName', 'Unknown')

        s_behaviors = [log for log in behavior_logs if log.get('studentId') == s_id]
        s_quizzes = [log for log in quiz_logs if log.get('studentId') == s_id]
        s_homework = [log for log in homework_logs if log.get('studentId') == s_id]

        state = calculate_flora_state(s_id, s_behaviors, s_quizzes, s_homework)

        total_growth += state['growth']
        total_vitality += state['vitality']

        indicator = "🌸" if state['vitality'] > 0.8 else "🥀" if state['vitality'] < 0.4 else "🌿"
        print(f"Student: {s_name} | Growth: {state['growth']:.2f} | Vitality: {state['vitality']:.2f} | State: {indicator}")

    avg_growth = total_growth / len(students) if students else 0
    avg_vitality = total_vitality / len(students) if students else 0

    print("\n--- Classroom Summary ---")
    print(f"Average Growth (Academic): {avg_growth:.2f}")
    print(f"Average Vitality (Climate): {avg_vitality:.2f}")

    climate = "Tropical (High Vitality)" if avg_vitality > 0.7 else "Tundra (Low Vitality)"
    print(f"Overall Ecosystem Climate: {climate}")

if __name__ == "__main__":
    # Example usage:
    # analyze_classroom_flora("classroom_export.json")
    print("Ghost Flora Analysis Engine Loaded.")
