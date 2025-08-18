package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.ConditionalFormattingRule
import com.example.myapplication.data.ConditionalFormattingRuleDao
import kotlinx.coroutines.launch

class ConditionalFormattingRuleViewModel(private val conditionalFormattingRuleDao: ConditionalFormattingRuleDao) : ViewModel() {
    val rules: LiveData<List<ConditionalFormattingRule>> = conditionalFormattingRuleDao.getAllRules()

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