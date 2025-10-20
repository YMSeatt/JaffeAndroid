package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.alarm.AlarmScheduler
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Reminder
import com.example.myapplication.data.ReminderDao
import kotlinx.coroutines.launch

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val reminderDao: ReminderDao
    private val alarmScheduler: AlarmScheduler

    init {
        val db = AppDatabase.getDatabase(application)
        reminderDao = db.reminderDao()
        alarmScheduler = AlarmScheduler(application)
    }

    val allReminders: LiveData<List<Reminder>> = reminderDao.getAllReminders()

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            val id = reminderDao.insert(reminder)
            val newReminder = reminder.copy(id = id)
            alarmScheduler.schedule(newReminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.delete(reminder)
            alarmScheduler.cancel(reminder)
        }
    }
}
