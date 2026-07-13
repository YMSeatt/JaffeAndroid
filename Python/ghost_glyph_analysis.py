import math

def resample(points, n):
    """Simplifies a list of (x, y) points into exactly n points."""
    if not points:
        return []

    def get_dist(p1, p2):
        return math.sqrt((p2[0] - p1[0])**2 + (p2[1] - p1[1])**2)

    path_length = sum(get_dist(points[i], points[i+1]) for i in range(len(points)-1))
    interval = path_length / (n - 1)
    resampled = [points[0]]

    D = 0.0
    i = 1
    while i < len(points):
        d = get_dist(points[i-1], points[i])
        if D + d >= interval:
            ratio = (interval - D) / d
            q = (points[i-1][0] + (points[i][0] - points[i-1][0]) * ratio,
                 points[i-1][1] + (points[i][1] - points[i-1][1]) * ratio)
            resampled.append(q)
            points.insert(i, q) # Insert the new point to continue correctly
            D = 0.0
        else:
            D += d
        i += 1

    if len(resampled) < n:
        resampled.append(points[-1])
    return resampled[:n]

def get_direction_sequence(points):
    """Maps points to an 8-point compass sequence (0: E, 1: SE, ... 7: NE)."""
    directions = []
    for i in range(len(points)-1):
        a, b = points[i], points[i+1]
        angle = math.atan2(b[1] - a[1], b[0] - a[0]) * 180 / math.pi
        normalized = (angle + 360) % 360
        direction = int((normalized + 22.5) / 45) % 8
        if not directions or directions[-1] != direction:
            directions.append(direction)
    return directions

def is_closed(directions):
    """Heuristic for determining if a gesture forms a closed loop."""
    turn_sum = 0
    for i in range(len(directions)-1):
        diff = directions[i+1] - directions[i]
        if diff > 4: diff -= 8
        if diff < -4: diff += 8
        turn_sum += diff
    return abs(turn_sum) >= 4

def identify_glyph(points):
    """Recognizes a glyph from point data."""
    if len(points) < 10:
        return "UNKNOWN"

    simplified = resample(points, 20)
    directions = get_direction_sequence(simplified)

    # Positive: SE-ish then NE-ish
    if 2 <= len(directions) <= 3:
        # Check for downward-then-upward transition
        # Directions are 0:E, 1:SE, 2:S, 3:SW, 4:W, 5:NW, 6:N, 7:NE
        if directions[0] in [0, 1, 2] and directions[-1] in [6, 7, 0]:
            return "POSITIVE"

    # Academic: 3+ directions forming a loop
    if len(directions) >= 3 and is_closed(directions):
        # Additional triangle check: check for roughly 3 main direction shifts
        if 3 <= len(directions) <= 5:
            return "ACADEMIC"
        return "NEGATIVE" # Looping / Complex

    if is_closed(directions):
        return "ACADEMIC"

    return "UNKNOWN"

if __name__ == "__main__":
    # Test cases:
    # Checkmark (V) - More points to meet the minimum requirement
    checkmark = [(float(i), float(i)) for i in range(10)] + [(10.0 + float(i), 10.0 - float(i)) for i in range(1, 10)]
    print(f"Checkmark (V): {identify_glyph(checkmark)}")

    # Triangle (▲) - More points to meet the minimum requirement
    triangle = ([(float(i), float(i)) for i in range(10)] +
                [(10.0 - float(i), 10.0) for i in range(1, 10)] +
                [(0.0, 10.0 - float(i)) for i in range(1, 11)])
    print(f"Triangle: {identify_glyph(triangle)}")
