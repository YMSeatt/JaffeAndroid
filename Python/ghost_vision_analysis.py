import math
import json
import os

"""
Ghost Vision: Neural AR Spatial Analysis (Logic Parity).

This script simulates the spatial "Field of View" (FoV) analysis used by
the Ghost Vision AR engine. It identifies which students fall within
the teacher's virtual AR viewport based on a given device orientation.
"""

def analyze_vision_engagement(json_data, azimuth_deg, pitch_deg, fov_deg=60.0):
    """
    Analyzes which students are 'Observed' in the AR viewport.

    :param json_data: Classroom layout JSON.
    :param azimuth_deg: Device Yaw in degrees.
    :param pitch_deg: Device Pitch in degrees.
    :param fov_deg: Field of View of the AR viewport.
    :return: List of visible student names.
    """
    students = json_data.get("students", [])
    visible_students = []

    azimuth_rad = math.radians(azimuth_deg)
    pitch_rad = math.radians(pitch_deg)
    fov_rad = math.radians(fov_deg)

    print(f"--- Ghost Vision Analysis (FoV: {fov_deg}°) ---")
    print(f"Orientation: Az={azimuth_deg}°, Pitch={pitch_deg}°\n")

    for student in students:
        name = student.get("name", "Unknown")
        x = student.get("x", 2000)
        y = student.get("y", 2000)

        # Logic Parity with GhostVisionEngine.kt project()
        target_az = ((x - 2000.0) / 2000.0) * math.pi
        target_pitch = ((y - 2000.0) / 2000.0) * (math.pi / 2.0)

        delta_az = target_az - azimuth_rad
        # Normalize
        while delta_az > math.pi: delta_az -= 2 * math.pi
        while delta_az < -math.pi: delta_az += 2 * math.pi

        delta_pitch = target_pitch - pitch_rad

        is_visible = abs(delta_az) < fov_rad and abs(delta_pitch) < fov_rad

        if is_visible:
            visible_students.append(name)
            print(f"[VISIBLE] {name.ljust(15)} | ΔAz: {math.degrees(delta_az):.1f}° | ΔPt: {math.degrees(delta_pitch):.1f}°")
        else:
            pass # Ocular occlusion logic could go here

    return visible_students

if __name__ == "__main__":
    # Mock data for parity testing
    mock_classroom = {
        "students": [
            {"name": "Alice", "x": 1000, "y": 1000},
            {"name": "Bob", "x": 2000, "y": 2000},
            {"name": "Charlie", "x": 3000, "y": 3000},
            {"name": "David", "x": 2100, "y": 1900}
        ]
    }

    # Simulate looking at the center (Bob/David)
    analyze_vision_engagement(mock_classroom, 0, 0)

    # Simulate looking left (Alice)
    print("\nScanning Left...")
    analyze_vision_engagement(mock_classroom, -45, -22)
