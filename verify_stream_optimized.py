
import collections

# Mocking StudentUiItem and other classes
class StudentUiItem:
    def __init__(self, id, name):
        self.id = id
        self.fullName = type('obj', (object,), {'value': name})

class BehaviorEvent:
    def __init__(self, studentId, type, timestamp, comment=None):
        self.studentId = studentId
        self.type = type
        self.timestamp = timestamp
        self.comment = comment

class QuizLog:
    def __init__(self, studentId, quizName, markValue, maxMarkValue, loggedAt):
        self.studentId = studentId
        self.quizName = quizName
        self.markValue = markValue
        self.maxMarkValue = maxMarkValue
        self.loggedAt = loggedAt

class HomeworkLog:
    def __init__(self, studentId, assignmentName, status, loggedAt):
        self.studentId = studentId
        self.assignmentName = assignmentName
        self.status = status
        self.loggedAt = loggedAt

def synthesize_stream(students, behavior_logs, quiz_logs, homework_logs, max_entries=20):
    if max_entries <= 0: return []

    pending = []

    # Behavior Logs
    for log in behavior_logs[:max_entries]:
        entry_type = "NEGATIVE" if "negative" in log.type.lower() else "POSITIVE"
        pending.append({'log': log, 'timestamp': log.timestamp, 'type': entry_type})

    # Quiz Logs
    for log in quiz_logs[:max_entries]:
        pending.append({'log': log, 'timestamp': log.loggedAt, 'type': "ACADEMIC"})

    # Homework Logs
    for log in homework_logs[:max_entries]:
        pending.append({'log': log, 'timestamp': log.loggedAt, 'type': "ACADEMIC"})

    pending.sort(key=lambda x: x['timestamp'], reverse=True)
    pending = pending[:max_entries]

    if not pending: return []

    # BOLT Optimization: Pre-index students
    student_map = {s.id: s for s in students}

    result = []
    for p in pending:
        item = p['log']
        if isinstance(item, BehaviorEvent):
            sid = item.studentId
        elif isinstance(item, QuizLog):
            sid = item.studentId
        elif isinstance(item, HomeworkLog):
            sid = item.studentId
        else:
            sid = 0

        student = student_map.get(sid)
        if not student: continue

        result.append({
            'timestamp': p['timestamp'],
            'studentName': student.fullName.value,
            'type': p['type']
        })
    return result

def test_optimized_stream():
    students = [StudentUiItem(1, "John Doe"), StudentUiItem(2, "Jane Smith")]
    behavior_logs = [BehaviorEvent(1, "Participating", 3000)]
    quiz_logs = [QuizLog(2, "Math", 9, 10, 1000)]
    homework_logs = [HomeworkLog(1, "Essay", "Done", 2000)]

    stream = synthesize_stream(students, behavior_logs, quiz_logs, homework_logs)

    assert len(stream) == 3
    assert stream[0]['timestamp'] == 3000
    assert stream[0]['studentName'] == "John Doe"
    assert stream[1]['timestamp'] == 2000
    assert stream[1]['studentName'] == "John Doe"
    assert stream[2]['timestamp'] == 1000
    assert stream[2]['studentName'] == "Jane Smith"

    print("GhostStreamEngine optimized logic verification PASSED ⚡")

if __name__ == "__main__":
    test_optimized_stream()
