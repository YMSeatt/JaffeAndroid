package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SettingsViewModel

@Composable
fun SmtpSettingsTab(
    viewModel: SettingsViewModel
) {
    val smtpSettings by viewModel.smtpSettings.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = smtpSettings.host,
            onValueChange = { viewModel.updateSmtpSettings(smtpSettings.copy(host = it)) },
            label = { Text("SMTP Host") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = smtpSettings.port.toString(),
            onValueChange = { port ->
                if (port.all { it.isDigit() }) {
                    viewModel.updateSmtpSettings(smtpSettings.copy(port = port.toIntOrNull() ?: 0))
                }
            },
            label = { Text("SMTP Port") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Use SSL")
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = smtpSettings.useSsl,
                onCheckedChange = { viewModel.updateSmtpSettings(smtpSettings.copy(useSsl = it)) }
            )
        }
    }
}