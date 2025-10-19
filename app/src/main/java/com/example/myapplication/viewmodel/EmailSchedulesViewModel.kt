package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.EmailSchedule
import com.example.myapplication.data.EmailScheduleDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmailSchedulesViewModel @Inject constructor(
    private val emailScheduleDao: EmailScheduleDao
) : ViewModel() {

    val schedules = emailScheduleDao.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSchedule(schedule: EmailSchedule) {
        viewModelScope.launch {
            emailScheduleDao.insert(schedule)
        }
    }

    fun updateSchedule(schedule: EmailSchedule) {
        viewModelScope.launch {
            emailScheduleDao.update(schedule)
        }
    }

    fun deleteSchedule(schedule: EmailSchedule) {
        viewModelScope.launch {
            emailScheduleDao.delete(schedule)
        }
    }
}
