import math
import time
import json
from dataclasses import dataclass
from typing import List, Dict

@dataclass
class OrbitalState:
    student_id: int
    x: float
    y: float
    center_x: float
    center_y: float
    angle: float
    speed: float
    radius: float
    energy: float
    stability: float

class GhostOrbitAnalysis:
    """
    GhostOrbitAnalysis: Logic parity blueprint for the Classroom Galaxy.
    Ported from GhostOrbitEngine.kt.
    """

    def __init__(self, students: List[Dict], behavior_logs: List[Dict]):
        self.students = students
        self.behavior_logs = behavior_logs
        self.current_time_ms = int(time.time() * 1000)
        self.window_ms = 3600000  # 1 hour window

    def calculate_orbits(self, elapsed_time_sec: float) -> List[OrbitalState]:
        if not self.students:
            return []

        current_time_ms = int(time.time() * 1000)

        # Group logs by student_id
        logs_by_student = {}
        for log in self.behavior_logs:
            sid = log.get('studentId')
            if sid not in logs_by_student:
                logs_by_student[sid] = []
            logs_by_student[sid].append(log)

        # Identify "Social Suns"
        suns = []
        for s in self.students:
            sid = s.get('id')
            logs = logs_by_student.get(sid, [])
            pos_count = sum(1 for log in logs if 'Negative' not in log.get('type', ''))
            if pos_count > 5:
                suns.append(s)

        orbital_states = []
        for s in self.students:
            sid = s.get('id')
            logs = logs_by_student.get(sid, [])
            recent_logs = [log for log in logs if current_time_ms - log.get('timestamp', 0) < self.window_ms]

            pos_count = sum(1 for log in logs if 'Negative' not in log.get('type', ''))
            total_count = len(logs)

            # Engagement drives Speed
            engagement = min(2.0, max(0.1, len(recent_logs) / 5.0))
            base_speed = 0.5 * engagement

            # Stability drives Radius
            stability = min(1.0, max(0.0, pos_count / total_count)) if total_count > 0 else 0.8
            base_radius = 150.0 + (1.0 - stability) * 300.0

            # Energy
            energy = min(1.0, max(0.2, total_count / 10.0))

            # Center calculation
            center_x, center_y = 2000.0, 2000.0
            other_suns = [sun for sun in suns if sun.get('id') != sid]
            if other_suns:
                nearest_sun = min(other_suns, key=lambda sun: (sun.get('xPosition', 0) - s.get('xPosition', 0))**2 + (sun.get('yPosition', 0) - s.get('yPosition', 0))**2)
                center_x = nearest_sun.get('xPosition', 2000.0)
                center_y = nearest_sun.get('yPosition', 2000.0)

            angle = (elapsed_time_sec * base_speed + (sid * 0.785)) % (2.0 * math.pi)
            orbital_x = center_x + math.cos(angle) * base_radius
            orbital_y = center_y + math.sin(angle) * base_radius

            orbital_states.append(OrbitalState(
                student_id=sid,
                x=orbital_x,
                y=orbital_y,
                center_x=center_x,
                center_y=center_y,
                angle=angle,
                speed=base_speed,
                radius=base_radius,
                energy=energy,
                stability=stability
            ))

        return orbital_states

    def generate_report(self) -> str:
        states = self.calculate_orbits(0)
        report = ["# 👻 GHOST ORBIT: CLASSROOM GALAXY ANALYSIS", ""]
        report.append("| Student | Orbital State | Stability | Engagement |")
        report.append("| :--- | :--- | :--- | :--- |")

        for state in states:
            student_name = next((s.get('firstName', 'Unknown') for s in self.students if s.get('id') == state.student_id), 'Unknown')
            status = "STABLE" if state.stability > 0.7 else "DECAYING"
            report.append(f"| {student_name} | {state.radius:.1f}m Radius | {state.stability:.2%} | {state.speed:.2f}x |")

        return "\n".join(report)

if __name__ == "__main__":
    # Mock data for parity testing
    mock_students = [
        {"id": 1, "firstName": "Alice", "xPosition": 1000, "yPosition": 1000},
        {"id": 2, "firstName": "Bob", "xPosition": 3000, "yPosition": 3000}
    ]
    mock_logs = [
        {"studentId": 1, "type": "Positive Participation", "timestamp": int(time.time() * 1000)},
        {"studentId": 1, "type": "Positive Participation", "timestamp": int(time.time() * 1000)},
        {"studentId": 2, "type": "Negative behavior", "timestamp": int(time.time() * 1000)}
    ]

    analyzer = GhostOrbitAnalysis(mock_students, mock_logs)
    print(analyzer.generate_report())
