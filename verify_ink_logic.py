
import unittest
from dataclasses import dataclass
from typing import List

@dataclass
class Offset:
    x: float
    y: float

class GhostInkEngine:
    @dataclass
    class Stroke:
        points: List[Offset]

    def __init__(self):
        self.strokes = []
        self.current_stroke_points = []

    def start_stroke(self, point: Offset):
        self.current_stroke_points = [point]

    def continue_stroke(self, point: Offset):
        if not self.current_stroke_points:
            self.current_stroke_points = [point]
            return

        last_point = self.current_stroke_points[-1]
        distance_sq = (point.x - last_point.x)**2 + (point.y - last_point.y)**2

        if distance_sq > 25.0: # 5-pixel logical distance
            self.current_stroke_points.append(point)

    def finish_stroke(self):
        if len(self.current_stroke_points) > 1:
            self.strokes.append(self.Stroke(points=list(self.current_stroke_points)))
        self.current_stroke_points = []

class TestGhostInkLogic(unittest.TestCase):
    def test_coordinate_transformation(self):
        # Simulation of logical space mapping: logical = (screen - offset) / scale
        canvas_scale = 2.0
        canvas_offset = Offset(100, 50)

        screen_points = [Offset(100, 50), Offset(200, 150), Offset(105, 55)]

        engine = GhostInkEngine()

        # Point 1: (100, 50) -> logical (0, 0)
        p1 = Offset((screen_points[0].x - canvas_offset.x) / canvas_scale,
                    (screen_points[0].y - canvas_offset.y) / canvas_scale)
        self.assertEqual(p1.x, 0.0)
        self.assertEqual(p1.y, 0.0)
        engine.start_stroke(p1)

        # Point 2: (200, 150) -> logical (50, 50)
        p2 = Offset((screen_points[1].x - canvas_offset.x) / canvas_scale,
                    (screen_points[1].y - canvas_offset.y) / canvas_scale)
        self.assertEqual(p2.x, 50.0)
        self.assertEqual(p2.y, 50.0)
        engine.continue_stroke(p2)

        # Point 3: (105, 55) -> logical (2.5, 2.5)
        # Dist from (50, 50) to (2.5, 2.5) is large, should be added
        p3 = Offset((screen_points[2].x - canvas_offset.x) / canvas_scale,
                    (screen_points[2].y - canvas_offset.y) / canvas_scale)
        engine.continue_stroke(p3)

        # Point 4: very close to P3 -> should be thinned out
        p4 = Offset(p3.x + 1, p3.y + 1)
        engine.continue_stroke(p4)

        engine.finish_stroke()

        self.assertEqual(len(engine.strokes), 1)
        self.assertEqual(len(engine.strokes[0].points), 3) # p1, p2, p3. p4 thinned.

if __name__ == "__main__":
    unittest.main()
