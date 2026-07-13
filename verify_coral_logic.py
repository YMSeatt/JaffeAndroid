import math

def calculate_social_reef(students, behavior_logs):
    PROXIMITY_THRESHOLD = 800
    PROXIMITY_THRESHOLD_SQ = PROXIMITY_THRESHOLD ** 2

    # 1. Individual Calcification Potential
    calcification_map = {}
    for s in students:
        sid = s['id']
        logs = behavior_logs.get(sid, [])

        positive_count = 0
        for log in logs:
            if is_positive(log['type']):
                positive_count += 1

        if positive_count > 0:
            calcification_map[sid] = min(positive_count / 5.0, 1.0)

    branches = []

    # 2. Spatial Pairing
    for i in range(len(students)):
        s1 = students[i]
        c1 = calcification_map.get(s1['id'], 0.0)

        for j in range(i + 1, len(students)):
            s2 = students[j]
            c2 = calcification_map.get(s2['id'], 0.0)

            dx = s1['x'] - s2['x']
            dy = s1['y'] - s2['y']
            dist_sq = dx*dx + dy*dy

            if dist_sq < PROXIMITY_THRESHOLD_SQ:
                base_density = (c1 + c2) / 2.0
                vitality = 1.0 - abs(c1 - c2)

                spatial_weight = math.exp(-dist_sq / (2 * PROXIMITY_THRESHOLD_SQ / 9.0))
                effective_density = base_density * spatial_weight

                if effective_density > 0.05:
                    branches.append({
                        'idA': s1['id'],
                        'idB': s2['id'],
                        'density': effective_density,
                        'vitality': vitality
                    })
    return branches

def is_positive(behavior_type):
    lower = behavior_type.lower()
    pos_keywords = ["positive", "participation", "great", "good", "excellent", "leadership", "helpful"]
    return any(k in lower for k in pos_keywords)

# Test Simulation
def test_coral_logic():
    students = [
        {'id': 1, 'x': 1000, 'y': 1000}, # Highly positive
        {'id': 2, 'x': 1200, 'y': 1000}, # Also positive, close to 1
        {'id': 3, 'x': 3000, 'y': 3000}, # Positive but far away
        {'id': 4, 'x': 1100, 'y': 1100}, # No logs, close to 1 and 2
    ]

    behavior_logs = {
        1: [{'type': 'Positive Participation'}] * 5,
        2: [{'type': 'Great Job'}] * 3,
        3: [{'type': 'Excellent'}] * 10,
    }

    branches = calculate_social_reef(students, behavior_logs)

    print(f"Total branches detected: {len(branches)}")
    for b in branches:
        print(f"Link {b['idA']}-{b['idB']}: Density={b['density']:.2f}, Vitality={b['vitality']:.2f}")

    # Assertions
    assert len(branches) > 0, "Should detect at least one branch"
    # Link 1-2 should exist
    link_1_2 = next((b for b in branches if (b['idA']==1 and b['idB']==2)), None)
    assert link_1_2 is not None, "Link 1-2 should exist"
    assert link_1_2['density'] > 0.5, f"Link 1-2 should have high density, got {link_1_2['density']}"

    # Link 1-4 should exist because 4 is close, but have lower density since 4 has no logs
    link_1_4 = next((b for b in branches if (b['idA']==1 and b['idB']==4)), None)
    assert link_1_4 is not None, "Link 1-4 should exist due to proximity and 1's potential"
    assert link_1_4['density'] < link_1_2['density'], "Link 1-4 should be weaker than 1-2"

    print("✅ Ghost Coral Logic Verified.")

if __name__ == "__main__":
    test_coral_logic()
