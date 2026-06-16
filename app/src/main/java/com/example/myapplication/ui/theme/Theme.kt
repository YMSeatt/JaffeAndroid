package com.example.myapplication.ui.theme

import android.os.Build
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * A [CompositionLocal] that provides a global [AnimationSpec] for the application.
 *
 * This allows UI components to respect user preferences regarding animation speed
 * and presence, enabling a BOLT-optimized "instant" UI mode when animations are disabled.
 */
val LocalAnimationSpec = staticCompositionLocalOf<AnimationSpec<Float>> {
    tween() // Default animation spec
}

/**
 * MyApplicationTheme: The central Design System coordinator for the application.
 *
 * This theme implements Material 3 and provides a unified visual framework for the
 * seating chart and Ghost Lab visualizations.
 *
 * @param darkTheme Whether to use the dark color scheme. Defaults to system settings.
 * @param dynamicColor Whether to use Android 12+ Dynamic Color (wallpaper-based).
 * @param disableAnimations When true, provides a zero-duration [AnimationSpec] via [LocalAnimationSpec].
 * @param useBoldFont When true, globally overrides typography weights to [FontWeight.Bold].
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    disableAnimations: Boolean = false,
    useBoldFont: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val animationSpec: AnimationSpec<Float> = if (disableAnimations) {
        tween(durationMillis = 0)
    } else {
        tween()
    }

    val typography = if (useBoldFont) {
        Typography.copy(
            displayLarge = Typography.displayLarge.copy(fontWeight = FontWeight.Bold),
            displayMedium = Typography.displayMedium.copy(fontWeight = FontWeight.Bold),
            displaySmall = Typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            headlineLarge = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            headlineMedium = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            headlineSmall = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            titleLarge = Typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            titleMedium = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            titleSmall = Typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            bodyLarge = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            bodyMedium = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            bodySmall = Typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            labelLarge = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            labelMedium = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            labelSmall = Typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    } else {
        Typography
    }

    CompositionLocalProvider(LocalAnimationSpec provides animationSpec) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}