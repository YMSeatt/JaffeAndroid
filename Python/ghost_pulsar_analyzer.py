import json
import os
import math

def analyze_pulsar_rhythms(backup_path):
    """
    Analyzes classroom rhythms by performing a frequency analysis on behavior logs.
    Identifies students with synchronized harmonic patterns.
    """
    if not os.path.exists(backup_path):
        print(f"Error: Backup file {backup_path} not found.")
        return

    with open(backup_path, 'r') as f:
        data = json.load(f)

    students_data = data.get('students', {})
    behavior_logs = data.get('behavior_logs', [])

    print(f"--- 👻 Ghost Pulsar: Classroom Rhythm Analysis ---")
    print(f"Analyzing {len(students_data)} students and {len(behavior_logs)} logs...\n")

    # Group logs by student
    student_logs = {}
    for log in behavior_logs:
        sid = log.get('studentId')
        if sid not in student_logs:
            student_logs[sid] = []
        student_logs[sid].append(log.get('timestamp'))

    harmonics = []
    window_ms = 600000  # 10 minutes

    for sid, student in students_data.items():
        name = f"{student.get('first_name')} {student.get('last_name')}"
        logs = student_logs.get(sid, [])

        # Simple frequency calculation (logs per minute in the last window)
        # For a real PoC, we use the full history but focus on density
        if not logs:
            continue

        freq = len(logs) / (window_ms / 60000.0)
        amplitude = min(1.5, len(logs) / 5.0)

        harmonics.append({
            'name': name,
            'freq': freq,
            'amplitude': amplitude
        })

    # Sort by frequency to find rhythm clusters
    harmonics.sort(key=lambda x: x['freq'], reverse=True)

    print(f"{'Student Name':<25} | {'Frequency (LPM)':<15} | {'Harmonic Amplitude':<15}")
    print("-" * 60)
    for h in harmonics:
        print(f"{h['name']:<25} | {h['freq']:<15.2f} | {h['amplitude']:<15.2f}")

    # Detect Synchronicity Bonds
    print("\n--- Detected Harmonic Bonds (Synchronized Pairs) ---")
    bonds_found = False
    for i in range(len(harmonics)):
        for j in range(i + 1, len(harmonics)):
            diff = abs(harmonics[i]['freq'] - harmonics[j]['freq'])
            if diff < 0.2:
                print(f"🔗 Bond: {harmonics[i]['name']} <-> {harmonics[j]['name']} (Sync: {1.0 - diff:.2f})")
                bonds_found = True

    if not bonds_found:
        print("No significant harmonic bonds detected in the current data stream.")

if __name__ == "__main__":
    # Mock backup path for demo
    analyze_pulsar_rhythms('Python/Samples/classroom_data_v10.json')
