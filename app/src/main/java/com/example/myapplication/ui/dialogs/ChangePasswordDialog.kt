package com.example.myapplication.ui.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ChangePasswordDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    Text("ChangePasswordDialog")
}
