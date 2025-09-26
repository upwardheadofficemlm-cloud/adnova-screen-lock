package com.example.signagekiosk.schedule

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object CommandScheduler {
    fun scheduleSync(context: Context) {
        val req = PeriodicWorkRequestBuilder<SyncCommandsWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "sync_commands",
            ExistingWorkPolicy.KEEP,
            req
        )
    }
}

