package com.example.myapplication.data

import kotlinx.serialization.Serializable

/**
 * Defines the orientation of a visual alignment guide on the seating chart.
 */
@Serializable
enum class GuideType {
    /** A horizontal line used for vertical alignment (parallel to the X-axis). */
    HORIZONTAL,
    /** A vertical line used for horizontal alignment (parallel to the Y-axis). */
    VERTICAL
}
