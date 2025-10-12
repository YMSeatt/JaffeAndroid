package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Reminder
import com.example.myapplication.data.ReminderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val reminderDao: ReminderDao
    val allReminders: LiveData<List<Reminder>>

    init {
        val db = AppDatabase.getDatabase(application)
        reminderDao = db.reminderDao()
        allReminders = reminderDao.getAllReminders().asLiveData()
    }

    fun insert(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.insert(reminder)
        }
    }

    fun update(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.update(reminder)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.delete(id)
        }
    }
}