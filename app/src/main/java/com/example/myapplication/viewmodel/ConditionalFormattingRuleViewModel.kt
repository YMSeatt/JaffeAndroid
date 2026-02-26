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

@HiltViewModel
class ConditionalFormattingRuleViewModel @Inject constructor(
    private val conditionalFormattingRuleDao: ConditionalFormattingRuleDao,
    private val customBehaviorDao: CustomBehaviorDao,
    private val systemBehaviorDao: SystemBehaviorDao,
    private val studentGroupDao: StudentGroupDao
) : ViewModel() {
    val rules: LiveData<List<ConditionalFormattingRule>> = conditionalFormattingRuleDao.getAllRules()
    val customBehaviors: LiveData<List<com.example.myapplication.data.CustomBehavior>> = customBehaviorDao.getAllCustomBehaviors()
    val systemBehaviors: LiveData<List<com.example.myapplication.data.SystemBehavior>> =
        systemBehaviorDao.getAllSystemBehaviors().asLiveData()
    val studentGroups: LiveData<List<com.example.myapplication.data.StudentGroup>> = studentGroupDao.getAllStudentGroups().asLiveData()


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

    fun bulkUpdateRules(rules: List<ConditionalFormattingRule>) {
        viewModelScope.launch {
            conditionalFormattingRuleDao.updateRules(rules)
        }
    }

    fun deleteRule(rule: ConditionalFormattingRule) {
        viewModelScope.launch {
            conditionalFormattingRuleDao.deleteRule(rule)
        }
    }
}