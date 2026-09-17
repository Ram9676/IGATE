package com.example.igate

import android.app.Application
import com.example.igate.di.AppContainer
import com.example.igate.di.DefaultAppContainer
import com.example.igate.data.analytics.AnalyticsTracker
import com.example.igate.data.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IgateApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)

        // Initialize Firebase Analytics
        AnalyticsTracker.init(this)

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // Seed mock data for demonstration
        CoroutineScope(Dispatchers.IO).launch {
            container.igateRepository.seedMockDataIfEmpty()
        }
        
        // Initialize Background Sync Engine
        val syncRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.igate.domain.worker.SyncWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        ).build()
        
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "IgateBackgroundSync",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
