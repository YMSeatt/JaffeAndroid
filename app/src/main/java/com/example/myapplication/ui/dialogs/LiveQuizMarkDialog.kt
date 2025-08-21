package com.example.myapplication.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizLog
import com.example.myapplication.viewmodel.SeatingChartViewModel

@Composable
fun LiveQuizMarkDialog(
    studentId: Long,
    viewModel: SeatingChartViewModel,
    onDismissRequest: () -> Unit,
    onSave: (QuizLog) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Mark Quiz") },
        text = {
            Column {
                Text("Mark this student's answer:")
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
