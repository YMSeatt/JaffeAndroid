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

/**
 * ViewModel for the Reminders screen, utilizing the [ReminderManager] utility.
 *
 * This ViewModel manages the lifecycle of teacher reminders, coordinating between
 * the Room database ([ReminderDao]) and the system's [android.app.AlarmManager]
 * via [ReminderManager].
 */
@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val reminderDao: ReminderDao,
    private val reminderManager: ReminderManager
) : ViewModel() {

    /**
     * Observable stream of all reminders stored in the database.
     */
    val allReminders: LiveData<List<Reminder>>

    init {
        allReminders = reminderDao.getAllReminders().asLiveData()
    }

    /**
     * Checks if the app has permission to schedule exact alarms on Android 12+.
     */
    fun canScheduleExactAlarms(): Boolean {
        return reminderManager.canScheduleExactAlarms()
    }

    /**
     * Saves a new reminder to the database and schedules its notification.
     *
     * @param reminder The reminder to insert and schedule.
     */
    fun insert(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = reminderDao.insert(reminder)
            val newReminder = reminder.copy(id = id)
            reminderManager.scheduleReminder(newReminder)
        }
    }

    /**
     * Updates an existing reminder and reschedules its notification.
     */
    fun update(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.update(reminder)
            reminderManager.scheduleReminder(reminder)
        }
    }

    /**
     * Deletes a reminder from the database and cancels its scheduled notification.
     *
     * @param id The unique identifier of the reminder to delete.
     */
    fun delete(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.delete(id)
            reminderManager.cancelReminder(id)
        }
    }
}