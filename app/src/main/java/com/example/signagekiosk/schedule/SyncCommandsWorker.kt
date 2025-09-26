package com.example.signagekiosk.schedule

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.signagekiosk.kiosk.KioskManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class SyncCommandsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val deviceId = android.os.Build.SERIAL ?: "unknown"
            val db = FirebaseFirestore.getInstance()
            val cmds = db.collection("devices").document(deviceId).collection("commands")
                .whereEqualTo("state", "pending").get().await()
            val kiosk = KioskManager(applicationContext)
            for (doc in cmds.documents) {
                when (doc.getString("action")) {
                    "touchLock" -> kiosk.lockTouch(true)
                    "touchUnlock" -> kiosk.lockTouch(false)
                    "sleep" -> kiosk.sleepScreen()
                    "wake" -> kiosk.wakeScreen()
                    "reboot" -> kiosk.rebootDevice()
                }
                doc.reference.update("state", "done")
            }
            Result.success()
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}

