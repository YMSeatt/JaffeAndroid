package com.example.myapplication

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.util.EmailWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

/**
 * MyApplication: The global application container and initialization hub.
 *
 * This class orchestrates the foundational services of the application, including
 * dependency injection and background task scheduling.
 *
 * ### Key Roles:
 * 1. **Dependency Injection**: Bootstraps the [Hilt] graph via the [@HiltAndroidApp]
 *    annotation, enabling property injection across activities and ViewModels.
 * 2. **Background Automation**: Acts as the centralized point for scheduling periodic
 *    [WorkManager] tasks, such as automated classroom reports.
 */
@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("MyApplication", "onCreate")
        // scheduleEmailWorker()
    }

    private fun scheduleEmailWorker() {
        val workRequest = PeriodicWorkRequestBuilder<EmailWorker>(15, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "EmailWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}