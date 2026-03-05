"""
Ghost Emergence Analysis: Behavioral Cellular Automata
Analyzes emergent clusters in classroom data by simulating a vitality field
on a 10x10 grid, mirroring the logic in GhostEmergenceEngine.kt.
"""

import json
import math
import os

GRID_SIZE = 10
CANVAS_SIZE = 4000.0
DECAY_RATE = 0.95
DIFFUSION_RATE = 0.1

def analyze_emergence(data):
    students = data.get('students', [])
    behavior_logs = data.get('behavior_events', [])

    # 1. Initialize Grids
    vitality_grid = [0.0] * (GRID_SIZE * GRID_SIZE)
    impulses = [0.0] * (GRID_SIZE * GRID_SIZE)

    # 2. Process Behavior Events (Impulse)
    # Note: In a real simulation this would be time-stepped,
    # but here we aggregate recent logs for a "snapshot".
    for event in behavior_logs:
        student_id = event.get('studentId')
        student = next((s for s in students if s.get('id') == student_id), None)
        if not student:
            continue

        gx = int((student.get('xPosition', 0) / CANVAS_SIZE) * GRID_SIZE)
        gy = int((student.get('yPosition', 0) / CANVAS_SIZE) * GRID_SIZE)
        gx = max(0, min(GRID_SIZE - 1, gx))
        gy = max(0, min(GRID_SIZE - 1, gy))
        idx = gy * GRID_SIZE + gx

        weight = 0.05
        etype = event.get('type', '').lower()
        if 'positive' in etype:
            weight = 0.15
        elif 'negative' in etype:
            weight = -0.2

        impulses[idx] += weight

    # 3. Simulate Iterations (Cellular Automata)
    # We run 10 iterations to show "emergence" and diffusion
    for _ in range(10):
        next_grid = [0.0] * (GRID_SIZE * GRID_SIZE)
        for y in range(GRID_SIZE):
            for x in range(GRID_SIZE):
                idx = y * GRID_SIZE + x

                # Neighbor diffusion
                neighbors_sum = 0.0
                neighbors_count = 0
                for dy in [-1, 0, 1]:
                    for dx in [-1, 0, 1]:
                        if dx == 0 and dy == 0: continue
                        nx, ny = x + dx, y + dy
                        if 0 <= nx < GRID_SIZE and 0 <= ny < GRID_SIZE:
                            neighbors_sum += vitality_grid[ny * GRID_SIZE + nx]
                            neighbors_count += 1

                neighbor_avg = neighbors_sum / neighbors_count if neighbors_count > 0 else 0

                # Transition Rules
                current = vitality_grid[idx]
                diffused = current + (neighbor_avg - current) * DIFFUSION_RATE
                decayed = diffused * DECAY_RATE

                # Add impulses on the first iteration or spread them?
                # To match Android PoC behavior:
                val = decayed + impulses[idx]
                next_grid[idx] = max(-1.0, min(1.0, val))

        vitality_grid = next_grid

    return vitality_grid

def generate_report(vitality_grid):
    print("Ghost Emergence Analysis Report")
    print("===============================")

    pos_count = sum(1 for v in vitality_grid if v > 0.1)
    neg_count = sum(1 for v in vitality_grid if v < -0.1)
    peak_vitality = max(vitality_grid)
    void_vitality = min(vitality_grid)

    print(f"Emergent Clusters (Positive): {pos_count}")
    print(f"Emergent Voids (Negative): {neg_count}")
    print(f"Peak Vitality: {peak_vitality:.2f}")
    print(f"Maximum Entropy/Void: {void_vitality:.2f}")

    if pos_count > neg_count:
        print("Status: Growth Dominant")
    else:
        print("Status: Decay Warning")

if __name__ == "__main__":
    # Mock data for demonstration if run directly
    mock_data = {
        "students": [{"id": 1, "xPosition": 1000, "yPosition": 1000}],
        "behavior_events": [{"studentId": 1, "type": "Positive Participation"}]
    }
    grid = analyze_emergence(mock_data)
    generate_report(grid)
