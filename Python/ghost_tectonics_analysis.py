import json
import math
from datetime import datetime

def analyze_tectonic_stress(classroom_data):
    """
    Analyzes social stress and seismic risk in the classroom.
    Logical parity with GhostTectonicEngine.kt.
    """
    students = classroom_data.get('students', [])
    behavior_logs = classroom_data.get('behavior_logs', [])

    stress_radius = 600.0
    negative_log_weight = 0.15

    # 1. Base stress from individual negative logs
    base_stress = {}
    negative_logs = [log for log in behavior_logs if 'negative' in log.get('type', '').lower()]

    for student in students:
        s_id = student.get('id')
        s_logs = [log for log in negative_logs if log.get('student_id') == s_id]
        base_stress[s_id] = min(len(s_logs) * negative_log_weight, 0.5)

    # 2. Accumulate proximity stress
    tectonic_nodes = []
    for s1 in students:
        s1_id = s1.get('id')
        proximity_stress = 0.0

        for s2 in students:
            s2_id = s2.get('id')
            if s1_id != s2_id:
                dx = s1.get('x', 0) - s2.get('x', 0)
                dy = s1.get('y', 0) - s2.get('y', 0)
                dist = math.sqrt(dx*dx + dy*dy)

                if dist < stress_radius:
                    s2_base = base_stress.get(s2_id, 0.0)
                    proximity_stress += (s2_base * (1.0 - dist / stress_radius)) * 0.5

        total_stress = min(max(base_stress.get(s1_id, 0.0) + proximity_stress, 0.0), 1.0)
        tectonic_nodes.append({
            'id': s1_id,
            'name': f"{s1.get('firstName', '')} {s1.get('lastName', '')}",
            'stress': total_stress,
            'x': s1.get('x'),
            'y': s1.get('y')
        })

    # 3. Macroscopic Analysis
    avg_stress = sum(n['stress'] for n in tectonic_nodes) / len(tectonic_nodes) if tectonic_nodes else 0
    peak_stress = max(n['stress'] for n in tectonic_nodes) if tectonic_nodes else 0

    fault_lines = 0
    for i in range(len(tectonic_nodes)):
        for j in range(i + 1, len(tectonic_nodes)):
            n1 = tectonic_nodes[i]
            n2 = tectonic_nodes[j]
            dist = math.sqrt((n1['x'] - n2['x'])**2 + (n1['y'] - n2['y'])**2)
            if dist < stress_radius and n1['stress'] > 0.4 and n2['stress'] > 0.4:
                fault_lines += 1

    risk_level = "STABLE"
    if peak_stress > 0.8 or fault_lines > 3:
        risk_level = "CRITICAL"
    elif peak_stress > 0.6 or fault_lines > 1:
        risk_level = "VOLATILE"
    elif avg_stress > 0.3:
        risk_level = "ACCUMULATING"

    return {
        'timestamp': datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        'risk_level': risk_level,
        'avg_stress': avg_stress,
        'peak_stress': peak_stress,
        'fault_lines': fault_lines,
        'nodes': tectonic_nodes
    }

def generate_report(analysis):
    report = f"# 🌋 GHOST TECTONICS: SEISMIC RISK ANALYSIS (PYTHON BRIDGE)\n"
    report += f"Risk Level: {analysis['risk_level']}\n"
    report += f"Avg Stress: {analysis['avg_stress']:.1%}\n"
    report += f"Peak Stress: {analysis['peak_stress']:.1%}\n"
    report += f"Fault Lines: {analysis['fault_lines']}\n"
    report += f"Analyzed at: {analysis['timestamp']}\n\n"
    report += "## [CRITICAL NODES]\n"

    critical_nodes = [n for n in analysis['nodes'] if n['stress'] > 0.5]
    for n in sorted(critical_nodes, key=lambda x: x['stress'], reverse=True):
        report += f"- {n['name']}: {n['stress']:.1%} stress\n"

    return report

if __name__ == "__main__":
    # Mock data for demonstration
    mock_data = {
        'students': [
            {'id': 1, 'firstName': 'John', 'lastName': 'Doe', 'x': 100, 'y': 100},
            {'id': 2, 'firstName': 'Jane', 'lastName': 'Smith', 'x': 200, 'y': 100},
            {'id': 3, 'firstName': 'Bob', 'lastName': 'Brown', 'x': 1500, 'y': 1500}
        ],
        'behavior_logs': [
            {'student_id': 1, 'type': 'Negative'},
            {'student_id': 1, 'type': 'Negative'},
            {'student_id': 2, 'type': 'Negative'}
        ]
    }

    analysis = analyze_tectonic_stress(mock_data)
    print(generate_report(analysis))
