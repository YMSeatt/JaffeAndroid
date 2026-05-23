import math

def calculate_cosine_similarity(vec_a, vec_b):
    dot_product = 0.0
    norm_a = 0.0
    for key, v in vec_a.items():
        norm_a += v * v
        v_b = vec_b.get(key, 0.0)
        dot_product += v * v_b

    norm_b = 0.0
    for key, v in vec_b.items():
        norm_b += v * v

    if norm_a == 0.0 or norm_b == 0.0:
        return 0.0

    return dot_product / (math.sqrt(norm_a) * math.sqrt(norm_b))

def test_carbon_logic():
    # Scenario 1: Identical vectors
    v1 = {"Talking": 5, "Helping": 10, "Great Participation": 2}
    v2 = {"Talking": 5, "Helping": 10, "Great Participation": 2}
    sim1 = calculate_cosine_similarity(v1, v2)
    print(f"Scenario 1 (Identical): {sim1:.4f}")
    assert sim1 >= 0.9999

    # Scenario 2: Proportional vectors
    v3 = {"Talking": 10, "Helping": 20, "Great Participation": 4}
    sim2 = calculate_cosine_similarity(v1, v3)
    print(f"Scenario 2 (Proportional): {sim2:.4f}")
    assert sim2 >= 0.9999

    # Scenario 3: Slightly different
    v4 = {"Talking": 5, "Helping": 11, "Great Participation": 2}
    sim3 = calculate_cosine_similarity(v1, v4)
    print(f"Scenario 3 (Slightly Different): {sim3:.4f}")
    assert 0.95 <= sim3 < 1.0

    # Scenario 4: Very different
    v5 = {"Talking": 0, "Helping": 0, "Great Participation": 10}
    sim4 = calculate_cosine_similarity(v1, v5)
    print(f"Scenario 4 (Very Different): {sim4:.4f}")
    assert sim4 < 0.5

    print("Carbon logic verification PASSED 👻")

if __name__ == "__main__":
    test_carbon_logic()
