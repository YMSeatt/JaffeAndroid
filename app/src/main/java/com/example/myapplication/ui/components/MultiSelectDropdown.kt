package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * An interactive dropdown component that allows users to select multiple items from a list.
 *
 * This component is optimized for teacher workflows, such as filtering logs by multiple
 * behavior types or selecting a subset of students for a report. It wraps the standard
 * Material 3 [ExposedDropdownMenuBox] and adds multi-selection logic using checkboxes.
 *
 * @param options The complete list of available strings to select from.
 * @param selectedOptions The current list of strings that are marked as selected.
 * @param onSelectionChanged Callback triggered whenever an item is toggled.
 * @param label The label to display for the collapsed dropdown field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiSelectDropdown(
    options: List<String>,
    selectedOptions: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    label: String
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedOptions.joinToString(),
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.exposedDropdownSize()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = selectedOptions.contains(option),
                                onCheckedChange = null
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    },
                    onClick = {
                        val newSelection = if (selectedOptions.contains(option)) {
                            selectedOptions.filter { it != option }
                        } else {
                            selectedOptions + option
                        }
                        onSelectionChanged(newSelection)
                    }
                )
            }
        }
    }
}
