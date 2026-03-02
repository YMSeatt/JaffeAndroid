import json
import math
from datetime import datetime

"""
Ghost Ion: Neural Ionization Analyzer (Python Bridge)

This script analyzes exported classroom data to calculate "Ionic Charge Distribution"
and "Atmospheric Agitation" levels. It maintains logic parity with the
Android GhostIonEngine.
"""

def analyze_ionization(data, battery_temp=30.0):
    students = data.get('students', [])
    behavior_logs = data.get('behavior_events', [])

    analysis = {
        "timestamp": datetime.now().isoformat(),
        "global_charge": 0.0,
        "ionic_hotspots": [],
        "agitation_index": 0.0
    }

    charges = []

    for student in students:
        s_id = student['id']
        # Get last 5 logs for this student
        s_logs = [l for l in behavior_logs if l['studentId'] == s_id][-5:]

        if not s_logs:
            charge = 0.0
        else:
            positives = sum(1 for l in s_logs if "Positive" in l.get('type', '') or "Participating" in l.get('type', ''))
            negatives = sum(1 for l in s_logs if "Negative" in l.get('type', '') or "Disruptive" in l.get('type', ''))
            charge = (positives - negatives) / len(s_logs)

        charges.append(charge)

        # Agitation is influenced by battery temp and activity
        temp_factor = max(0, min(20, battery_temp - 25)) / 20.0
        density = (len(s_logs) / 5.0 * 0.7) + (temp_factor * 0.3)

        analysis["ionic_hotspots"].append({
            "student_name": student.get('firstName', 'Unknown'),
            "charge": charge,
            "density": min(1.0, density)
        })

    if charges:
        analysis["global_charge"] = sum(charges) / len(charges)
        analysis["agitation_index"] = sum(h["density"] for h in analysis["ionic_hotspots"]) / len(students)

    return analysis

if __name__ == "__main__":
    # Mock data for demonstration
    mock_data = {
        "students": [
            {"id": 1, "firstName": "Alice"},
            {"id": 2, "firstName": "Bob"}
        ],
        "behavior_events": [
            {"studentId": 1, "type": "Positive Participation"},
            {"studentId": 2, "type": "Negative Behavior"}
        ]
    }

    results = analyze_ionization(mock_data, battery_temp=38.5)
    print(f"--- Ghost Ion Analysis ---")
    print(f"Global Charge: {results['global_charge']:.2f}")
    print(f"Agitation Index: {results['agitation_index']:.2f}")
    for hotspot in results['ionic_hotspots']:
        print(f" - {hotspot['student_name']}: Charge={hotspot['charge']:.2f}, Density={hotspot['density']:.2f}")
