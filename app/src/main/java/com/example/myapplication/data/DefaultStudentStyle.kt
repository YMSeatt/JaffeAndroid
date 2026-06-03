package com.example.myapplication.data

/**
 * Defines the default visual styling parameters for students on the seating chart.
 *
 * This data class is used to manage application-wide style defaults, typically
 * persisted in [com.example.myapplication.preferences.AppPreferencesRepository]. These values
 * serve as the fallback when a [Student] entity does not have specific custom style
 * overrides (e.g., `customBackgroundColor` is null).
 *
 * @property backgroundColor Default background/fill color in Hex format.
 * @property outlineColor Default border/outline color in Hex format.
 * @property textColor Default color for primary text (e.g., student name).
 * @property width Default width of the student icon in DP.
 * @property height Default height of the student icon in DP.
 * @property outlineThickness Default thickness of the icon border in DP.
 * @property fontFamily Default font family name (e.g., "sans-serif").
 * @property fontSize Default font size in SP.
 * @property fontColor Default color for all font elements.
 * @property cornerRadius Default corner radius for the student box in DP.
 * @property padding Default internal padding for the student box in DP.
 */
data class DefaultStudentStyle(
    val backgroundColor: String,
    val outlineColor: String,
    val textColor: String,
    val width: Int,
    val height: Int,
    val outlineThickness: Int,
    val fontFamily: String,
    val fontSize: Int,
    val fontColor: String,
    val cornerRadius: Int,
    val padding: Int
)
