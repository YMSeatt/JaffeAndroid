package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizLog
import com.example.myapplication.data.Student
import com.example.myapplication.viewmodel.SeatingChartViewModel

@Composable
fun LiveQuizMarkDialog(
    studentId: Long,
    viewModel: SeatingChartViewModel,
    onDismissRequest: () -> Unit,
    onSave: (QuizLog) -> Unit
) {
    var student by remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(studentId) {
        student = viewModel.getStudentForEditing(studentId)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(student?.let { "Mark Quiz for ${it.firstName} ${it.lastName}" } ?: "Mark Quiz") },
        text = {
            Column {
                Text(student?.let { "Mark ${it.firstName}'s answer:" } ?: "Mark this student's answer:")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val log = QuizLog(
                        studentId = studentId,
                        quizName = "Live Quiz",
                        loggedAt = System.currentTimeMillis(),
                        comment = "Correct",
                        marksData = "{\"Correct\": 1}",
                        markValue = 1.0,
                        markType = "Correct",
                        maxMarkValue = 1.0,
                        numQuestions = 1
                    )
                    onSave(log)
                    onDismissRequest()
                }) {
                    Text("Correct")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val log = QuizLog(
                        studentId = studentId,
                        quizName = "Live Quiz",
                        loggedAt = System.currentTimeMillis(),
                        comment = "Incorrect",
                        marksData = "{\"Incorrect\": 1}",
                        markValue = 0.0,
                        markType = "Incorrect",
                        maxMarkValue = 1.0,
                        numQuestions = 1
                    )
                    onSave(log)
                    onDismissRequest()
                }) {
                    Text("Incorrect")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
