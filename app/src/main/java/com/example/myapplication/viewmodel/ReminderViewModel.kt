package com.example.myapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.myapplication.data.Reminder
import com.example.myapplication.data.ReminderDao
import com.example.myapplication.util.ReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderDao: ReminderDao,
    private val reminderManager: ReminderManager
) : ViewModel() {

    val allReminders: LiveData<List<Reminder>>

    init {
        allReminders = reminderDao.getAllReminders().asLiveData()
    }

    fun canScheduleExactAlarms(): Boolean {
        return reminderManager.canScheduleExactAlarms()
    }

    fun insert(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = reminderDao.insert(reminder)
            val newReminder = reminder.copy(id = id)
            reminderManager.scheduleReminder(newReminder)
        }
    }

    fun update(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.update(reminder)
            reminderManager.scheduleReminder(reminder)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.delete(id)
            reminderManager.cancelReminder(id)
        }
    }
}