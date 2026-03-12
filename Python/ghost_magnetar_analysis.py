import json
import math
import os

def analyze_magnetic_field(students_json_path, behaviors_json_path):
    """
    Simulates the "Ghost Magnetar" social magnetic field mapping for the Python desktop suite.

    This script maps students to magnetic dipoles based on behavioral trends and
    calculates field vectors at specific logical points.
    """
    if not os.path.exists(students_json_path) or not os.path.exists(behaviors_json_path):
        print("Data files missing.")
        return

    with open(students_json_path, 'r') as f:
        students = json.load(f)
    with open(behaviors_json_path, 'r') as f:
        behaviors = json.load(f)

    # 1. Map students to Dipoles
    dipoles = []
    for student in students:
        sid = student.get('id')
        name = f"{student.get('firstName')} {student.get('lastName')}"
        x, y = student.get('xPosition', 0), student.get('yPosition', 0)

        # Calculate polarity (North/South weight)
        student_logs = [b for b in behaviors if b.get('studentId') == sid]
        north = len([l for l in student_logs if "Positive" in l.get('type', '')])
        south = len([l for l in student_logs if "Negative" in l.get('type', '')])

        strength = north - (south * 1.5)
        dipoles.append({
            "name": name,
            "x": x,
            "y": y,
            "strength": strength
        })

    print(f"--- GHOST MAGNETAR ANALYSIS (Python Bridge) ---")
    print(f"Analyzed {len(dipoles)} neural dipoles.\n")

    # 2. Sample Field Strengths at logical quadrant centers
    quadrants = [
        ("Top-Left", 500, 375),
        ("Top-Right", 1500, 375),
        ("Bottom-Left", 500, 1125),
        ("Bottom-Right", 1500, 1125)
    ]

    for q_name, qx, qy in quadrants:
        fx, fy = 0.0, 0.0
        for d in dipoles:
            dx = qx - d['x']
            dy = qy - d['y']
            r2 = dx*dx + dy*dy + 100.0
            r3 = math.pow(r2, 1.5)

            # Field contribution
            fx += (dx / r3) * d['strength'] * 5000.0
            fy += (dy / r3) * d['strength'] * 5000.0

        total_strength = math.sqrt(fx*fx + fy*fy)
        print(f"Quadrant {q_name}: Field Intensity = {total_strength:.4f} μG")

    print("\n--- Magnetic Polarity Map ---")
    for d in dipoles:
        polarity = "NORTH (+)" if d['strength'] > 0 else "SOUTH (-)" if d['strength'] < 0 else "NEUTRAL"
        print(f"[{d['name']}]: {polarity} (Strength: {d['strength']:.2f})")

if __name__ == "__main__":
    # Mock paths for logic parity demonstration
    analyze_magnetic_field("students.json", "behaviors.json")
