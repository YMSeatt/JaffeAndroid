"""
Ghost Osmosis Analyzer: Classroom Knowledge Diffusion & Behavioral Concentration.

This script provides a desktop-side analysis of classroom 'Osmotic Balance'.
It calculates the diffusion of academic performance and behavior across the
classroom layout, identifying high-potential 'Diffusion Zones' and 'Social Pressure' points.

Logic parity with GhostOsmosisEngine.kt (Android).
"""

import math

def calculate_student_potentials(behavior_logs, quiz_logs, homework_logs):
    """
    Calculates knowledge potential and behavior concentration for a student.

    Parity with GhostOsmosisEngine.calculateStudentPotentials
    """
    # Knowledge Potential (0..1)
    if not quiz_logs and not homework_logs:
        k_potential = 0.5
    else:
        q_avg = sum(q['mark'] / q['max_mark'] for q in quiz_logs) / len(quiz_logs) if quiz_logs else 0.5
        h_done = sum(1 for h in homework_logs if 'Done' in h['status'])
        h_avg = h_done / len(homework_logs) if homework_logs else 0.5
        k_potential = (q_avg + h_avg) / 2.0

    # Behavior Concentration (-1..1)
    if not behavior_logs:
        b_concentration = 0.0
    else:
        pos = sum(1 for b in behavior_logs if 'Negative' not in b['type'])
        neg = sum(1 for b in behavior_logs if 'Negative' in b['type'])
        b_concentration = (pos - neg) / len(behavior_logs)

    return max(0.0, min(1.0, k_potential)), max(-1.0, min(1.0, b_concentration))

def analyze_osmotic_balance(students, diffusion_radius=1000.0):
    """
    Analyzes the overall classroom osmotic balance.

    :param students: List of dicts with 'x', 'y', 'k' (knowledge), 'b' (behavior)
    :return: Analysis report dict
    """
    if not students:
        return {"status": "VOID", "balance_score": 0.0}

    total_diffusion = 0.0
    interactions = 0

    for i in range(len(students)):
        for j in range(i + 1, len(students)):
            s1 = students[i]
            s2 = students[j]

            dx = s1['x'] - s2['x']
            dy = s1['y'] - s2['y']
            dist = math.sqrt(dx*dx + dy*dy)

            if dist < diffusion_radius:
                # Calculate 'Osmotic Pressure' (difference in potential)
                k_diff = abs(s1['k'] - s2['k'])
                b_diff = abs(s1['b'] - s2['b'])

                # Weight by proximity (Gaussian)
                weight = math.exp(-(dist**2) / (2 * 400.0**2))
                total_diffusion += (k_diff + b_diff) * weight
                interactions += 1

    avg_diffusion = total_diffusion / interactions if interactions > 0 else 0.0

    # Balance Score: Lower diffusion delta indicates a more 'balanced' classroom
    balance_score = max(0.0, 1.0 - (avg_diffusion * 2.0))

    status = "STABLE"
    if balance_score > 0.8:
        status = "EQUILIBRIUM"
    elif balance_score < 0.4:
        status = "HIGH_GRADIENT"

    return {
        "status": status,
        "balance_score": round(balance_score, 2),
        "total_interactions": interactions,
        "avg_diffusion_delta": round(avg_diffusion, 3)
    }

if __name__ == "__main__":
    # PoC Demo Data
    mock_students = [
        {'id': 1, 'x': 500, 'y': 500, 'k': 0.9, 'b': 0.8},  # High performing mentor
        {'id': 2, 'x': 600, 'y': 600, 'k': 0.4, 'b': 0.1},  # Nearby student (Diffusion zone)
        {'id': 3, 'x': 3000, 'y': 3000, 'k': 0.7, 'b': -0.5} # Isolated student with friction
    ]

    results = analyze_osmotic_balance(mock_students)
    print("--- Ghost Osmosis Analysis ---")
    print(f"Classroom Status: {results['status']}")
    print(f"Osmotic Balance: {results['balance_score'] * 100}%")
    print(f"Active Diffusion Zones: {results['total_interactions']}")
    print(f"Avg Diffusion Delta: {results['avg_diffusion_delta']}")
