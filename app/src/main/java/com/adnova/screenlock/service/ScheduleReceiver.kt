package com.adnova.screenlock.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.adnova.screenlock.manager.ScheduleManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Broadcast receiver for handling scheduled lock/unlock operations
 */
class ScheduleReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ScheduleReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "ScheduleReceiver received: ${intent.action}")
        
        when (intent.action) {
            "com.adnova.screenlock.SCHEDULED_LOCK" -> {
                handleScheduledLock(context, intent)
            }
            "com.adnova.screenlock.SCHEDULED_UNLOCK" -> {
                handleScheduledUnlock(context, intent)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                handleBootCompleted(context)
            }
        }
    }
    
    /**
     * Handle scheduled lock operation
     */
    private fun handleScheduledLock(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val hour = intent.getIntExtra("hour", 0)
                val minute = intent.getIntExtra("minute", 0)
                val days = intent.getIntArrayExtra("days")?.toList() ?: emptyList()
                
                Log.d(TAG, "Executing scheduled lock for $hour:$minute on days: $days")
                
                // Start lock service
                val lockIntent = Intent(context, LockOverlayService::class.java).apply {
                    putExtra("action", "lock")
                    putExtra("scheduled", true)
                    putExtra("scheduleTime", "$hour:$minute")
                }
                context.startForegroundService(lockIntent)
                
                // Log the event
                logScheduledEvent(context, "scheduled_lock", mapOf(
                    "hour" to hour,
                    "minute" to minute,
                    "days" to days
                ))
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle scheduled lock", e)
            }
        }
    }
    
    /**
     * Handle scheduled unlock operation
     */
    private fun handleScheduledUnlock(context: Context, intent: Intent) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val hour = intent.getIntExtra("hour", 0)
                val minute = intent.getIntExtra("minute", 0)
                val days = intent.getIntArrayExtra("days")?.toList() ?: emptyList()
                val duration = intent.getIntExtra("duration", -1)
                
                Log.d(TAG, "Executing scheduled unlock for $hour:$minute on days: $days")
                
                // Stop lock service
                val lockIntent = Intent(context, LockOverlayService::class.java)
                context.stopService(lockIntent)
                
                // Log the event
                val eventData = mutableMapOf<String, Any>(
                    "hour" to hour,
                    "minute" to minute,
                    "days" to days
                )
                if (duration > 0) {
                    eventData["duration"] = duration
                }
                
                logScheduledEvent(context, "scheduled_unlock", eventData)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle scheduled unlock", e)
            }
        }
    }
    
    /**
     * Handle boot completed - reschedule all operations
     */
    private fun handleBootCompleted(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Boot completed, rescheduling operations")
                
                val scheduleManager = ScheduleManager(context)
                val config = scheduleManager.getScheduleConfig()
                
                if (config.enabled) {
                    // Reschedule lock if configured
                    config.lockTime?.let { lockTime ->
                        val timeParts = lockTime.split(":")
                        if (timeParts.size == 2) {
                            val hour = timeParts[0].toIntOrNull() ?: 0
                            val minute = timeParts[1].toIntOrNull() ?: 0
                            scheduleManager.scheduleLock(hour, minute, config.days)
                        }
                    }
                    
                    // Reschedule unlock if configured
                    config.unlockTime?.let { unlockTime ->
                        val timeParts = unlockTime.split(":")
                        if (timeParts.size == 2) {
                            val hour = timeParts[0].toIntOrNull() ?: 0
                            val minute = timeParts[1].toIntOrNull() ?: 0
                            scheduleManager.scheduleUnlock(hour, minute, config.days)
                        }
                    }
                }
                
                Log.d(TAG, "Operations rescheduled successfully")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reschedule operations after boot", e)
            }
        }
    }
    
    /**
     * Log scheduled event for analytics
     */
    private fun logScheduledEvent(context: Context, eventType: String, data: Map<String, Any>) {
        try {
            // This would integrate with your analytics system
            Log.d(TAG, "Scheduled event: $eventType with data: $data")
            
            // You could send this to Firebase Analytics or your preferred analytics service
            // FirebaseAnalytics.getInstance(context).logEvent(eventType, Bundle().apply {
            //     data.forEach { (key, value) ->
            //         putString(key, value.toString())
            //     }
            // })
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log scheduled event", e)
        }
    }
}
