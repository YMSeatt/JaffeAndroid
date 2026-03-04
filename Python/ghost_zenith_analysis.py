import math
import json
import random

"""
Ghost Zenith: Neural Depth Analysis Blueprint.

This script demonstrates the mathematical foundation for mapping multi-dimensional
student data into a 3D coordinate space for spatial visualization.

Metrics Mapped:
- X, Y: Seating Chart Coordinates.
- Z (Altitude): Academic Buoyancy + Behavioral Stability.
- Vector (Tilt): Simulated device orientation impact on node projections.
"""

def calculate_student_altitude(quiz_avg, behavior_score):
    """
    Calculates the 'Altitude' (Z-axis) of a student node.
    Logic Parity with GhostZenithEngine.kt.
    """
    # Weighted average: Academic performance provides 70% of buoyancy
    altitude = (quiz_avg * 0.7) + (behavior_score * 0.3)
    return max(0.0, min(1.0, altitude))

def project_3d_to_2d(x, y, z, pitch, roll):
    """
    Projects a 3D node onto a 2D plane based on device tilt.
    Simulates the parallax shift seen in the Android app.
    """
    # Parallax shift increases with 'distance' from the glass (1.0 - z)
    depth_factor = 1.0 - z

    # max shift of 100 pixels
    shift_x = roll * 100 * depth_factor
    shift_y = pitch * 100 * depth_factor

    return x + shift_x, y + shift_y

def run_zenith_simulation():
    print("--- 👻 GHOST ZENITH: SPATIAL DATA ANALYSIS ---")

    # Mock Student Data
    students = [
        {"name": "Student Alpha", "x": 500, "y": 500, "quiz": 0.95, "behavior": 0.9},  # High performer (Close to glass)
        {"name": "Student Beta", "x": 2000, "y": 2000, "quiz": 0.4, "behavior": 0.3}, # Struggling (Deep in neural sea)
        {"name": "Student Gamma", "x": 3500, "y": 3500, "quiz": 0.7, "behavior": 0.8} # Average (Mid-depth)
    ]

    # Simulated Device Tilt (in radians)
    pitch = 0.2  # 11 degrees tilt back
    roll = -0.1  # 5 degrees tilt left

    print(f"Device State: Pitch={pitch:.2f} rad, Roll={roll:.2f} rad\n")
    print(f"{'Student':<15} | {'Altitude':<10} | {'Original (X,Y)':<15} | {'Projected (X,Y)':<15}")
    print("-" * 65)

    for s in students:
        alt = calculate_student_altitude(s['quiz'], s['behavior'])
        proj_x, proj_y = project_3d_to_2d(s['x'], s['y'], alt, pitch, roll)

        print(f"{s['name']:<15} | {alt:<10.2f} | ({s['x']:>4}, {s['y']:>4}) | ({proj_x:>6.1f}, {proj_y:>6.1f})")

    print("\n[ANALYSIS]")
    print("- High altitude nodes (Alpha) exhibit minimal parallax shift.")
    print("- Low altitude nodes (Beta) are submerged deeper, showing significant displacement.")
    print("- This creates a physical hierarchy of 'needs' where distant nodes stand out during tilt.")

if __name__ == "__main__":
    run_zenith_simulation()
