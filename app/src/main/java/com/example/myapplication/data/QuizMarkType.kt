package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines a category of mark that can be applied to a quiz question.
 *
 * This entity serves as the metadata definition for the granular marks stored in the
 * `marksData` JSON field of [QuizLog]. It determines how individual marks (e.g., "Half Credit",
 * "Correct") translate into numeric points during score calculation.
 *
 * ### Scoring Heuristics:
 * - **Denominator Selection**: The engine (see [com.example.myapplication.util.QuizScoreEngine])
 *   looks for a mark type named **"Correct"** that has [contributesToTotal] set to `true`.
 *   The [defaultPoints] of this mark type is multiplied by `QuizLog.numQuestions` to
 *   establish the maximum possible points (the denominator).
 * - **Point Contribution**: Only mark types where [contributesToTotal] is `true` are summed
 *   into the earned points (the numerator) by default.
 * - **Extra Credit**: Marks flagged with [isExtraCredit] contribute to the earned points
 *   but are generally excluded from the denominator calculation.
 */
@Entity(tableName = "quiz_mark_types")
data class QuizMarkType(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /**
     * The human-readable label for the mark (e.g., "Correct", "Participation").
     * **Warning**: The scoring engine relies on the name "Correct" (case-insensitive)
     * as a heuristic for the standard "full credit" mark.
     */
    val name: String,
    /** The number of points awarded for a single occurrence of this mark. */
    val defaultPoints: Double,
    /** Whether occurrences of this mark should be summed toward the total quiz score. */
    val contributesToTotal: Boolean,
    /** Whether this mark represents bonus points above the standard quiz total. */
    val isExtraCredit: Boolean
)
