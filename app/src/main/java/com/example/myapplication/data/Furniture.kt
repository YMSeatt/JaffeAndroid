package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents a physical object on the classroom seating chart, such as a desk, chair, or bookshelf.
 *
 * Furniture items are used to provide spatial context to the classroom layout. Like students,
 * they have precise coordinates and dimensions on the 4000x4000 logical canvas.
 *
 * @property id Unique identifier for the furniture item.
 * @property stringId Persistent identifier for cross-platform JSON synchronization (e.g., "furniture_1").
 * @property name A descriptive name for the item (e.g., "Teacher's Desk").
 * @property type The category of furniture (e.g., "desk", "bookshelf", "table").
 * @property xPosition Horizontal position on the seating chart canvas.
 * @property yPosition Vertical position on the seating chart canvas.
 * @property width The width of the item in logical units.
 * @property height The height of the item in logical units.
 * @property fillColor The interior color of the furniture icon (Hex string).
 * @property outlineColor The border color of the furniture icon (Hex string).
 */
@Serializable
@Entity(tableName = "furniture")
data class Furniture(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val stringId: String? = null,
    val name: String,
    val type: String,
    var xPosition: Float = 0f,
    var yPosition: Float = 0f,
    var width: Int,
    var height: Int,
    var fillColor: String? = null,
    var outlineColor: String? = null
)
