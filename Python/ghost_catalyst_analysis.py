import json
import sys
import time
import math

def analyze_catalyst_kinetics(student_data, events):
    """
    Performs macroscopic kinetics analysis on classroom behavioral data.

    This script identifies 'Social Catalysts' by detecting chain reactions
    (sequential logs in spatial proximity) and calculating reaction rates.

    Args:
        student_data (list): List of students with x, y coordinates.
        events (list): List of behavior events with student_id and timestamp.

    Returns:
        dict: Analysis results including Reaction Rate and Activation Energy.
    """
    radius = 800
    time_window = 300  # 5 minutes in seconds

    reactions = []

    # Sort events by timestamp
    sorted_events = sorted(events, key=lambda e: e['timestamp'])

    # Map student IDs to positions for O(1) lookup
    student_pos = {s['id']: (s['x'], s['y']) for s in student_data}

    for i in range(len(sorted_events)):
        catalyst = sorted_events[i]
        c_id = catalyst['student_id']
        if c_id not in student_pos: continue
        c_x, c_y = student_pos[c_id]

        for j in range(i + 1, len(sorted_events)):
            reactant = sorted_events[j]
            r_id = reactant['student_id']

            # Temporal Window
            if reactant['timestamp'] > catalyst['timestamp'] + time_window:
                break

            if c_id == r_id: continue
            if r_id not in student_pos: continue

            r_x, r_y = student_pos[r_id]

            # Spatial Distance
            dist = math.sqrt((c_x - r_x)**2 + (c_y - r_y)**2)

            if dist < radius:
                reactions.append({
                    'catalyst': c_id,
                    'reactant': r_id,
                    'dt': reactant['timestamp'] - catalyst['timestamp']
                })

    # Macroscopic Metrics
    unique_reactions = len(set((r['catalyst'], r['reactant']) for r in reactions))
    reaction_rate = unique_reactions / 5.0  # Reactions per 5-min

    # Average Activation Energy (Inverse of global engagement)
    if not sorted_events:
        activation_energy = 1.0
    else:
        duration = max(1, sorted_events[-1]['timestamp'] - sorted_events[0]['timestamp'])
        global_freq = len(sorted_events) / duration
        activation_energy = max(0.1, 1.0 - (global_freq * 10.0))

    return {
        "status": "ANALYSIS_COMPLETE",
        "timestamp": time.strftime('%Y-%m-%d %H:%M:%S'),
        "reactions_detected": len(reactions),
        "reaction_rate": reaction_rate,
        "activation_energy": f"{activation_energy:.2f} eV (equivalent)",
        "equilibrium_constant": f"{reaction_rate / max(0.1, activation_energy):.2f}"
    }

if __name__ == "__main__":
    print("🧪 Ghost Catalyst: Kinetics Analysis Bridge active.")

    if len(sys.argv) > 1:
        try:
            input_data = json.loads(sys.argv[1])
            results = analyze_catalyst_kinetics(input_data['students'], input_data['events'])
            print(json.dumps(results, indent=2))
        except Exception as e:
            print(f"❌ Error: {e}")
    else:
        # Demo Mode
        mock_students = [
            {'id': 1, 'x': 1000, 'y': 1000},
            {'id': 2, 'x': 1200, 'y': 1100},
            {'id': 3, 'x': 3000, 'y': 3000}
        ]
        mock_events = [
            {'student_id': 1, 'timestamp': 100},
            {'student_id': 2, 'timestamp': 150},
            {'student_id': 3, 'timestamp': 200}
        ]
        print("\n[MOCK MODE RESULTS]")
        print(json.dumps(analyze_catalyst_kinetics(mock_students, mock_events), indent=2))
