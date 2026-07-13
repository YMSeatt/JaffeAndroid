import math

def identify_threads(quiz_logs_by_student, homework_logs_by_student):
    threads = []

    student_ids_list = []
    high_performing_quizzes = {}
    completed_homework = {}

    # Pre-process Quiz Logs
    for student_id, logs in quiz_logs_by_student.items():
        student_ids_list.append(student_id)
        quiz_set = set()
        for q in logs:
            mark = q.get('markValue', 0.0) or 0.0
            max_mark = q.get('maxMarkValue', 0.0) or 0.0
            score = mark / max_mark if max_mark > 0 else 0.0
            if score >= 0.8:
                quiz_set.add(q['quizName'])
        if quiz_set:
            high_performing_quizzes[student_id] = quiz_set

    # Pre-process Homework Logs
    for student_id, logs in homework_logs_by_student.items():
        if student_id not in quiz_logs_by_student:
            student_ids_list.append(student_id)
        hw_set = set()
        for h in logs:
            status = h.get('status', '').lower()
            is_done = ('done' in status and 'not' not in status) or 'complete' in status
            if is_done:
                hw_set.add(h['assignmentName'])
        if hw_set:
            completed_homework[student_id] = hw_set

    # Pairwise student comparison
    for i in range(len(student_ids_list)):
        id_a = student_ids_list[i]
        quizzes_a = high_performing_quizzes.get(id_a)
        homework_a = completed_homework.get(id_a)

        for j in range(i + 1, len(student_ids_list)):
            id_b = student_ids_list[j]

            # 1. Check shared high-performing quizzes
            quizzes_b = high_performing_quizzes.get(id_b)
            if quizzes_a and quizzes_b:
                shared_quizzes = quizzes_a.intersection(quizzes_b)
                shared_quiz_count = len(shared_quizzes)
                if shared_quiz_count > 0:
                    threads.append({
                        'studentA': id_a,
                        'studentB': id_b,
                        'strength': min(shared_quiz_count / 3.0, 1.0),
                        'type': 'ACADEMIC_SYNERGY'
                    })

            # 2. Check shared completed homework
            homework_b = completed_homework.get(id_b)
            if homework_a and homework_b:
                shared_homework = homework_a.intersection(homework_b)
                shared_homework_count = len(shared_homework)
                if shared_homework_count > 0:
                    threads.append({
                        'studentA': id_a,
                        'studentB': id_b,
                        'strength': min(shared_homework_count / 5.0, 1.0),
                        'type': 'HOMEWORK_COLLABORATION'
                    })

    return threads

def test_weaver_logic():
    # Setup mock data
    quiz_logs = {
        1: [{'quizName': 'Math', 'markValue': 9.0, 'maxMarkValue': 10.0}],
        2: [{'quizName': 'Math', 'markValue': 8.5, 'maxMarkValue': 10.0}],
        3: [{'quizName': 'Math', 'markValue': 5.0, 'maxMarkValue': 10.0}]
    }

    homework_logs = {
        1: [{'assignmentName': 'Essay', 'status': 'Done'}],
        2: [{'assignmentName': 'Essay', 'status': 'Complete'}],
        4: [{'assignmentName': 'Essay', 'status': 'Not Done'}]
    }

    threads = identify_threads(quiz_logs, homework_logs)

    # Verify results
    assert len(threads) == 2, f"Expected 2 threads, got {len(threads)}"

    synergy = [t for t in threads if t['type'] == 'ACADEMIC_SYNERGY']
    assert len(synergy) == 1
    assert synergy[0]['studentA'] == 1 and synergy[0]['studentB'] == 2

    collab = [t for t in threads if t['type'] == 'HOMEWORK_COLLABORATION']
    assert len(collab) == 1
    assert collab[0]['studentA'] == 1 and collab[0]['studentB'] == 2

    print("Weaver logic verification PASSED 👻")

if __name__ == "__main__":
    test_weaver_logic()
