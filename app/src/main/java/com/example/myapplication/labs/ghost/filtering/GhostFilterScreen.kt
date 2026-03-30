package com.example.myapplication.labs.ghost.filtering

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.labs.ghost.util.ghostShimmer

/**
 * GhostFilterScreen: A high-performance student list featuring complex filtering.
 *
 * This screen demonstrates optimized UI updates by leveraging reactive flows from the ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhostFilterScreen(
    viewModel: GhostFilterViewModel,
    onBack: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGroupIds by viewModel.selectedGroupIds.collectAsState()
    val allGroups by viewModel.allGroups.collectAsState()
    val filteredStudents by viewModel.filteredStudents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Neural Filter 👻") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name or nickname...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            // Group Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                items(allGroups) { group ->
                    val isSelected = selectedGroupIds.contains(group.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.toggleGroupSelection(group.id) },
                        label = { Text(group.name) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            // Student List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (filteredStudents.isEmpty() && searchQuery.isEmpty()) {
                    // Shimmer state if nothing loaded yet
                    items(5) {
                        GhostShimmerItem()
                    }
                } else if (filteredStudents.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No students found matching your criteria.", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    items(filteredStudents) { student ->
                        StudentFilterCard(
                            name = "${student.firstName} ${student.lastName}",
                            nickname = student.nickname,
                            initials = student.getEffectiveInitials()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentFilterCard(name: String, nickname: String?, initials: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleMedium)
                if (!nickname.isNullOrBlank()) {
                    Text("\"$nickname\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

/**
 * GhostShimmerItem: A futuristic skeleton loader for the student list.
 */
@Composable
fun GhostShimmerItem() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .ghostShimmer()
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(20.dp)
                        .ghostShimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp)
                        .ghostShimmer()
                )
            }
        }
    }
}
