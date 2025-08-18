package com.example.myapplication.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.StudentGroup
import com.example.myapplication.ui.dialogs.AddEditStudentGroupDialog
import com.example.myapplication.viewmodel.StudentGroupsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentGroupsScreen(viewModel: StudentGroupsViewModel, onDismiss: () -> Unit) {
    val groups by viewModel.studentGroups.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<StudentGroup?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Groups") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedGroup = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Group")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            items(groups) { group ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedGroup = group
                            showDialog = true
                        }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(group.color)))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(group.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (showDialog) {
            AddEditStudentGroupDialog(
                group = selectedGroup,
                viewModel = viewModel,
                onDismiss = { showDialog = false }
            )
        }
    }
}