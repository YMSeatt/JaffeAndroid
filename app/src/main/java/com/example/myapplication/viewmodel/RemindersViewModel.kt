package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.alarm.AlarmScheduler
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Reminder
import com.example.myapplication.data.ReminderDao
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings-based Reminders screen, utilizing the [AlarmScheduler].
 *
 * This ViewModel handles reminder persistence and scheduling specifically for the
 * settings-integrated reminders view.
 * Note: This class overlaps significantly with [ReminderViewModel].
 */
class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val reminderDao: ReminderDao
    private val alarmScheduler: AlarmScheduler

    init {
        val db = AppDatabase.getDatabase(application)
        reminderDao = db.reminderDao()
        alarmScheduler = AlarmScheduler(application)
    }

    /**
     * Observable stream of all reminders.
     */
    val allReminders: LiveData<List<Reminder>> = reminderDao.getAllReminders().asLiveData()

    /**
     * Adds a new reminder and schedules its alarm.
     */
    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            val id = reminderDao.insert(reminder)
            val newReminder = reminder.copy(id = id)
            alarmScheduler.schedule(newReminder)
        }
    }

    /**
     * Deletes a reminder and cancels its scheduled alarm.
     */
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.delete(reminder.id)
            alarmScheduler.cancel(reminder)
        }
    }

    /**
     * Updates an existing reminder and reschedules its alarm.
     */
    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderDao.update(reminder)
            alarmScheduler.schedule(reminder)
        }
    }
}
