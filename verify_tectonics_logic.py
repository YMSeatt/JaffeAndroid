import math

def calculate_tectonic_stress(students, behavior_logs):
    STRESS_RADIUS = 600.0
    NEGATIVE_LOG_WEIGHT = 0.15
    STRESS_RADIUS_SQ = STRESS_RADIUS * STRESS_RADIUS

    # 1. Base stress from individual negative logs
    base_stresses = {}
    negative_counts = {}
    for log in behavior_logs:
        if 'negative' in log['type'].lower():
            s_id = log['student_id']
            negative_counts[s_id] = negative_counts.get(s_id, 0) + 1

    for s in students:
        s_id = s['id']
        count = negative_counts.get(s_id, 0)
        base_stresses[s_id] = min(count * NEGATIVE_LOG_WEIGHT, 0.5)

    # 2. Proximity stress
    results = []
    for s1 in students:
        s1_id = s1['id']
        proximity_stress = 0.0
        for s2 in students:
            s2_id = s2['id']
            if s1_id == s2_id:
                continue

            dx = s1['x'] - s2['x']
            dy = s1['y'] - s2['y']
            dist_sq = dx*dx + dy*dy

            if dist_sq < STRESS_RADIUS_SQ:
                dist = math.sqrt(dist_sq)
                proximity_stress += (base_stresses[s2_id] * (1.0 - dist / STRESS_RADIUS)) * 0.5

        total_stress = min(max(base_stresses[s1_id] + proximity_stress, 0.0), 1.0)
        results.append({
            'id': s1_id,
            'x': s1['x'],
            'y': s1['y'],
            'stress': total_stress
        })

    return results

def analyze_risk(nodes):
    STRESS_RADIUS_SQ = 600.0 * 600.0
    total_stress = 0.0
    peak_stress = 0.0
    fault_lines = 0

    for i in range(len(nodes)):
        n1 = nodes[i]
        total_stress += n1['stress']
        if n1['stress'] > peak_stress:
            peak_stress = n1['stress']

        for j in range(i + 1, len(nodes)):
            n2 = nodes[j]
            dx = n1['x'] - n2['x']
            dy = n1['y'] - n2['y']
            dist_sq = dx*dx + dy*dy

            if dist_sq < STRESS_RADIUS_SQ and n1['stress'] > 0.4 and n2['stress'] > 0.4:
                fault_lines += 1

    avg_stress = total_stress / len(nodes) if nodes else 0

    if peak_stress > 0.8 or fault_lines > 3:
        risk = "CRITICAL"
    elif peak_stress > 0.6 or fault_lines > 1:
        risk = "VOLATILE"
    elif avg_stress > 0.3:
        risk = "ACCUMULATING"
    else:
        risk = "STABLE"

    return avg_stress, peak_stress, fault_lines, risk

def test_tectonics():
    print("Running Ghost Tectonics Logic Verification...")

    # Scenario 1: Stable
    students_stable = [
        {'id': 1, 'x': 100, 'y': 100},
        {'id': 2, 'x': 1000, 'y': 1000}
    ]
    logs_stable = []
    nodes_stable = calculate_tectonic_stress(students_stable, logs_stable)
    avg, peak, fl, risk = analyze_risk(nodes_stable)
    assert risk == "STABLE"
    assert peak == 0.0
    print("[PASS] Scenario 1: Stable classroom.")

    # Scenario 2: Volatile Cluster
    # 3 students close together, each with 3 negative logs (base stress 0.45)
    students_volatile = [
        {'id': 1, 'x': 100, 'y': 100},
        {'id': 2, 'x': 200, 'y': 100},
        {'id': 3, 'x': 150, 'y': 200}
    ]
    logs_volatile = [
        {'student_id': 1, 'type': 'Negative'}, {'student_id': 1, 'type': 'Negative'}, {'student_id': 1, 'type': 'Negative'},
        {'student_id': 2, 'type': 'Negative'}, {'student_id': 2, 'type': 'Negative'}, {'student_id': 2, 'type': 'Negative'},
        {'student_id': 3, 'type': 'Negative'}, {'student_id': 3, 'type': 'Negative'}, {'student_id': 3, 'type': 'Negative'}
    ]
    nodes_volatile = calculate_tectonic_stress(students_volatile, logs_volatile)
    avg, peak, fl, risk = analyze_risk(nodes_volatile)

    # Base stress is 0.45. Proximity will push it higher.
    # student 1 should see students 2 and 3.
    assert peak > 0.6
    assert fl >= 1
    assert risk in ["VOLATILE", "CRITICAL"]
    print(f"[PASS] Scenario 2: Volatile cluster. Risk: {risk}, Fault Lines: {fl}, Peak: {peak:.2f}")

    # Scenario 3: Critical
    # One student with 6 negative logs (base stress capped at 0.5)
    # Plus heavy proximity from others.
    students_critical = [
        {'id': 1, 'x': 100, 'y': 100},
        {'id': 2, 'x': 150, 'y': 100},
        {'id': 3, 'x': 100, 'y': 150},
        {'id': 4, 'x': 150, 'y': 150}
    ]
    logs_critical = []
    for s_id in [1, 2, 3, 4]:
        for _ in range(5):
            logs_critical.append({'student_id': s_id, 'type': 'Negative'})

    nodes_critical = calculate_tectonic_stress(students_critical, logs_critical)
    avg, peak, fl, risk = analyze_risk(nodes_critical)
    assert risk == "CRITICAL"
    assert fl >= 3
    print(f"[PASS] Scenario 3: Critical classroom. Risk: {risk}, Fault Lines: {fl}, Peak: {peak:.2f}")

    print("\n✅ GHOST TECTONICS LOGIC VERIFIED.")

if __name__ == "__main__":
    test_tectonics()
