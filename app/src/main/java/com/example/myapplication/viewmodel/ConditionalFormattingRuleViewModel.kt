package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.data.ConditionalFormattingRuleDao
import com.example.myapplication.data.CustomBehaviorDao
import com.example.myapplication.data.SystemBehaviorDao
import com.example.myapplication.data.StudentGroupDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the configuration and lifecycle of Conditional Formatting rules.
 *
 * This ViewModel serves as the state manager for the rule builder UI. It aggregates multiple
 * data streams—including user-defined behaviors, system-level behaviors, and student groups—to
 * provide the "available options" for the reactive styling DSL.
 *
 * ### Architectural Role:
 * It bridges the rule editing UI with the persistence layer ([ConditionalFormattingRuleDao]),
 * allowing teachers to define complex visual logic (e.g., highlighting students with 3+ negative
 * logs) that drives the "Fluid Interaction" seating chart experience.
 */
@HiltViewModel
class ConditionalFormattingRuleViewModel @Inject constructor(
    private val conditionalFormattingRuleDao: ConditionalFormattingRuleDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val systemBehaviorDao: SystemBehaviorDao,
    private val studentGroupDao: StudentGroupDao
) : ViewModel() {

    /** Reactive stream of all defined conditional formatting rules. */
    val rules: LiveData<List<ConditionalFormattingRule>> = conditionalFormattingRuleDao.getAllRules()

    /**
     * Stream of teacher-defined behavior categories. Used as triggers in the `behavior_count`
     * condition type.
     */
    val customBehaviors: LiveData<List<com.example.myapplication.data.CustomBehavior>> = customBehaviorDao.getAllCustomBehaviors()

    /**
     * Stream of standardized, system-level feedback categories. Provides consistent triggers
     * for rules across all classrooms.
     */
    val systemBehaviors: LiveData<List<com.example.myapplication.data.SystemBehavior>> =
        systemBehaviorDao.getAllSystemBehaviors().asLiveData()

    /**
     * Stream of all student groups. Used to populate the "Group" condition type, allowing
     * for group-based visual feedback.
     */
    val studentGroups: LiveData<List<com.example.myapplication.data.StudentGroup>> = studentGroupDao.getAllStudentGroups().asLiveData()


    /**
     * Persists a new conditional formatting rule to the database.
     * @param rule The rule entity containing the JSON-based condition and format.
     */
    fun addRule(rule: ConditionalFormattingRule) {
        viewModelScope.launch {
            conditionalFormattingRuleDao.insertRule(rule)
        }
    }

    /**
     * Updates an existing rule's configuration or priority.
     * @param rule The updated rule entity.
     */
    fun updateRule(rule: ConditionalFormattingRule) {
        viewModelScope.launch {
            conditionalFormattingRuleDao.updateRule(rule)
        }
    }

    /**
     * Performs a bulk update for multiple rules. Typically used when reordering rules
     * to adjust their execution priority.
     * @param rules The list of rules with updated properties (e.g., priority).
     */
    fun bulkUpdateRules(rules: List<ConditionalFormattingRule>) {
        viewModelScope.launch {
            conditionalFormattingRuleDao.updateRules(rules)
        }
    }

    /**
     * Removes a rule from the database, permanently disabling its visual effect.
     * @param rule The rule to delete.
     */
    fun deleteRule(rule: ConditionalFormattingRule) {
        viewModelScope.launch {
            conditionalFormattingRuleDao.deleteRule(rule)
        }
    }
}
