import math
import random

class GhostSparkSimulator:
    """
    GhostSparkSimulator: Python logic parity for the Neural Particle System.

    This simulator models 'Social Contagion' via discrete data particles (Sparks)
    that react to student energy nodes.
    """

    def __init__(self, canvas_size=4000):
        self.canvas_size = canvas_size
        self.sparks = []
        self.max_sparks = 300

    class Spark:
        def __init__(self, x, y, vx, vy, life, color_type, size):
            self.x = x
            self.y = y
            self.vx = vx
            self.vy = vy
            self.life = life
            self.color_type = color_type
            self.size = size

    def emit(self, x, y, behavior_type):
        """
        Emits a burst of particles based on behavior type.
        """
        color_type = 0 # Positive
        if "Negative" in behavior_type:
            color_type = 1
        elif "Academic" not in behavior_type:
            color_type = 2

        burst_count = 15
        for _ in range(burst_count):
            if len(self.sparks) < self.max_sparks:
                angle = random.uniform(0, 2 * math.pi)
                speed = random.uniform(5, 20)
                self.sparks.append(self.Spark(
                    x=x,
                    y=y,
                    vx=math.cos(angle) * speed,
                    vy=math.sin(angle) * speed,
                    life=1.0,
                    color_type=color_type,
                    size=random.uniform(4, 16)
                ))

    def update(self, students, delta_time=1.0):
        """
        Updates particle physics with social gravity attraction.
        """
        new_sparks = []
        for spark in self.sparks:
            # 1. Apply Friction
            spark.vx *= 0.96
            spark.vy *= 0.96

            # 2. Social Gravity (Attraction to students)
            for student in students:
                # student = {"id": id, "x": x, "y": y}
                dx = student['x'] - spark.x
                dy = student['y'] - spark.y
                dist_sq = dx*dx + dy*dy

                if 400.0 < dist_sq < 900000.0:
                    dist = math.sqrt(dist_sq)
                    force = (2.5 / dist) * delta_time
                    spark.vx += (dx / dist) * force
                    spark.vy += (dy / dist) * force

            # 3. Movement
            spark.x += spark.vx * delta_time
            spark.y += spark.vy * delta_time

            # 4. Life Decay
            spark.life -= 0.008 * delta_time

            # 5. Bounds Check
            if spark.x < 0 or spark.x > self.canvas_size: spark.vx *= -0.5
            if spark.y < 0 or spark.y > self.canvas_size: spark.vy *= -0.5

            if spark.life > 0:
                new_sparks.append(spark)

        self.sparks = new_sparks

def demo_simulation():
    """
    Runs a minimal demo of the spark simulation.
    """
    sim = GhostSparkSimulator()
    students = [{"id": 1, "x": 1000, "y": 1000}, {"id": 2, "x": 3000, "y": 3000}]

    print(f"Emitting sparks at (500, 500)...")
    sim.emit(500, 500, "Positive Participation")

    print(f"Initial spark count: {len(sim.sparks)}")

    # Run 50 frames of simulation
    for i in range(50):
        sim.update(students)
        if i % 10 == 0:
            avg_x = sum(s.x for s in sim.sparks) / len(sim.sparks) if sim.sparks else 0
            print(f"Frame {i}: Count={len(sim.sparks)}, AvgX={avg_x:.2f}")

if __name__ == "__main__":
    demo_simulation()
