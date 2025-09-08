package com.adnova.screenlock.manager

import android.content.Context
import android.util.Log
import com.adnova.screenlock.data.PreferencesManager
import com.adnova.screenlock.firebase.FirebaseManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Advanced analytics and reporting manager
 */
class AnalyticsManager(private val context: Context) {
    
    private val preferencesManager = PreferencesManager(context)
    private val firebaseManager = FirebaseManager(context)
    
    companion object {
        private const val TAG = "AnalyticsManager"
        private const val ANALYTICS_ENABLED_KEY = "analytics_enabled"
        private const val SESSION_START_KEY = "session_start_time"
        private const val TOTAL_SESSIONS_KEY = "total_sessions"
        private const val TOTAL_LOCK_TIME_KEY = "total_lock_time"
        private const val TOTAL_UNLOCK_EVENTS_KEY = "total_unlock_events"
    }
    
    /**
     * Enable/disable analytics
     */
    fun setAnalyticsEnabled(enabled: Boolean) {
        preferencesManager.saveBoolean(ANALYTICS_ENABLED_KEY, enabled)
        Log.d(TAG, "Analytics enabled: $enabled")
    }
    
    /**
     * Check if analytics is enabled
     */
    fun isAnalyticsEnabled(): Boolean {
        return preferencesManager.getBoolean(ANALYTICS_ENABLED_KEY, true)
    }
    
    /**
     * Start analytics session
     */
    fun startSession() {
        if (!isAnalyticsEnabled()) return
        
        try {
            val sessionStartTime = System.currentTimeMillis()
            preferencesManager.saveLong(SESSION_START_KEY, sessionStartTime)
            
            val totalSessions = preferencesManager.getInt(TOTAL_SESSIONS_KEY, 0) + 1
            preferencesManager.saveInt(TOTAL_SESSIONS_KEY, totalSessions)
            
            logEvent("session_start", mapOf(
                "session_id" to sessionStartTime,
                "total_sessions" to totalSessions
            ))
            
            Log.d(TAG, "Analytics session started")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start analytics session", e)
        }
    }
    
    /**
     * End analytics session
     */
    fun endSession() {
        if (!isAnalyticsEnabled()) return
        
        try {
            val sessionStartTime = preferencesManager.getLong(SESSION_START_KEY, 0)
            if (sessionStartTime > 0) {
                val sessionDuration = System.currentTimeMillis() - sessionStartTime
                
                logEvent("session_end", mapOf(
                    "session_id" to sessionStartTime,
                    "duration" to sessionDuration
                ))
                
                preferencesManager.saveLong(SESSION_START_KEY, 0)
                Log.d(TAG, "Analytics session ended")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to end analytics session", e)
        }
    }
    
    /**
     * Log lock event
     */
    fun logLockEvent(lockType: String, duration: Long? = null) {
        if (!isAnalyticsEnabled()) return
        
        try {
            val eventData = mutableMapOf<String, Any>(
                "lock_type" to lockType,
                "timestamp" to System.currentTimeMillis()
            )
            
            duration?.let { eventData["duration"] = it }
            
            logEvent("lock_event", eventData)
            
            // Update total lock time
            val totalLockTime = preferencesManager.getLong(TOTAL_LOCK_TIME_KEY, 0)
            preferencesManager.saveLong(TOTAL_LOCK_TIME_KEY, totalLockTime + (duration ?: 0))
            
            Log.d(TAG, "Lock event logged: $lockType")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log lock event", e)
        }
    }
    
    /**
     * Log unlock event
     */
    fun logUnlockEvent(unlockMethod: String, lockDuration: Long? = null) {
        if (!isAnalyticsEnabled()) return
        
        try {
            val eventData = mutableMapOf<String, Any>(
                "unlock_method" to unlockMethod,
                "timestamp" to System.currentTimeMillis()
            )
            
            lockDuration?.let { eventData["lock_duration"] = it }
            
            logEvent("unlock_event", eventData)
            
            // Update total unlock events
            val totalUnlockEvents = preferencesManager.getInt(TOTAL_UNLOCK_EVENTS_KEY, 0) + 1
            preferencesManager.saveInt(TOTAL_UNLOCK_EVENTS_KEY, totalUnlockEvents)
            
            Log.d(TAG, "Unlock event logged: $unlockMethod")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log unlock event", e)
        }
    }
    
    /**
     * Log error event
     */
    fun logErrorEvent(error: String, details: String? = null) {
        if (!isAnalyticsEnabled()) return
        
        try {
            val eventData = mutableMapOf<String, Any>(
                "error" to error,
                "timestamp" to System.currentTimeMillis()
            )
            
            details?.let { eventData["details"] = it }
            
            logEvent("error_event", eventData)
            
            Log.d(TAG, "Error event logged: $error")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log error event", e)
        }
    }
    
    /**
     * Log feature usage
     */
    fun logFeatureUsage(feature: String, action: String, metadata: Map<String, Any> = emptyMap()) {
        if (!isAnalyticsEnabled()) return
        
        try {
            val eventData = mutableMapOf<String, Any>(
                "feature" to feature,
                "action" to action,
                "timestamp" to System.currentTimeMillis()
            )
            
            eventData.putAll(metadata)
            
            logEvent("feature_usage", eventData)
            
            Log.d(TAG, "Feature usage logged: $feature - $action")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log feature usage", e)
        }
    }
    
    /**
     * Log performance metrics
     */
    fun logPerformanceMetrics(metrics: Map<String, Any>) {
        if (!isAnalyticsEnabled()) return
        
        try {
            val eventData = mutableMapOf<String, Any>(
                "timestamp" to System.currentTimeMillis()
            )
            
            eventData.putAll(metrics)
            
            logEvent("performance_metrics", eventData)
            
            Log.d(TAG, "Performance metrics logged")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log performance metrics", e)
        }
    }
    
    /**
     * Get usage statistics
     */
    fun getUsageStatistics(): UsageStatistics {
        return try {
            UsageStatistics(
                totalSessions = preferencesManager.getInt(TOTAL_SESSIONS_KEY, 0),
                totalLockTime = preferencesManager.getLong(TOTAL_LOCK_TIME_KEY, 0),
                totalUnlockEvents = preferencesManager.getInt(TOTAL_UNLOCK_EVENTS_KEY, 0),
                averageSessionDuration = calculateAverageSessionDuration(),
                mostUsedLockType = getMostUsedLockType(),
                mostUsedUnlockMethod = getMostUsedUnlockMethod(),
                errorCount = getErrorCount()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get usage statistics", e)
            UsageStatistics()
        }
    }
    
    /**
     * Generate usage report
     */
    fun generateUsageReport(): UsageReport {
        return try {
            val statistics = getUsageStatistics()
            val reportDate = Date()
            
            UsageReport(
                reportDate = reportDate,
                statistics = statistics,
                recommendations = generateRecommendations(statistics),
                insights = generateInsights(statistics)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate usage report", e)
            UsageReport()
        }
    }
    
    /**
     * Export analytics data
     */
    fun exportAnalyticsData(): String {
        return try {
            val statistics = getUsageStatistics()
            val report = generateUsageReport()
            
            """
            {
                "exportDate": "${Date()}",
                "statistics": {
                    "totalSessions": ${statistics.totalSessions},
                    "totalLockTime": ${statistics.totalLockTime},
                    "totalUnlockEvents": ${statistics.totalUnlockEvents},
                    "averageSessionDuration": ${statistics.averageSessionDuration},
                    "mostUsedLockType": "${statistics.mostUsedLockType}",
                    "mostUsedUnlockMethod": "${statistics.mostUsedUnlockMethod}",
                    "errorCount": ${statistics.errorCount}
                },
                "recommendations": ${report.recommendations.joinToString("\"", "\"", "\"") { it }},
                "insights": ${report.insights.joinToString("\"", "\"", "\"") { it }}
            }
            """.trimIndent()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export analytics data", e)
            "{}"
        }
    }
    
    /**
     * Log event to Firebase
     */
    private fun logEvent(eventName: String, parameters: Map<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // This would integrate with Firebase Analytics
                // FirebaseAnalytics.getInstance(context).logEvent(eventName, Bundle().apply {
                //     parameters.forEach { (key, value) ->
                //         when (value) {
                //             is String -> putString(key, value)
                //             is Long -> putLong(key, value)
                //             is Int -> putInt(key, value)
                //             is Double -> putDouble(key, value)
                //             is Boolean -> putBoolean(key, value)
                //         }
                //     }
                // })
                
                Log.d(TAG, "Event logged: $eventName with parameters: $parameters")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log event to Firebase", e)
            }
        }
    }
    
    /**
     * Calculate average session duration
     */
    private fun calculateAverageSessionDuration(): Long {
        // This would calculate based on stored session data
        return 0L
    }
    
    /**
     * Get most used lock type
     */
    private fun getMostUsedLockType(): String {
        // This would analyze stored lock events
        return "full"
    }
    
    /**
     * Get most used unlock method
     */
    private fun getMostUsedUnlockMethod(): String {
        // This would analyze stored unlock events
        return "floating_button"
    }
    
    /**
     * Get error count
     */
    private fun getErrorCount(): Int {
        // This would count stored error events
        return 0
    }
    
    /**
     * Generate recommendations based on usage
     */
    private fun generateRecommendations(statistics: UsageStatistics): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (statistics.totalSessions > 100) {
            recommendations.add("Consider using scheduled locking for better automation")
        }
        
        if (statistics.errorCount > 10) {
            recommendations.add("Review error logs and consider updating the app")
        }
        
        if (statistics.averageSessionDuration > 3600000) { // 1 hour
            recommendations.add("Long sessions detected - consider battery optimization")
        }
        
        return recommendations
    }
    
    /**
     * Generate insights based on usage
     */
    private fun generateInsights(statistics: UsageStatistics): List<String> {
        val insights = mutableListOf<String>()
        
        insights.add("Most used lock type: ${statistics.mostUsedLockType}")
        insights.add("Most used unlock method: ${statistics.mostUsedUnlockMethod}")
        insights.add("Total usage time: ${statistics.totalLockTime / 1000 / 60} minutes")
        
        return insights
    }
}

/**
 * Data class for usage statistics
 */
data class UsageStatistics(
    val totalSessions: Int = 0,
    val totalLockTime: Long = 0,
    val totalUnlockEvents: Int = 0,
    val averageSessionDuration: Long = 0,
    val mostUsedLockType: String = "",
    val mostUsedUnlockMethod: String = "",
    val errorCount: Int = 0
)

/**
 * Data class for usage report
 */
data class UsageReport(
    val reportDate: Date = Date(),
    val statistics: UsageStatistics = UsageStatistics(),
    val recommendations: List<String> = emptyList(),
    val insights: List<String> = emptyList()
)
