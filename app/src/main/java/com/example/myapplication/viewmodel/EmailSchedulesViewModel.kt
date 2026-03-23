package com.example.myapplication.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.data.EmailRepository
import com.example.myapplication.data.EmailSchedule
import com.example.myapplication.worker.EmailSchedulerWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * EmailSchedulesViewModel: Manages the lifecycle of automated classroom reporting schedules.
 *
 * This ViewModel serves as the bridge between the [EmailRepository] and the UI (EmailSchedulesScreen).
 * It provides a reactive stream of defined schedules and coordinates the registration of
 * background workers to ensure reports are triggered at the correct times.
 *
 * ### Architectural Role:
 * - **State Management**: Exposes a [StateFlow] of [EmailSchedule] entities, automatically
 *   mapping encrypted database fields to plaintext for UI presentation.
 * - **Worker Coordination**: Whenever a schedule is added, updated, or deleted, this ViewModel
 *   triggers a "Refresh" of the [EmailSchedulerWorker] via [WorkManager].
 */
@HiltViewModel
class EmailSchedulesViewModel @Inject constructor(
    private val emailRepository: EmailRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)

    /**
     * A reactive stream of all automated email schedules.
     * The list is automatically kept in sync with the database and is scoped to
     * the [viewModelScope] with a 5-second subscription buffer.
     */
    val schedules = emailRepository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Adds a new automated report schedule and refreshes the background scheduler.
     */
    fun addSchedule(schedule: EmailSchedule) {
        viewModelScope.launch {
            emailRepository.insertSchedule(schedule)
            scheduleNext()
        }
    }

    /**
     * Updates an existing report schedule and refreshes the background scheduler.
     */
    fun updateSchedule(schedule: EmailSchedule) {
        viewModelScope.launch {
            emailRepository.updateSchedule(schedule)
            scheduleNext()
        }
    }

    /**
     * Permanently removes a report schedule and refreshes the background scheduler.
     */
    fun deleteSchedule(schedule: EmailSchedule) {
        viewModelScope.launch {
            emailRepository.deleteSchedule(schedule)
            scheduleNext()
        }
    }

    /**
     * Registers or refreshes the [EmailSchedulerWorker] in [WorkManager].
     *
     * Utilizes a [PeriodicWorkRequestBuilder] with a 15-minute interval. This worker
     * acts as the primary evaluator that determines when individual reports
     * should be dispatched to the SMTP server.
     */
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
