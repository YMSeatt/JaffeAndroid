package com.example.myapplication.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

/**
 * A highly customizable dropdown menu implementation designed for high-performance UIs.
 *
 * Unlike the standard Material 3 [androidx.compose.material3.DropdownMenu], this component:
 * 1. **Supports Animation Toggling**: Allows animations to be disabled globally via
 *    [noAnimations], which is critical for maintaining 60fps on lower-end devices or
 *    during heavy classroom simulations.
 * 2. **Explicit Positioning**: Uses [IntOffset] and [Popup] for more predictable placement
 *    on the large seating chart canvas.
 *
 * @param expanded Whether the menu is currently visible.
 * @param onDismissRequest Callback triggered when the user clicks outside the menu.
 * @param modifier Modifier for the menu's card container.
 * @param offset The relative displacement of the menu from its anchor point.
 * @param noAnimations If true, disables the fade-in/fade-out transitions.
 * @param content The composable content (typically [androidx.compose.material3.DropdownMenuItem]s).
 */
@Composable
fun CustomDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    noAnimations: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    if (expanded) {
        val intOffset = IntOffset(offset.x.value.roundToInt(), offset.y.value.roundToInt())
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true),
            offset = intOffset
        ) {
            val enter = if (noAnimations) EnterTransition.None else fadeIn()
            val exit = if (noAnimations) ExitTransition.None else fadeOut()

            AnimatedVisibility(
                visible = expanded,
                enter = enter,
                exit = exit
            ) {
                Card(
                    modifier = modifier,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
