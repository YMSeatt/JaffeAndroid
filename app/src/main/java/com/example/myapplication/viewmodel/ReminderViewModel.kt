package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Reminder
import com.example.myapplication.data.ReminderDao
import com.example.myapplication.util.ReminderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val reminderDao: ReminderDao
    private val reminderManager: ReminderManager
    val allReminders: LiveData<List<Reminder>>

    init {
        val db = AppDatabase.getDatabase(application)
        reminderDao = db.reminderDao()
        allReminders = reminderDao.getAllReminders().asLiveData()
        reminderManager = ReminderManager(application)
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