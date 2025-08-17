package com.example.myapplication.ui.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SetPasswordDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    Text("SetPasswordDialog")
}
