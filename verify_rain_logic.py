import math

def simulate_rain_physics(drop_y, drop_vel, dt, gravity):
    new_y = drop_y + drop_vel * dt * 100.0
    new_vel = drop_vel + gravity * dt
    return new_y, new_vel

def check_intersection(drop_x, drop_y, sx, sy, sw, sh):
    return (drop_x >= sx and drop_x <= sx + sw and
            drop_y >= sy and drop_y <= sy + sh)

def test_rain_logic():
    # Scenario 1: Falling Droplet
    y, vel = simulate_rain_physics(0.0, 20.0, 0.016, 15.0)
    print(f"Scenario 1 (Falling): y={y:.2f}, vel={vel:.2f}")
    assert y > 0.0
    assert vel > 20.0

    # Scenario 2: Intersection Hit
    hit = check_intersection(500, 500, 450, 450, 100, 100)
    print(f"Scenario 2 (Hit): {hit}")
    assert hit == True

    # Scenario 3: Intersection Miss
    miss = check_intersection(100, 100, 450, 450, 100, 100)
    print(f"Scenario 3 (Miss): {miss}")
    assert miss == False

    # Scenario 4: Intensity Calculation (Simulated)
    logs = [
        {"timestamp": 1000},
        {"timestamp": 2000},
        {"timestamp": 3000}
    ]
    now = 5000
    window = 10000
    activity_count = sum(1 for log in logs if now - log["timestamp"] < window)
    print(f"Scenario 4 (Activity): {activity_count}")
    assert activity_count == 3

    print("Ghost Rain logic verification PASSED 👻")

if __name__ == "__main__":
    test_rain_logic()
