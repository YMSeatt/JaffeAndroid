package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.data.ConditionalFormattingRuleDao
import com.example.myapplication.data.CustomBehaviorDao
import com.example.myapplication.data.SystemBehaviorDao
import kotlinx.coroutines.launch

class ConditionalFormattingRuleViewModel(
    private val conditionalFormattingRuleDao: ConditionalFormattingRuleDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val systemBehaviorDao: SystemBehaviorDao
) : ViewModel() {
    val rules: LiveData<List<ConditionalFormattingRule>> = conditionalFormattingRuleDao.getAllRules()
    val customBehaviors: LiveData<List<com.example.myapplication.data.CustomBehavior>> = customBehaviorDao.getAllCustomBehaviors()
    val systemBehaviors: LiveData<List<com.example.myapplication.data.SystemBehavior>> = systemBehaviorDao.getAllSystemBehaviors()

    fun addRule(rule: ConditionalFormattingRule) {
        viewModelScope.launch {
            conditionalFormattingRuleDao.insertRule(rule)
        }
    }

    fun updateRule(rule: ConditionalFormattingRule) {
        viewModelScope.launch {
            conditionalFormattingRuleDao.updateRule(rule)
        }
    }

    fun deleteRule(rule: ConditionalFormattingRule) {
        viewModelScope.launch {
            conditionalFormattingRuleDao.deleteRule(rule)
        }
    }
}