import time

def simulate_mirage_decay(initial_intensity, decay_rate, seconds):
    """
    Simulates the linear decay of a Ghost Mirage focus cell.

    Formula: I_new = max(0, I_old - (decay_rate * delta_time))
    """
    intensity = initial_intensity
    print(f"Starting Intensity: {intensity:.2f}")
    print(f"Decay Rate: {decay_rate:.2f} units/sec")
    print("-" * 30)

    for s in range(1, seconds + 1):
        intensity = max(0.0, intensity - decay_rate)
        print(f"After {s}s: {intensity:.2f}")
        if intensity <= 0:
            print("Focus fully decayed.")
            break

def verify_coordinate_mapping(x, y, grid_size=20):
    """
    Verifies the mapping from 4000x4000 logical canvas to 20x20 grid index.
    """
    col = int((x / 4000.0) * grid_size)
    row = int((y / 4000.0) * grid_size)
    index = row * grid_size + col
    print(f"Logical Coord: ({x}, {y})")
    print(f"Grid Cell: [Row {row}, Col {col}]")
    print(f"Flattened Index: {index}")
    return index

if __name__ == "__main__":
    print("=== Ghost Mirage Logic Verification ===")
    simulate_mirage_decay(initial_intensity=0.8, decay_rate=0.05, seconds=20)
    print("\n=== Coordinate Mapping Verification ===")
    verify_coordinate_mapping(2000, 2000) # Center
    verify_coordinate_mapping(0, 0)       # Top Left
    verify_coordinate_mapping(3999, 3999) # Bottom Right
