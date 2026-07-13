package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a visual alignment line on the seating chart canvas.
 *
 * Guides are used to help teachers align students and furniture symmetrically.
 * They can be either horizontal or vertical and are rendered as thin lines on the canvas.
 *
 * @property id Unique identifier for the guide.
 * @property type The orientation of the guide (HORIZONTAL or VERTICAL).
 * @property position The coordinate of the guide on the respective axis (X for vertical, Y for horizontal).
 */
@Serializable
@Entity(tableName = "guides")
data class Guide(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: GuideType,
    var position: Float
)
