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

        // Initialize Firebase Analytics safely
        try {
            AnalyticsTracker.init(this)
        } catch (e: Exception) {
            android.util.Log.w("IgateApplication", "Analytics init failed: ${e.message}")
        }

        // Create notification channels safely
        try {
            NotificationHelper.createNotificationChannels(this)
        } catch (e: Exception) {
            android.util.Log.w("IgateApplication", "Notification channel creation failed: ${e.message}")
        }

        // Seed mock data safely in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                container.igateRepository.seedMockDataIfEmpty()
            } catch (e: Throwable) {
                android.util.Log.w("IgateApplication", "Mock data seeding skipped: ${e.message}")
            }
        }
        
        // Initialize Background Sync Engine safely
        try {
            val syncRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.igate.domain.worker.SyncWorker>(
                15, java.util.concurrent.TimeUnit.MINUTES
            ).build()
            
            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "IgateBackgroundSync",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        } catch (e: Exception) {
            android.util.Log.w("IgateApplication", "WorkManager background sync init skipped: ${e.message}")
        }
    }
}
