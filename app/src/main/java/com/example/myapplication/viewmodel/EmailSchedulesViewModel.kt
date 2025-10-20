package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.data.EmailSchedule
import com.example.myapplication.data.EmailScheduleDao
import com.example.myapplication.worker.EmailSchedulerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class EmailSchedulesViewModel @Inject constructor(
    private val emailScheduleDao: EmailScheduleDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    val schedules = emailScheduleDao.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSchedule(schedule: EmailSchedule) {
        viewModelScope.launch {
            emailScheduleDao.insert(schedule)
            scheduleNext()
        }
    }

    fun updateSchedule(schedule: EmailSchedule) {
        viewModelScope.launch {
            emailScheduleDao.update(schedule)
            scheduleNext()
        }
    }

    fun deleteSchedule(schedule: EmailSchedule) {
        viewModelScope.launch {
            emailScheduleDao.delete(schedule)
            scheduleNext()
        }
    }

    private fun scheduleNext() {
        val workRequest = PeriodicWorkRequestBuilder<EmailSchedulerWorker>(15, TimeUnit.MINUTES)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "email_scheduler",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
}
