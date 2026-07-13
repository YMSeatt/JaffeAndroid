import json
import math
from dataclasses import dataclass
from typing import List, Optional

@dataclass
class NeuralBasePair:
    type: str
    intensity: float
    timestamp: int

@dataclass
class HelixSequence:
    student_id: int
    base_pairs: List[NeuralBasePair]
    twist_rate: float
    stability: float

class GhostHelixAnalyzer:
    """
    Python parity for GhostHelixEngine.
    Provides deep genomic analysis of classroom neural data.
    """

    @staticmethod
    def sequence_data(student_id: int, behavior_logs: List[dict], quiz_logs: List[dict]) -> HelixSequence:
        base_pairs = []

        # 1. Behavior Sequencing
        for log in behavior_logs:
            is_negative = "Negative" in log.get("type", "")
            base_pairs.append(NeuralBasePair(
                type="THYMINE" if is_negative else "ADENINE",
                intensity=0.8,
                timestamp=log.get("timestamp", 0)
            ))

        # 2. Academic Sequencing
        for log in quiz_logs:
            mark = log.get("markValue", 0)
            max_mark = log.get("maxMarkValue", 1)
            ratio = mark / max_mark if max_mark > 0 else 0.7

            is_low = ratio < 0.6
            base_pairs.append(NeuralBasePair(
                type="GUANINE" if is_low else "CYTOSINE",
                intensity=max(0.1, min(1.0, float(ratio))),
                timestamp=log.get("loggedAt", 0)
            ))

        base_pairs.sort(key=lambda x: x.timestamp)

        # Global Parameters
        negative_count = sum(1 for bp in base_pairs if bp.type in ["THYMINE", "GUANINE"])
        total_count = max(1, len(base_pairs))
        stability = max(0.1, min(1.0, 1.0 - (negative_count / total_count)))

        academic_activity = sum(1 for bp in base_pairs if bp.type in ["CYTOSINE", "GUANINE"])
        twist_rate = 1.0 + min(2.0, academic_activity / 10.0)

        return HelixSequence(student_id, base_pairs, twist_rate, stability)

    @staticmethod
    def analyze_genomic_drift(sequence: HelixSequence) -> float:
        """ Calculates the trajectory score (0..1) """
        if not sequence.base_pairs:
            return 0.5

        score = 0.0
        weights = {
            "ADENINE": 1.0,
            "CYTOSINE": 0.8,
            "THYMINE": -1.0,
            "GUANINE": -0.6
        }

        for bp in sequence.base_pairs:
            score += weights.get(bp.type, 0.0) * bp.intensity

        normalized = 0.5 + (score / len(sequence.base_pairs)) * 0.5
        return max(0.0, min(1.0, normalized))

# Example usage
if __name__ == "__main__":
    mock_behavior = [{"type": "Talking (Negative)", "timestamp": 1600000000}]
    mock_quiz = [{"markValue": 8.0, "maxMarkValue": 10.0, "loggedAt": 1600000010}]

    analyzer = GhostHelixAnalyzer()
    seq = analyzer.sequence_data(101, mock_behavior, mock_quiz)
    drift = analyzer.analyze_genomic_drift(seq)

    print(f"Student 101 DNA Stability: {seq.stability:.2f}")
    print(f"Neural Trajectory: {drift:.2f}")
