package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object for conditional formatting rules.
 *
 * This DAO manages the persistence of [ConditionalFormattingRule] entities, which
 * drive the seating chart's reactive styling engine. Rules are prioritized by an
 * integer value to ensure deterministic style resolution when multiple conditions match.
 */
@Dao
interface ConditionalFormattingRuleDao {
    /**
     * Saves a new rule or replaces an existing one if their IDs match.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ConditionalFormattingRule)

    /**
     * Updates an existing rule in the database.
     */
    @Update
    suspend fun updateRule(rule: ConditionalFormattingRule)

    /**
     * Performs a bulk update of multiple rules, typically used when reordering priorities.
     */
    @Update
    suspend fun updateRules(rules: List<ConditionalFormattingRule>)

    /**
     * Removes a rule from the database.
     */
    @Delete
    suspend fun deleteRule(rule: ConditionalFormattingRule)

    /**
     * Retrieves all rules, sorted by their execution priority.
     * @return A reactive [LiveData] stream of rule entities.
     */
    @Query("SELECT * FROM conditional_formatting_rules ORDER BY priority ASC")
    fun getAllRules(): LiveData<List<ConditionalFormattingRule>>

    /**
     * Fetches a specific rule by its unique database ID.
     */
    @Query("SELECT * FROM conditional_formatting_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): ConditionalFormattingRule?

    /**
     * Deletes a rule identified by its primary key.
     */
    @Query("DELETE FROM conditional_formatting_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)
}
