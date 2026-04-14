package com.example.myapplication.labs.ghost.util

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ui.theme.*

/**
 * GhostChroma: The experimental R&D theme engine for Ghost Lab.
 *
 * It provides a "Neural Ghost" color palette and a Composable theme wrapper
 * that integrates Material You dynamic colors with the Ghost Lab aesthetic.
 */

private val GhostDarkColorScheme = darkColorScheme(
    primary = GhostCyan,
    secondary = GhostElectricBlue,
    tertiary = GhostMagenta,
    background = Color(0xFF000808),
    surface = Color(0xFF001212),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Cyan,
    onSurface = Color.Cyan
)

private val GhostLightColorScheme = lightColorScheme(
    primary = Color(0xFF008877),
    secondary = Color(0xFF006699),
    tertiary = Color(0xFFBB2255),
    background = Color(0xFFE0F7F7),
    surface = Color(0xFFF0FFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF004444),
    onSurface = Color(0xFF004444)
)

/**
 * GhostChromaTheme: A specialized theme for Ghost Lab R&D.
 *
 * @param themeMode The active theme mode ("LIGHT", "DARK", "GHOST").
 * @param dynamicColor Whether Material You dynamic colors should be used (API 31+).
 */
@Composable
fun GhostChromaTheme(
    themeMode: String = "GHOST",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == "GHOST" -> GhostDarkColorScheme
        themeMode == "DARK" -> darkColorScheme(primary = GhostCyan, secondary = GhostElectricBlue)
        themeMode == "LIGHT" -> lightColorScheme(primary = Color(0xFF008877), secondary = Color(0xFF006699))
        else -> if (darkTheme) darkColorScheme() else lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
