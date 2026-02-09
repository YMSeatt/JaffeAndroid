package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.QuizTemplate
import com.example.myapplication.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageInitialsScreen(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Behaviors", "Homework", "Quizzes")

    val systemBehaviors by viewModel.allSystemBehaviors.observeAsState(initial = emptyList())
    val customBehaviors by viewModel.customBehaviors.observeAsState(initial = emptyList())

    val homeworkTypes by viewModel.customHomeworkTypes.observeAsState(initial = emptyList())

    val quizTemplates by viewModel.allQuizTemplates.observeAsState(initial = emptyList())

    val behaviorInitialsMapStr by viewModel.behaviorInitialsMap.collectAsState()
    val homeworkInitialsMapStr by viewModel.homeworkInitialsMap.collectAsState()
    val quizInitialsMapStr by viewModel.quizInitialsMap.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Initials") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    val allBehaviors = (systemBehaviors.map { it.name } + customBehaviors.map { it.name }).distinct()
                    InitialsList(
                        items = allBehaviors,
                        initialsMapStr = behaviorInitialsMapStr,
                        onUpdateMap = { viewModel.updateBehaviorInitialsMap(it) }
                    )
                }
                1 -> {
                    val allHomework = homeworkTypes.map { it.name }.distinct()
                    InitialsList(
                        items = allHomework,
                        initialsMapStr = homeworkInitialsMapStr,
                        onUpdateMap = { viewModel.updateHomeworkInitialsMap(it) }
                    )
                }
                2 -> {
                    val allQuizzes = quizTemplates.map { it.name }.distinct()
                    InitialsList(
                        items = allQuizzes,
                        initialsMapStr = quizInitialsMapStr,
                        onUpdateMap = { viewModel.updateQuizInitialsMap(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun InitialsList(
    items: List<String>,
    initialsMapStr: String,
    onUpdateMap: (String) -> Unit
) {
    val initialsMap = remember(initialsMapStr) {
        initialsMapStr.split(",")
            .mapNotNull {
                val parts = it.split(":", limit = 2)
                if (parts.size == 2) parts[0].trim() to parts[1].trim() else null
            }
            .toMap()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = initialsMap[item] ?: "",
                    onValueChange = { newValue ->
                        val newMap = initialsMap.toMutableMap()
                        if (newValue.isBlank()) {
                            newMap.remove(item)
                        } else {
                            newMap[item] = newValue.take(5) // Limit to 5 chars
                        }
                        onUpdateMap(newMap.map { "${it.key}:${it.value}" }.joinToString(","))
                    },
                    label = { Text("Initial") },
                    modifier = Modifier.weight(0.4f),
                    singleLine = true
                )
            }
            HorizontalDivider()
        }
    }
}
