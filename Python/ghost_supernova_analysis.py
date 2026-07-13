import json
import math
import time

class GhostSupernovaAnalysis:
    """
    GhostSupernovaAnalysis: Python blueprint for classroom criticality and
    supernova trajectory simulation.

    Parity with GhostSupernovaEngine.kt.
    """

    def __init__(self, student_count=20):
        self.student_count = student_count
        self.pressure = 0.0
        self.stage = "IDLE"  # IDLE, CONTRACTION, EXPLOSION, NEBULA
        self.progress = 0.0

    def calculate_criticality(self, log_count, negative_ratio):
        """
        Calculates the 'Supernova Criticality' based on behavioral logs.
        """
        # logCount / (studentCount * 5.0)
        base = log_count / (max(1, self.student_count) * 5.0)

        # stressMultiplier = 1.0 + (negativeRatio * 2.0)
        stress_multiplier = 1.0 + (negative_ratio * 2.0)

        return min(1.0, base * stress_multiplier)

    def analyze_logs(self, logs_json):
        """
        Analyzes a set of behavioral logs to determine the classroom pressure.
        """
        logs = json.loads(logs_json)
        if not logs:
            return 0.0

        # In a real scenario, we'd filter for the last 15 minutes
        # Here we just count types
        total_logs = len(logs)
        negative_logs = sum(1 for log in logs if "Negative" in log.get("type", ""))
        negative_ratio = negative_logs / total_logs if total_logs > 0 else 0.0

        pressure = self.calculate_criticality(total_logs, negative_ratio)
        return pressure

    def simulate_lifecycle(self, steps=100):
        """
        Simulates the four stages of a supernova for plotting/debugging.
        """
        trajectory = []

        # Stage 1: Contraction (25 steps)
        for i in range(25):
            trajectory.append({"stage": "CONTRACTION", "progress": i / 25.0, "intensity": 1.0 - (i / 25.0)})

        # Stage 2: Explosion (25 steps)
        for i in range(25):
            trajectory.append({"stage": "EXPLOSION", "progress": i / 25.0, "intensity": i / 25.0})

        # Stage 3: Nebula (50 steps)
        for i in range(50):
            trajectory.append({"stage": "NEBULA", "progress": i / 50.0, "intensity": 0.5 * (1.0 - (i / 50.0))})

        return trajectory

def main():
    analyzer = GhostSupernovaAnalysis(student_count=25)

    # Mock logs: High friction
    mock_logs = [
        {"type": "Negative behavior"},
        {"type": "Negative behavior"},
        {"type": "Positive Participation"},
        {"type": "Negative behavior"},
        {"type": "Negative behavior"}
    ]

    pressure = analyzer.analyze_logs(json.dumps(mock_logs))
    print(f"[GHOST SUPERNOVA] Simulated Pressure: {pressure:.4f}")

    if pressure > 0.5:
        print("[CRITICAL] Classroom energy is unstable. Supernova imminent.")
    else:
        print("[STABLE] Nominal classroom dynamics detected.")

if __name__ == "__main__":
    main()
