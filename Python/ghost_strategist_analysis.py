import json
import time

"""
GhostStrategistAnalysis: Pedagogical Heuristics Blueprint.

This script provides logic parity for the on-device AI strategist.
It defines how the generative advisor transforms classroom logs and prophecies
into tactical interventions.
"""

def generate_interventions(students, logs, prophecies, goal='STABILITY'):
    interventions = []

    # 1. Prophecy Mapping
    for p in prophecies:
        if p['type'] == 'SOCIAL_FRICTION' and goal in ['HARMONY', 'STABILITY']:
            interventions.append({
                'student_id': p['student_id'],
                'title': "Proactive Buffer Insertion",
                'description': f"Social friction predicted for Student {p['student_id']}. Recommend 5m collaborative task with neutral partner.",
                'urgency': p['confidence'],
                'category': 'SOCIAL_DYNAMICS'
            })
        elif p['type'] == 'ENGAGEMENT_DROP' and goal != 'EXCELLENCE':
            interventions.append({
                'student_id': p['student_id'],
                'title': "Positive Recalibration",
                'description': f"Engagement for Student {p['student_id']} is decaying. Strategic Action: Deliver Micro-Feedback immediately.",
                'urgency': 0.8,
                'category': 'BEHAVIORAL_REINFORCEMENT'
            })

    # 2. Heuristic Analysis
    current_ts = time.time() * 1000
    for s in students:
        s_id = s['id']
        s_logs = [l for l in logs if l['student_id'] == s_id]

        # High Frequency Negative Logic
        recent_negatives = [l for l in s_logs if 'Negative' in l['type'] and current_ts - l['timestamp'] < 3600000]
        if len(recent_negatives) > 1:
            interventions.append({
                'student_id': s_id,
                'title': "Atmospheric Reset",
                'description': f"High-density behavioral friction for Student {s_id}. Recommend private 1:1 check-in.",
                'urgency': 0.95,
                'category': 'TEMPORAL_ADJUSTMENT'
            })

    # Deduplicate and sort
    seen = set()
    unique_interventions = []
    for i in sorted(interventions, key=lambda x: x['urgency'], reverse=True):
        key = (i['student_id'], i['title'])
        if key not in seen:
            unique_interventions.append(i)
            seen.add(key)

    return unique_interventions

if __name__ == "__main__":
    # Mock Scenario
    mock_students = [{'id': 1}, {'id': 2}]
    mock_logs = [
        {'student_id': 1, 'type': 'Negative behavior', 'timestamp': time.time() * 1000 - 1000}
    ]
    mock_prophecies = [
        {'student_id': 2, 'type': 'SOCIAL_FRICTION', 'confidence': 0.9}
    ]

    results = generate_interventions(mock_students, mock_logs, mock_prophecies)
    print(json.dumps(results, indent=2))
