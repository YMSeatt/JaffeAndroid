import json
import math
import sys
import os

def analyze_curvature(data_path):
    """
    Analyzes 'Spacetime Curvature' in the classroom based on student behavioral density.
    Calculates the 'Curvature Gradient' and identifies gravitational hotspots.
    """
    if not os.path.exists(data_path):
        print(f"Error: File {data_path} not found.")
        return

    with open(data_path, 'r') as f:
        data = json.load(f)

    students = data.get('students', [])
    behavior_logs = data.get('behavior_events', [])

    if not students:
        print("No student data found.")
        return

    print("--- 👻 Ghost Warp Analysis: Classroom Curvature ---")

    # Calculate student mass
    student_mass = {}
    for log in behavior_logs:
        s_id = log.get('studentId')
        log_type = log.get('type', '')

        weight = 1.5 if 'Negative' in log_type else 1.0
        student_mass[s_id] = student_mass.get(s_id, 0) + weight

    # Calculate Local Curvature (Density of mass in spatial clusters)
    hotspots = []
    for s in students:
        s_id = s.get('id')
        mass = student_mass.get(s_id, 0)
        if mass == 0: continue

        x, y = s.get('x_position', 0), s.get('y_position', 0)

        # Influence radius (scaled by mass)
        radius = 200 + (mass * 50)

        hotspots.append({
            'name': s.get('first_name', 'Student'),
            'x': x,
            'y': y,
            'mass': mass,
            'curvature': round(mass / 10.0, 2)
        })

    # Sort by curvature
    hotspots.sort(key=lambda h: h['curvature'], reverse=True)

    print(f"\nDetected {len(hotspots)} Gravitational Hotspots:")
    for h in hotspots[:5]:
        print(f"  • {h['name']}: Curvature {h['curvature']} at ({h['x']}, {h['y']})")

    # Global Curvature Metric
    total_mass = sum(student_mass.values())
    avg_curvature = total_mass / len(students) if students else 0

    print(f"\nGlobal Classroom Curvature: {avg_curvature:.2f}")

    if avg_curvature > 5.0:
        print("Status: HIGH DISTORTION - Significant behavioral energy detected.")
    elif avg_curvature > 2.0:
        print("Status: NOMINAL - Stable data-plane.")
    else:
        print("Status: FLAT - Low activity levels.")

    print("\n[Recommendation]: Increase spacing near high-curvature nodes to prevent social collision.")
    print("--------------------------------------------------")

if __name__ == "__main__":
    path = sys.argv[1] if len(sys.argv) > 1 else 'classroom_data.json'
    analyze_curvature(path)
