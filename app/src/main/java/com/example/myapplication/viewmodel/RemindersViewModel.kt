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
 * RemindersViewModel: A secondary, legacy coordinator for teacher reminders.
 *
 * This ViewModel serves the settings-integrated reminders view but overlaps
 * significantly with the primary [ReminderViewModel]. It utilizes the legacy
 * [AlarmScheduler] and manual database access rather than Hilt-driven injection.
 *
 * ### ⚠️ Redundancy Note:
 * This component is considered **legacy**. New features should utilize
 * [ReminderViewModel] and the associated primary [com.example.myapplication.util.ReminderManager].
 */
@Deprecated(
    message = "Use ReminderViewModel for the primary reminder implementation.",
    replaceWith = ReplaceWith("ReminderViewModel", "com.example.myapplication.viewmodel.ReminderViewModel")
)
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
