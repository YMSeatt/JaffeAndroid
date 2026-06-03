package com.example.myapplication.data

import kotlinx.serialization.Serializable

/**
 * Represents the spatial and identity state of a student at the time a layout was saved.
 *
 * This DTO is used within [LayoutData] to persist student positions. It supports a
 * multi-stage matching strategy when a layout is restored:
 * 1. **ID Match**: Uses the [id] for a direct O(1) lookup.
 * 2. **Exact Name Match**: If ID fails, matches [firstName] and [lastName] against the current roster.
 * 3. **Fuzzy Name Match**: Uses Levenshtein distance (threshold >= 0.85) to reconcile minor spelling variations.
 *
 * @property id The database primary key of the student.
 * @property x The horizontal coordinate on the 4000x4000 logical canvas.
 * @property y The vertical coordinate on the 4000x4000 logical canvas.
 * @property firstName The student's first name, used for name-based matching.
 * @property lastName The student's last name, used for name-based matching.
 * @property nickname The student's nickname, preserved for display context.
 */
@Serializable
data class StudentLayout(
    val id: Long,
    val x: Float,
    val y: Float,
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = ""
)

/**
 * Represents the spatial state of a furniture item at the time a layout was saved.
 *
 * @property id The database primary key of the furniture item.
 * @property x The horizontal coordinate on the 4000x4000 logical canvas.
 * @property y The vertical coordinate on the 4000x4000 logical canvas.
 */
@Serializable
data class FurnitureLayout(
    val id: Int,
    val x: Float,
    val y: Float
)

/**
 * A comprehensive spatial snapshot of the classroom's seating chart.
 *
 * This class is the serialized payload stored in the `layoutDataJson` column of the
 * `layout_templates` table. It encapsulates the precise coordinates of every student
 * and furniture item, allowing for the restoration of complex classroom arrangements
 * (e.g., "Exam Mode", "Group Work").
 *
 * @property students The list of student positions and identities.
 * @property furniture The list of furniture positions.
 */
@Serializable
data class LayoutData(
    val students: List<StudentLayout>,
    val furniture: List<FurnitureLayout>
)
