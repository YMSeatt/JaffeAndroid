package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AdvancedSettingsTab(
    onNavigateToStudentGroups: () -> Unit,
    onNavigateToConditionalFormatting: () -> Unit,
    onNavigateToExport: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Button(onClick = onNavigateToStudentGroups, modifier = Modifier.fillMaxWidth()) {
                Text("Manage Student Groups")
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        item {
            Button(
                onClick = onNavigateToConditionalFormatting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Manage Conditional Formatting Rules")
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
        }
        item {
            Button(onClick = onNavigateToExport, modifier = Modifier.fillMaxWidth()) {
                Text("Export Data")
            }
        }
    }
}
