
import json

class HomeworkLog:
    def __init__(self, status, marks_data):
        self.status = status
        self.marks_data = json.dumps(marks_data) if marks_data else None

class HomeworkMarkMetadata:
    def __init__(self, name, default_points):
        self.name = name
        self.default_points = default_points

def calculate_total_points(log, mark_metadata):
    total_points = 0.0
    marks_data = json.loads(log.marks_data) if log.marks_data else {}

    # 1. Numeric accumulation from marksData
    for value in marks_data.values():
        try:
            total_points += float(value)
        except (ValueError, TypeError):
            pass

    # 2. Status-based points
    status_normalized = log.status.strip()

    if status_normalized.lower() == "yes":
        complete_meta = next((m for m in mark_metadata if m.name.lower() == "complete"), None)
        if complete_meta:
            total_points += complete_meta.default_points
    else:
        status_meta = next((m for m in mark_metadata if m.name.lower() == status_normalized.lower()), None)
        if status_meta:
            total_points += status_meta.default_points

    # 3. Mapping of status-like keys in marksData
    for key, value in marks_data.items():
        is_numeric = False
        try:
            float(value)
            is_numeric = True
        except (ValueError, TypeError):
            pass

        if not is_numeric:
            key_meta = next((m for m in mark_metadata if m.name.lower() == key.lower()), None)
            if key_meta:
                total_points += key_meta.default_points

    return total_points

# Test cases
metadata = [
    HomeworkMarkMetadata("Complete", 10.0),
    HomeworkMarkMetadata("Late", 5.0),
    HomeworkMarkMetadata("Bonus", 2.0)
]

tests = [
    (HomeworkLog("Yes", {"Reading": 1.0}), 11.0),
    (HomeworkLog("Late", {}), 5.0),
    (HomeworkLog("No", {"Bonus": "Yes", "Math": 5.0}), 7.0),
    (HomeworkLog("Complete", {"Extra": 5.0}), 15.0)
]

for log, expected in tests:
    result = calculate_total_points(log, metadata)
    print(f"Log(status={log.status}, marks={log.marks_data}) -> Result: {result}, Expected: {expected}")
    assert result == expected

print("All tests passed!")
