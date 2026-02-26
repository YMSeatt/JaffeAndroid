import json
import random
from datetime import datetime, timedelta

"""
Ghost Future: Neural Classroom Simulation (Python Bridge)

This script provides a high-fidelity simulation of classroom behavioral
trajectories based on exported JSON data. It extends the logic of the
Android engine to support multi-day simulations and entropy analysis.
"""

def simulate_classroom(data, days=7):
    students = data.get('students', [])
    behavior_logs = data.get('behavior_events', [])

    simulation_report = {
        "simulation_date": datetime.now().isoformat(),
        "days_simulated": days,
        "predicted_events": [],
        "hotspots": []
    }

    # Simple personality inference based on history
    student_profiles = {}
    for student in students:
        s_id = student['id']
        logs = [l for l in behavior_logs if l['studentId'] == s_id]
        negatives = [l for l in logs if 'Negative' in l.get('type', '')]

        student_profiles[s_id] = {
            "name": student.get('firstName', 'Unknown'),
            "negativity_bias": len(negatives) / len(logs) if logs else 0.1,
            "activity_rate": len(logs) / 30.0 # Normalized over a month
        }

    # Simulation loop
    current_time = datetime.now()
    for day in range(days):
        day_time = current_time + timedelta(days=day)

        for s_id, profile in student_profiles.items():
            # Neural probability of event
            prob = profile['activity_rate'] * (1.0 + profile['negativity_bias'])

            if random.random() < prob:
                is_negative = random.random() < profile['negativity_bias']
                event_type = "Simulated Disruptive" if is_negative else "Simulated Focused"

                simulation_report['predicted_events'].append({
                    "studentId": s_id,
                    "studentName": profile['name'],
                    "type": event_type,
                    "timestamp": (day_time + timedelta(hours=random.randint(1, 6))).isoformat()
                })

    return simulation_report

if __name__ == "__main__":
    # Example usage with mock data
    mock_data = {
        "students": [{"id": 1, "firstName": "Alice"}, {"id": 2, "firstName": "Bob"}],
        "behavior_events": [{"studentId": 2, "type": "Negative Behavior", "timestamp": 0}]
    }

    report = simulate_classroom(mock_data)
    print(f"Ghost Future Simulation Complete.")
    print(f"Predicted Events: {len(report['predicted_events'])}")
    for event in report['predicted_events'][:5]:
        print(f" - {event['timestamp']}: {event['studentName']} -> {event['type']}")
