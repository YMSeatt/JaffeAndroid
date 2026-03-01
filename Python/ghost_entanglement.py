import math
import json
import random

"""
Ghost Entanglement: Quantum Social Synchronicity Analysis (Blueprint)
This script provides the mathematical foundation for calculating "Quantum Coherence"
between student nodes in a classroom environment.
"""

class GhostEntanglementAnalyzer:
    def __init__(self, students, behavior_logs):
        self.students = students
        self.behavior_logs = behavior_logs
        self.CANVAS_SIZE = 4000.0

    def calculate_coherence(self, student_a, student_b):
        """
        Calculates the coherence between two students based on spatial proximity,
        behavioral timing similarity, and academic parity.
        """
        # 1. Spatial Coherence (Proximity)
        dx = student_a['x'] - student_b['x']
        dy = student_a['y'] - student_b['y']
        dist = math.sqrt(dx*dx + dy*dy)
        spatial_coherence = math.exp(-(dist*dist) / (2 * 600**2))

        # 2. Behavioral Synchronicity (Mock timing)
        # In a real app, this would use timestamps from behavior_logs
        sync_factor = random.uniform(0.4, 0.9)

        # 3. Academic Parity (Mock assessment similarity)
        parity_factor = random.uniform(0.6, 1.0)

        # 4. Group Multiplier
        group_multiplier = 1.5 if student_a.get('group_id') == student_b.get('group_id') else 1.0

        coherence = (spatial_coherence * 0.4 + sync_factor * 0.3 + parity_factor * 0.3) * group_multiplier
        return min(1.0, coherence)

    def analyze_classroom_entanglement(self):
        """
        Generates a global entanglement matrix for the classroom.
        """
        matrix = []
        for i in range(len(self.students)):
            for j in range(i + 1, len(self.students)):
                coherence = self.calculate_coherence(self.students[i], self.students[j])
                if coherence > 0.7:
                    matrix.append({
                        "node_a": self.students[i]['id'],
                        "node_b": self.students[j]['id'],
                        "coherence": round(coherence, 3)
                    })
        return matrix

def main():
    # Mock data for blueprint verification
    students = [
        {"id": 1, "x": 500, "y": 500, "group_id": 10},
        {"id": 2, "x": 600, "y": 600, "group_id": 10},
        {"id": 3, "x": 2000, "y": 2000, "group_id": 20},
        {"id": 4, "x": 2100, "y": 2100, "group_id": 20}
    ]

    analyzer = GhostEntanglementAnalyzer(students, [])
    matrix = analyzer.analyze_classroom_entanglement()

    print("--- 👻 GHOST ENTANGLEMENT REPORT ---")
    print(f"Total Students: {len(students)}")
    print(f"Active Quantum Links: {len(matrix)}")
    print("\n[COHERENCE MATRIX]")
    for link in matrix:
        print(f"Link {link['node_a']} <-> {link['node_b']} | Coherence: {link['coherence']*100}%")

    avg_coherence = sum(l['coherence'] for l in matrix) / len(matrix) if matrix else 0
    print(f"\nGlobal Classroom Coherence: {round(avg_coherence * 100, 1)}%")

if __name__ == "__main__":
    main()
