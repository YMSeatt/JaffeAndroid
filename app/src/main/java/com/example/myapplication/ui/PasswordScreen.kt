package com.example.myapplication.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun PasswordScreen(
    settingsViewModel: SettingsViewModel,
    onUnlocked: () -> Unit
) {
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    var passwordAttempt by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    // Observe passwordEnabled to potentially show a message if no password is set
    // val passwordEnabled by settingsViewModel.passwordEnabled.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter Password", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = passwordAttempt,
            onValueChange = {
                passwordAttempt = it
                showError = false // Clear error when user types
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = showError
        )
        if (showError) {
            Text(
                "Incorrect password. Please try again.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    if (settingsViewModel.checkPassword(passwordAttempt)) {
                        onUnlocked()
                    } else {
                        showError = true
                    }
                }
            },
            // Consider disabling button if !passwordEnabled, though MainActivity should probably handle this
            // enabled = passwordEnabled
        ) {
            Text("Unlock")
        }
    }
}
