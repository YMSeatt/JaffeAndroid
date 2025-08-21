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
import com.example.myapplication.data.HomeworkLog
import com.example.myapplication.viewmodel.SeatingChartViewModel

@Composable
fun LiveHomeworkMarkDialog(
    studentId: Long,
    viewModel: SeatingChartViewModel,
    onDismissRequest: () -> Unit,
    onSave: (HomeworkLog) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Mark Homework") },
        text = {
            Column {
                Text("Mark this student's homework:")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    val log = HomeworkLog(
                        studentId = studentId,
                        assignmentName = "Live Homework",
                        status = "Done",
                        loggedAt = System.currentTimeMillis(),
                        marksData = "{}"
                    )
                    onSave(log)
                    onDismissRequest()
                }) {
                    Text("Done")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    val log = HomeworkLog(
                        studentId = studentId,
                        assignmentName = "Live Homework",
                        status = "Not Done",
                        loggedAt = System.currentTimeMillis(),
                        marksData = "{}"
                    )
                    onSave(log)
                    onDismissRequest()
                }) {
                    Text("Not Done")
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
