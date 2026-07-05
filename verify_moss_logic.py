import time

def calculate_dormancy_score(last_activity_ts, now_ts):
    MS_PER_DAY = 1000 * 60 * 60 * 24
    GROWTH_START_DAYS = 7
    MAX_GROWTH_DAYS = 21

    if last_activity_ts == 0:
        return 0.2

    days_inactive = (now_ts - last_activity_ts) / MS_PER_DAY

    if days_inactive < GROWTH_START_DAYS:
        return 0.0
    if days_inactive >= MAX_GROWTH_DAYS:
        return 1.0

    return (days_inactive - GROWTH_START_DAYS) / (MAX_GROWTH_DAYS - GROWTH_START_DAYS)

def test_moss_logic():
    now = int(time.time() * 1000)

    # Case 1: Active student (logged today)
    active_ts = now - (1 * 1000 * 60 * 60) # 1 hour ago
    assert calculate_dormancy_score(active_ts, now) == 0.0

    # Case 2: Just before moss starts (6 days)
    six_days_ts = now - (6 * 24 * 60 * 60 * 1000)
    assert calculate_dormancy_score(six_days_ts, now) == 0.0

    # Case 3: Moss starts (7 days)
    seven_days_ts = now - (7 * 24 * 60 * 60 * 1000)
    assert calculate_dormancy_score(seven_days_ts, now) == 0.0

    # Case 4: Mid-growth (14 days)
    # (14 - 7) / (21 - 7) = 7 / 14 = 0.5
    fourteen_days_ts = now - (14 * 24 * 60 * 60 * 1000)
    assert abs(calculate_dormancy_score(fourteen_days_ts, now) - 0.5) < 0.001

    # Case 5: Max growth (21 days)
    twenty_one_days_ts = now - (21 * 24 * 60 * 60 * 1000)
    assert calculate_dormancy_score(twenty_one_days_ts, now) == 1.0

    # Case 6: Beyond max growth (30 days)
    thirty_days_ts = now - (30 * 24 * 60 * 60 * 1000)
    assert calculate_dormancy_score(thirty_days_ts, now) == 1.0

    # Case 7: Never logged
    assert calculate_dormancy_score(0, now) == 0.2

    print("Ghost Moss Logic Parity: PASSED ✅")

if __name__ == "__main__":
    test_moss_logic()
