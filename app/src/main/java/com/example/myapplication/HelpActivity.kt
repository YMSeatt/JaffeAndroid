package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme

class HelpActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Help") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                        item {
                            Text("Welcome to the Seating Chart App!", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(
                                "This app helps you manage your classroom by creating seating charts, logging student behavior, and tracking quiz and homework scores.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.padding(16.dp))
                        }

                        item {
                            HelpSection(
                                title = "Seating Chart",
                                content = """
                                - **Move Students/Furniture**: Drag items to move them around the canvas.
                                - **Resize**: Long-press a student or furniture to access the resize option, or use the 'Change Box Size' option in the menu.
                                - **Zoom/Pan**: Pinch to zoom and drag with two fingers to pan the canvas.
                                - **Grid & Rulers**: Toggle the grid and rulers in Settings to help align items. Enable 'Snap to Grid' for precise placement.
                                - **Guides**: Add horizontal or vertical guides from the 'View' menu to assist with alignment.
                                - **Layouts**: Save your current arrangement as a layout template via 'File > Save Layout'. Load saved layouts via 'File > Load Layout'.
                                """.trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = "Students & Furniture",
                                content = """
                                - **Add**: Use the Floating Action Button (FAB) to add new students or furniture.
                                - **Edit**: Long-press a student to access the action menu, where you can edit details, change style, or delete.
                                - **Selection**: Toggle 'Select' mode to select multiple students. You can then perform batch actions like aligning, distributing, or logging behavior.
                                - **Styling**: Customize the appearance of individual students (color, font, size) via 'Change Student Box Style'.
                                - **Groups**: Assign students to groups for easier management and conditional formatting.
                                """.trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = "Logging",
                                content = """
                                - **Behavior**: Log behavior events (positive/negative) for students. Customize behavior types in Settings.
                                - **Quiz**: Log quiz scores. You can enter scores manually or use the 'Live Quiz' mode.
                                - **Homework**: Log homework completion and grades.
                                - **Recent Logs**: View recent logs for a student by long-pressing and selecting 'Show Recent Logs'.
                                """.trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = "Conditional Formatting",
                                content = """
                                - **Rules**: Create rules to automatically change the appearance of students based on conditions (e.g., low quiz score, specific group, behavior count).
                                - **Active Modes**: Rules can be active only in specific modes (Behavior, Quiz, Homework).
                                - **Active Times**: Rules can be scheduled to be active only during specific times and days.
                                - **Priority**: Rules are applied in order of priority. Higher priority rules override lower ones.
                                """.trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = "Exporting & Data",
                                content = """
                                - **Export to Excel**: Export student data, logs, and summaries to an Excel file via 'File > Export to Excel'.
                                - **Email**: Share exported files directly via email.
                                - **Import**: Import student lists from Excel or restore data from JSON backups.
                                - **Backup**: Export the entire database to a file for backup purposes via 'File > Export Database'.
                                """.trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = "Settings",
                                content = """
                                - **Display**: Customize the canvas background color, default student box size, and font settings.
                                - **Behaviors/Homework**: Manage custom behavior types and homework types.
                                - **Security**: Set a master password to protect sensitive data.
                                - **Theme**: Switch between Light, Dark, and System themes.
                                """.trimIndent()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HelpSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.padding(4.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium)
    }
}