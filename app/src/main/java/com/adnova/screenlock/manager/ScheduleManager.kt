package com.adnova.screenlock.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.adnova.screenlock.data.LockConfiguration
import com.adnova.screenlock.data.PreferencesManager
import com.adnova.screenlock.service.LockOverlayService
import com.adnova.screenlock.service.ScheduleReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Manages scheduled lock/unlock operations
 */
class ScheduleManager(private val context: Context) {
    
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val TAG = "ScheduleManager"
        private const val ACTION_SCHEDULED_LOCK = "com.adnova.screenlock.SCHEDULED_LOCK"
        private const val ACTION_SCHEDULED_UNLOCK = "com.adnova.screenlock.SCHEDULED_UNLOCK"
        private const val REQUEST_CODE_LOCK = 1001
        private const val REQUEST_CODE_UNLOCK = 1002
    }
    
    /**
     * Schedule automatic lock at specified time
     */
    fun scheduleLock(hour: Int, minute: Int, days: List<Int> = listOf()) {
        try {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If time has passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val intent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_SCHEDULED_LOCK
                putExtra("hour", hour)
                putExtra("minute", minute)
                putExtra("days", days.toIntArray())
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_LOCK,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (days.isEmpty()) {
                // Daily schedule
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else {
                // Weekly schedule
                scheduleWeeklyLock(calendar, days, pendingIntent)
            }
            
            // Save schedule to preferences
            preferencesManager.saveScheduleConfig(
                ScheduleConfig(
                    lockTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}",
                    unlockTime = null,
                    days = days,
                    enabled = true
                )
            )
            
            Log.d(TAG, "Scheduled lock for $hour:$minute on days: $days")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule lock", e)
        }
    }
    
    /**
     * Schedule automatic unlock at specified time
     */
    fun scheduleUnlock(hour: Int, minute: Int, days: List<Int> = listOf()) {
        try {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If time has passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            val intent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_SCHEDULED_UNLOCK
                putExtra("hour", hour)
                putExtra("minute", minute)
                putExtra("days", days.toIntArray())
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_UNLOCK,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (days.isEmpty()) {
                // Daily schedule
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            } else {
                // Weekly schedule
                scheduleWeeklyUnlock(calendar, days, pendingIntent)
            }
            
            // Update schedule config
            val currentConfig = preferencesManager.getScheduleConfig()
            preferencesManager.saveScheduleConfig(
                currentConfig.copy(
                    unlockTime = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                )
            )
            
            Log.d(TAG, "Scheduled unlock for $hour:$minute on days: $days")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule unlock", e)
        }
    }
    
    /**
     * Schedule weekly lock operations
     */
    private fun scheduleWeeklyLock(calendar: Calendar, days: List<Int>, pendingIntent: PendingIntent) {
        days.forEach { dayOfWeek ->
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
            
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                dayCalendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
    }
    
    /**
     * Schedule weekly unlock operations
     */
    private fun scheduleWeeklyUnlock(calendar: Calendar, days: List<Int>, pendingIntent: PendingIntent) {
        days.forEach { dayOfWeek ->
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
            
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                dayCalendar.timeInMillis,
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
            )
        }
    }
    
    /**
     * Cancel all scheduled operations
     */
    fun cancelAllSchedules() {
        try {
            val lockIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_SCHEDULED_LOCK
            }
            val lockPendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_LOCK,
                lockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(lockPendingIntent)
            
            val unlockIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_SCHEDULED_UNLOCK
            }
            val unlockPendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_UNLOCK,
                unlockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(unlockPendingIntent)
            
            // Clear schedule config
            preferencesManager.saveScheduleConfig(
                ScheduleConfig(
                    lockTime = null,
                    unlockTime = null,
                    days = emptyList(),
                    enabled = false
                )
            )
            
            Log.d(TAG, "All schedules cancelled")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel schedules", e)
        }
    }
    
    /**
     * Get current schedule configuration
     */
    fun getScheduleConfig(): ScheduleConfig {
        return preferencesManager.getScheduleConfig()
    }
    
    /**
     * Check if current time matches any scheduled operation
     */
    fun checkScheduledOperations() {
        val config = getScheduleConfig()
        if (!config.enabled) return
        
        val now = Calendar.getInstance()
        val currentTime = "${now.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')}:${now.get(Calendar.MINUTE).toString().padStart(2, '0')}"
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        
        // Check if today is in the scheduled days
        val isScheduledDay = config.days.isEmpty() || config.days.contains(currentDay)
        
        if (isScheduledDay) {
            config.lockTime?.let { lockTime ->
                if (currentTime == lockTime) {
                    executeScheduledLock()
                }
            }
            
            config.unlockTime?.let { unlockTime ->
                if (currentTime == unlockTime) {
                    executeScheduledUnlock()
                }
            }
        }
    }
    
    /**
     * Execute scheduled lock operation
     */
    private fun executeScheduledLock() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val intent = Intent(context, LockOverlayService::class.java).apply {
                    putExtra("action", "lock")
                    putExtra("scheduled", true)
                }
                context.startForegroundService(intent)
                
                Log.d(TAG, "Scheduled lock executed")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute scheduled lock", e)
            }
        }
    }
    
    /**
     * Execute scheduled unlock operation
     */
    private fun executeScheduledUnlock() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val intent = Intent(context, LockOverlayService::class.java).apply {
                    putExtra("action", "unlock")
                    putExtra("scheduled", true)
                }
                context.stopService(intent)
                
                Log.d(TAG, "Scheduled unlock executed")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute scheduled unlock", e)
            }
        }
    }
    
    /**
     * Schedule lock for specific duration
     */
    fun scheduleLockForDuration(durationMinutes: Int) {
        try {
            val calendar = Calendar.getInstance().apply {
                add(Calendar.MINUTE, durationMinutes)
            }
            
            val intent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_SCHEDULED_UNLOCK
                putExtra("duration", durationMinutes)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_UNLOCK + 1000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            
            Log.d(TAG, "Scheduled unlock after $durationMinutes minutes")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule duration lock", e)
        }
    }
}

/**
 * Data class for schedule configuration
 */
data class ScheduleConfig(
    val lockTime: String? = null,
    val unlockTime: String? = null,
    val days: List<Int> = emptyList(),
    val enabled: Boolean = false
)
