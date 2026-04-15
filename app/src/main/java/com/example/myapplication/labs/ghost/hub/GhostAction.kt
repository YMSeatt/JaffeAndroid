package com.example.myapplication.labs.ghost.hub

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * GhostAction: Data model for a radial menu action.
 */
data class GhostAction(
    val id: String,
    val icon: ImageVector,
    val label: String
)
