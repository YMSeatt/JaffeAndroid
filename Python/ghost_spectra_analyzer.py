import json
import os
from datetime import datetime

def analyze_spectra(data_path):
    """
    Analyzes the 'Spectral Signature' of a classroom based on exported JSON data.
    In 2027, this would be part of a real-time neural dashboard.
    """
    if not os.path.exists(data_path):
        print(f"Error: {data_path} not found.")
        return

    with open(data_path, 'r') as f:
        data = json.load(f)

    students = data.get('students', [])
    behaviors = data.get('behavior_events', [])

    print(f"--- 👻 Ghost Spectra: Neural Analysis Report ---")
    print(f"Timestamp: {datetime.now().isoformat()}")
    print(f"Classroom Size: {len(students)} nodes")
    print(f"Behavioral Log Volume: {len(behaviors)} events")

    # Calculate Spectral Dispersion
    # Higher dispersion = high variance in behavior types
    behavior_types = set([b.get('type') for b in behaviors])
    dispersion = len(behavior_types) / 10.0 if behavior_types else 0.0

    # Calculate Chromatic Agitation
    # High agitation = frequent recent negative logs
    negative_logs = [b for b in behaviors if 'Negative' in b.get('type', '')]
    agitation = len(negative_logs) / len(behaviors) if behaviors else 0.0

    print(f"\n[SPECTRAL METRICS]")
    print(f"Dispersion Index: {dispersion:.2f} (Refractive Variance)")
    print(f"Agitation Level:  {agitation:.2f} (Neural Turbulence)")

    print(f"\n[NODE SPECTROSCOPY]")
    for student in students[:10]: # Analyze top 10 for PoC
        s_id = student.get('id')
        s_logs = [b for b in behaviors if b.get('studentId') == s_id]
        s_neg = [b for b in s_logs if 'Negative' in b.get('type', '')]

        s_intensity = len(s_logs) / 20.0 # Normalized intensity
        s_shift = len(s_neg) / len(s_logs) if s_logs else 0.0

        status = "STABLE"
        if s_shift > 0.5: status = "INFRARED (At Risk)"
        elif s_intensity > 0.8: status = "ULTRAVIOLET (High Engagement)"

        print(f" - {student.get('firstName')} {student.get('lastName')}: {status} (I:{s_intensity:.2f}, S:{s_shift:.2f})")

    print(f"\n--- End of Ghost Spectra Report ---")

if __name__ == "__main__":
    # Path to sample data if exists, else instructions
    sample_path = 'classroom_data.json'
    if os.path.exists(sample_path):
        analyze_spectra(sample_path)
    else:
        print("Ghost Spectra Analyzer ready. Export classroom data as JSON to begin.")
