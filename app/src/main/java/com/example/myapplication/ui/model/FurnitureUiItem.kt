package com.example.myapplication.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * A UI-optimized representation of a Furniture item.
 *
 * Similar to [StudentUiItem], this class uses [MutableState] for its properties to enable:
 * 1. **Fine-grained Recomposition**: Compose can observe and react to changes in individual fields
 *    (e.g., position, size, or colors) without recomposing the entire component.
 * 2. **Instance Reuse**: Cached in the ViewModel to reduce memory churn and GC overhead
 *    during frequent updates (like dragging).
 */
data class FurnitureUiItem(
    val id: Int,
    val stringId: String?,
    val name: MutableState<String>,
    val type: MutableState<String>,
    val xPosition: MutableState<Float>,
    val yPosition: MutableState<Float>,
    val displayWidth: MutableState<Dp>,
    val displayHeight: MutableState<Dp>,
    val displayBackgroundColor: MutableState<Color>,
    val displayOutlineColor: MutableState<Color>,
    val displayTextColor: MutableState<Color>,
    val displayOutlineThickness: MutableState<Dp>
)
