package com.example.myapplication.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ConditionalFormattingRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: ConditionalFormattingRule)

    @Update
    suspend fun updateRule(rule: ConditionalFormattingRule)

    @Delete
    suspend fun deleteRule(rule: ConditionalFormattingRule)

    @Query("SELECT * FROM conditional_formatting_rules ORDER BY priority ASC")
    fun getAllRules(): LiveData<List<ConditionalFormattingRule>>

    @Query("SELECT * FROM conditional_formatting_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): ConditionalFormattingRule?

    @Query("DELETE FROM conditional_formatting_rules WHERE id = :id")
    suspend fun deleteRuleById(id: Long)
}
