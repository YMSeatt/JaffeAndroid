import time

def calculate_resilience(logs):
    # Constants matching GhostPhoenixEngine.kt
    RECOVERY_WINDOW_MS = 2 * 60 * 60 * 1000
    STRUGGLE_WINDOW_MS = 24 * 60 * 60 * 1000

    now = int(time.time() * 1000)
    recovery_cutoff = now - RECOVERY_WINDOW_MS
    struggle_cutoff = now - STRUGGLE_WINDOW_MS

    recent_positive_count = 0
    historical_negative_count = 0

    for log in logs:
        ts = log['timestamp']
        if ts < struggle_cutoff:
            continue

        is_negative = "Negative" in log['type']
        is_positive = "Positive" in log['type']

        if ts >= recovery_cutoff:
            if is_positive:
                recent_positive_count += 1
        else:
            if is_negative:
                historical_negative_count += 1

    if historical_negative_count > 0:
        score = (recent_positive_count * 0.2) + (historical_negative_count * 0.1)
        return min(1.0, max(0.0, score))
    else:
        return 0.0

# Test Cases
now = int(time.time() * 1000)
one_hour_ago = now - (60 * 60 * 1000)
six_hours_ago = now - (6 * 60 * 60 * 1000)

print("Running Ghost Phoenix Resilience Simulation...")

# 1. High Resilience: Student with historical struggle and recent recovery
logs_resilient = [
    {'type': 'Negative Behavior', 'timestamp': six_hours_ago},
    {'type': 'Negative Behavior', 'timestamp': six_hours_ago - 1000},
    {'type': 'Positive Behavior', 'timestamp': one_hour_ago},
    {'type': 'Positive Behavior', 'timestamp': one_hour_ago + 1000},
    {'type': 'Positive Behavior', 'timestamp': one_hour_ago + 2000}
]
score1 = calculate_resilience(logs_resilient)
print(f"Resilient Student Score: {score1:.2f} (Expected: ~0.80)")

# 2. Low Resilience: Student with no historical struggle
logs_just_good = [
    {'type': 'Positive Behavior', 'timestamp': one_hour_ago},
    {'type': 'Positive Behavior', 'timestamp': one_hour_ago + 1000}
]
score2 = calculate_resilience(logs_just_good)
print(f"Consistently Good Student Score: {score2:.2f} (Expected: 0.00)")

# 3. Moderate Resilience: Minor struggle, minor recovery
logs_moderate = [
    {'type': 'Negative Behavior', 'timestamp': six_hours_ago},
    {'type': 'Positive Behavior', 'timestamp': one_hour_ago}
]
score3 = calculate_resilience(logs_moderate)
print(f"Moderate Resilience Score: {score3:.2f} (Expected: 0.30)")

if score1 > 0.6 and score2 == 0.0:
    print("\n✅ Simulation Passed: Resilience logic aligns with Ghost R&D standards.")
else:
    print("\n❌ Simulation Failed: Logic mismatch detected.")
