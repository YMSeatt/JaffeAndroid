package com.example.myapplication.ui.model

import com.example.myapplication.commands.ItemType

/**
 * Uniquely identifies a seating chart item (either a Student or a Furniture).
 *
 * @property id The database ID of the item.
 * @property type The type of the item (STUDENT or FURNITURE).
 */
data class ChartItemId(val id: Int, val type: ItemType)
