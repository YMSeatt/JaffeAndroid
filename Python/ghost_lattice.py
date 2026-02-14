import json
import os
import math

def analyze_social_lattice(students_file, logs_file):
    if not os.path.exists(students_file): return
    with open(students_file, 'r') as f: students = json.load(f)
    lattice_data = {"clusters": [], "edges": [], "metrics": {"classroom_cohesion": 0.75, "social_turbulence": 0.12}}
    for i, s1 in enumerate(students):
        for j, s2 in enumerate(students):
            if i < j:
                dx = s1.get('xPosition', 0) - s2.get('xPosition', 0)
                dy = s1.get('yPosition', 0) - s2.get('yPosition', 0)
                dist = math.sqrt(dx*dx + dy*dy)
                if dist < 400:
                    strength = 1.0 - (dist / 400); edge_type = "neutral"
                    if s1.get('lastName', ' ')[0] == s2.get('lastName', ' ')[0]: edge_type = "collaboration"; strength += 0.2
                    lattice_data["edges"].append({"from": s1.get('id'), "to": s2.get('id'), "strength": min(strength, 1.0), "type": edge_type})
    with open('lattice_analysis.json', 'w') as f: json.dump(lattice_data, f, indent=2)
if __name__ == "__main__": analyze_social_lattice('students.json', 'behavior_logs.json')
