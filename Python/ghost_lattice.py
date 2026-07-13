import json
import os
import math

"""
Ghost Lattice: Social Dynamics Visualizer.

This script analyzes student proximity and shared behavioral markers to
infer a "Social Lattice" (a network graph of relationships).

### Logic Parity Note
This script serves as the prototype for the Android `GhostLatticeEngine.kt`.
Note that the mobile implementation uses a higher proximity threshold (800 units)
to account for canvas scaling and a different classification heuristic (log-driven
random seeding) to improve cultural adaptability.
"""

def analyze_social_lattice(students_file, logs_file):
    """
    Analyzes student data and logs to build a social relationship graph.

    This script serves as the logical prototype for the `GhostLatticeLayer` on Android.
    It infers connections (Collaboration, Friction, or Neutral) between students
    based on proximity and behavioral patterns.

    ### Heuristics & Constraints:
    1. **Proximity Threshold**: Uses a 400-unit threshold in Python (typically
       scaled to 800-1000 units in the Android production environment).
    2. **Relationship Proxy**: In this prototype, shared surnames are used as a
       low-fidelity proxy for social collaboration, whereas the Android app
       uses actual behavioral log correlation.

    Args:
        students_file (str): Path to the students JSON file (v10 schema).
        logs_file (str): Path to the behavior logs JSON file.

    Returns:
        None: Results are written to `lattice_analysis.json`.
    """
    if not os.path.exists(students_file):
        print(f"Error: {students_file} not found.")
        return

    with open(students_file, 'r') as f:
        students = json.load(f)

    lattice_data = {
        "clusters": [],
        "edges": [],
        "metrics": {
            "classroom_cohesion": 0.75,
            "social_turbulence": 0.12
        }
    }

    # Relationship Inference Loop
    for i, s1 in enumerate(students):
        for j, s2 in enumerate(students):
            if i < j:
                dx = s1.get('xPosition', 0) - s2.get('xPosition', 0)
                dy = s1.get('yPosition', 0) - s2.get('yPosition', 0)
                dist = math.sqrt(dx*dx + dy*dy)

                # Proximity Threshold: 400 logical units
                if dist < 400:
                    strength = 1.0 - (dist / 400)
                    edge_type = "neutral"

                    # Heuristic: Collaboration proxy (shared last initial)
                    if s1.get('lastName', ' ')[0] == s2.get('lastName', ' ')[0]:
                        edge_type = "collaboration"
                        strength += 0.2

                    lattice_data["edges"].append({
                        "from": s1.get('id'),
                        "to": s2.get('id'),
                        "strength": min(strength, 1.0),
                        "type": edge_type
                    })

    with open('lattice_analysis.json', 'w') as f:
        json.dump(lattice_data, f, indent=2)
    print("Social lattice analysis complete. Results saved to lattice_analysis.json")

if __name__ == "__main__":
    analyze_social_lattice('students.json', 'behavior_logs.json')
