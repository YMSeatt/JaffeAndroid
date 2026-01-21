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
import androidx.compose.ui.res.stringResource
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
                            title = { Text(stringResource(R.string.help_screen_title)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.help_screen_back_button_description)
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
                        item {
                            Text(stringResource(R.string.help_screen_welcome), style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.padding(8.dp))
                            Text(
                                stringResource(R.string.help_screen_app_description),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.padding(16.dp))
                        }

                        item {
                            HelpSection(
                                title = stringResource(R.string.help_screen_seating_chart_title),
                                content = stringResource(R.string.help_screen_seating_chart_content).trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = stringResource(R.string.help_screen_students_furniture_title),
                                content = stringResource(R.string.help_screen_students_furniture_content).trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = stringResource(R.string.help_screen_logging_title),
                                content = stringResource(R.string.help_screen_logging_content).trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = stringResource(R.string.help_screen_conditional_formatting_title),
                                content = stringResource(R.string.help_screen_conditional_formatting_content).trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = stringResource(R.string.help_screen_exporting_data_title),
                                content = stringResource(R.string.help_screen_exporting_data_content).trimIndent()
                            )
                        }

                        item {
                            HelpSection(
                                title = stringResource(R.string.help_screen_settings_title),
                                content = stringResource(R.string.help_screen_settings_content).trimIndent()
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
        Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.padding(4.dp))
        Text(content, style = MaterialTheme.typography.bodyMedium)
    }
}
