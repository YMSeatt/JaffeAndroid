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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

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
        val density = LocalDensity.current
        val intOffset = with(density) { IntOffset(offset.x.roundToPx(), offset.y.roundToPx()) }
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
