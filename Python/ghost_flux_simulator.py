import json
import os
import math
from datetime import datetime

"""
Ghost Flux Simulator: A Python bridge for the 2027 Classroom Neural Flow experiment.

This script demonstrates how classroom "Momentum" and "Flow Intensity" can be
calculated from exported student behavioral data. In a production environment,
this logic could be offloaded to a local Python bridge or a specialized ML model.
"""

def calculate_flow_intensity(data):
    """
    Calculates the 'Neural Flow' intensity of a classroom based on behavioral events.
    Higher density of recent events leads to a stronger 'Flux' surge.
    """
    students = data.get("students", [])
    behavior_logs = data.get("behavior_logs", [])

    if not behavior_logs:
        return 0.1

    # Simple metric: Activity density relative to classroom size
    student_count = len(students) if students else 1
    log_count = len(behavior_logs)

    # Calculate base intensity (Density of interaction)
    intensity = (log_count / (student_count * 5.0))

    # Apply sinusoidal 'Classroom Tempo' factor to simulate organic waves of engagement
    tempo = 1.0 + 0.2 * math.sin(log_count / 5.0)

    return min(1.0, intensity * tempo)

def main():
    print("🌊 Ghost Lab: Neural Flux Simulator (2027 Prototype)")
    print("-" * 50)

    # Check for exported app data, otherwise use holographic mock data
    export_path = "classroom_export.json"
    if os.path.exists(export_path):
        print(f"🔍 Analyzing real data from {export_path}...")
        with open(export_path, 'r') as f:
            data = json.load(f)
    else:
        print("💡 No app export found. Generating holographic simulation data...")
        data = {
            "students": [{"id": i} for i in range(20)],
            "behavior_logs": [
                {"timestamp": 1700000000, "type": "Participation"},
                {"timestamp": 1700000100, "type": "Creative Insight"},
                {"timestamp": 1700000200, "type": "Behavioral Friction"}
            ] * 12 # Simulating high activity
        }

    intensity = calculate_flow_intensity(data)

    print(f"Calculated Flow Intensity: {intensity:.2f}")

    # Recommendations for the Android Flux Engine
    print("\n[Haptic Engine Recommendation]")
    if intensity > 0.8:
        print(">> 🔥 CRITICAL FLUX SURGE: Trigger PRIMITIVE_QUICK_RISE at 100% amplitude.")
    elif intensity > 0.4:
        print(">> 🌊 STEADY NEURAL FLOW: Trigger rhythmic PRIMITIVE_LOW_TICK pulses.")
    else:
        print(">> 🌑 NEURAL STASIS: Background ripple only (Minimal haptics).")

    print("\n[Visual Shader Recommendation]")
    print(f">> Pulse Scale: {0.8 + 0.2 * intensity:.2f}")
    print(f">> Flow Speed: {1.0 + intensity * 2.0:.1f}x")
    print("-" * 50)

if __name__ == "__main__":
    main()
