import time
import random

class MockBehaviorEvent:
    def __init__(self, type, timestamp):
        self.type = type
        self.timestamp = timestamp

class MockQuizLog:
    def __init__(self, timestamp):
        self.timestamp = timestamp

class GhostWeatherEngineSim:
    def __init__(self):
        self.SPAWN_WINDOW_MS = 600000
        self.intensity = 0.1
        self.wind_force = 0.0
        self.current_mode = "RAIN"
        self.lightning_alpha = 0.0
        self.last_lightning_time = 0

    def update(self, behavior_logs, quiz_logs, now):
        # 1. Analyze Climate
        recent_count = 0
        positive_count = 0
        negative_count = 0

        for log in behavior_logs:
            if now - log.timestamp < self.SPAWN_WINDOW_MS:
                recent_count += 1
                if "Negative" in log.type:
                    negative_count += 1
                else:
                    positive_count += 1

        self.intensity = max(0.05, min(1.0, recent_count / 20.0))
        self.wind_force = (positive_count - negative_count) * 10.0

        if negative_count > positive_count * 2 and recent_count > 5:
            self.current_mode = "NEURAL_STORM"
        elif positive_count > recent_count * 0.8 and recent_count > 5:
            self.current_mode = "SNOW"
        else:
            self.current_mode = "RAIN"

        # 2. Lightning
        has_recent_quiz = any(now - q.timestamp < 60000 for q in quiz_logs)
        if has_recent_quiz and now - self.last_lightning_time > 5000:
            self.lightning_alpha = 1.0
            self.last_lightning_time = now

def test_weather_logic():
    print("--- Starting Ghost Weather Logic Verification ---")
    sim = GhostWeatherEngineSim()
    now = int(time.time() * 1000)

    # Test Case 1: Neutral/Low Activity
    sim.update([], [], now)
    print(f"Low Activity: Intensity={sim.intensity}, Mode={sim.current_mode}")
    assert sim.intensity == 0.05
    assert sim.current_mode == "RAIN"

    # Test Case 2: Positive Bias (Wind East + Snow)
    pos_logs = [MockBehaviorEvent("Positive", now - 1000) for _ in range(15)]
    sim.update(pos_logs, [], now)
    print(f"Positive Bias: Intensity={sim.intensity}, Wind={sim.wind_force}, Mode={sim.current_mode}")
    assert sim.wind_force == 150.0
    assert sim.current_mode == "SNOW"

    # Test Case 3: Negative Bias (Wind West + Storm)
    neg_logs = [MockBehaviorEvent("Negative", now - 1000) for _ in range(15)]
    sim.update(neg_logs, [], now)
    print(f"Negative Bias: Intensity={sim.intensity}, Wind={sim.wind_force}, Mode={sim.current_mode}")
    assert sim.wind_force == -150.0
    assert sim.current_mode == "NEURAL_STORM"

    # Test Case 4: Academic Lightning
    quizzes = [MockQuizLog(now - 1000)]
    sim.update([], quizzes, now)
    print(f"Academic Lightning: Alpha={sim.lightning_alpha}")
    assert sim.lightning_alpha == 1.0

    print("--- Verification Successful ---")

if __name__ == "__main__":
    test_weather_logic()
