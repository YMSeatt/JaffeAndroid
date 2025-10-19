package com.example.myapplication

import android.app.Application
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.util.EmailWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

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