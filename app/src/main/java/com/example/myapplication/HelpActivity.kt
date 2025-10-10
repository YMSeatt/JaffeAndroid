package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                        }
                        item {
                            Text(
                                """
                                This app helps you manage your classroom by creating seating charts, logging student behavior, and tracking quiz and homework scores.
                                """.trimIndent(),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        item {
                            Text("Features:", style = MaterialTheme.typography.titleMedium)
                        }
                        item {
                            Text(
                                """
                                - Create and save multiple seating chart layouts.
                                - Add, edit, and delete students and furniture.
                                - Log student behavior, quiz scores, and homework.
                                - Customize the appearance of student boxes.
                                - Export your data to Excel for further analysis.
                                - Backup and restore your database.
                                """.trimIndent(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}