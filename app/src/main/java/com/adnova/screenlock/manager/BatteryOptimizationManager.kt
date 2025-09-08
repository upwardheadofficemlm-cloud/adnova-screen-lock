package com.adnova.screenlock.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.adnova.screenlock.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Manages battery optimization and power management
 */
class BatteryOptimizationManager(private val context: Context) {
    
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val TAG = "BatteryOptimizationManager"
        private const val BATTERY_OPTIMIZATION_KEY = "battery_optimization_enabled"
        private const val LOW_BATTERY_THRESHOLD = 20
        private const val CRITICAL_BATTERY_THRESHOLD = 10
    }
    
    /**
     * Check if battery optimization is enabled for the app
     */
    fun isBatteryOptimizationEnabled(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }
    
    /**
     * Request battery optimization exemption
     */
    fun requestBatteryOptimizationExemption() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to request battery optimization exemption", e)
                // Fallback to general battery optimization settings
                openBatteryOptimizationSettings()
            }
        }
    }
    
    /**
     * Open battery optimization settings
     */
    fun openBatteryOptimizationSettings() {
        try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open battery optimization settings", e)
        }
    }
    
    /**
     * Get current battery level
     */
    fun getBatteryLevel(): Int {
        return try {
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            batteryLevel
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get battery level", e)
            0
        }
    }
    
    /**
     * Check if device is charging
     */
    fun isCharging(): Boolean {
        return try {
            val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check charging status", e)
            false
        }
    }
    
    /**
     * Get battery health information
     */
    fun getBatteryHealth(): BatteryHealth {
        return try {
            val level = getBatteryLevel()
            val charging = isCharging()
            
            val health = when {
                level >= 80 -> BatteryHealth.EXCELLENT
                level >= 60 -> BatteryHealth.GOOD
                level >= 40 -> BatteryHealth.FAIR
                level >= 20 -> BatteryHealth.LOW
                else -> BatteryHealth.CRITICAL
            }
            
            BatteryHealth(
                level = level,
                isCharging = charging,
                health = health,
                isLowBattery = level <= LOW_BATTERY_THRESHOLD,
                isCriticalBattery = level <= CRITICAL_BATTERY_THRESHOLD
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get battery health", e)
            BatteryHealth()
        }
    }
    
    /**
     * Start battery monitoring
     */
    fun startBatteryMonitoring() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val batteryHealth = getBatteryHealth()
                    handleBatteryStatus(batteryHealth)
                    delay(60000) // Check every minute
                } catch (e: Exception) {
                    Log.e(TAG, "Error in battery monitoring", e)
                    delay(30000) // Retry after 30 seconds on error
                }
            }
        }
    }
    
    /**
     * Handle battery status changes
     */
    private fun handleBatteryStatus(batteryHealth: BatteryHealth) {
        when {
            batteryHealth.isCriticalBattery -> {
                handleCriticalBattery()
            }
            batteryHealth.isLowBattery -> {
                handleLowBattery()
            }
            batteryHealth.level >= 80 && batteryHealth.isCharging -> {
                handleBatteryCharged()
            }
        }
        
        // Log battery status for analytics
        logBatteryStatus(batteryHealth)
    }
    
    /**
     * Handle critical battery level
     */
    private fun handleCriticalBattery() {
        Log.w(TAG, "Critical battery level detected")
        
        // Disable non-essential features
        preferencesManager.saveBoolean("emergency_mode", true)
        
        // Reduce lock service frequency
        preferencesManager.saveInt("heartbeat_interval", 300000) // 5 minutes
        
        // Send critical battery notification
        sendBatteryNotification("Critical Battery", "Battery level is critically low. Some features may be disabled.")
    }
    
    /**
     * Handle low battery level
     */
    private fun handleLowBattery() {
        Log.w(TAG, "Low battery level detected")
        
        // Reduce background activity
        preferencesManager.saveInt("heartbeat_interval", 180000) // 3 minutes
        
        // Send low battery notification
        sendBatteryNotification("Low Battery", "Battery level is low. Consider charging your device.")
    }
    
    /**
     * Handle battery charged
     */
    private fun handleBatteryCharged() {
        Log.d(TAG, "Battery charged")
        
        // Restore normal operation
        preferencesManager.saveBoolean("emergency_mode", false)
        preferencesManager.saveInt("heartbeat_interval", 30000) // 30 seconds
        
        // Send battery charged notification
        sendBatteryNotification("Battery Charged", "Battery level is good. All features restored.")
    }
    
    /**
     * Send battery notification
     */
    private fun sendBatteryNotification(title: String, message: String) {
        // This would integrate with your notification system
        Log.d(TAG, "Battery notification: $title - $message")
    }
    
    /**
     * Log battery status for analytics
     */
    private fun logBatteryStatus(batteryHealth: BatteryHealth) {
        // This would integrate with your analytics system
        Log.d(TAG, "Battery status: ${batteryHealth.level}% - ${batteryHealth.health} - Charging: ${batteryHealth.isCharging}")
    }
    
    /**
     * Optimize app for battery usage
     */
    fun optimizeForBattery() {
        try {
            // Reduce background activity
            preferencesManager.saveInt("heartbeat_interval", 300000) // 5 minutes
            preferencesManager.saveBoolean("reduce_animations", true)
            preferencesManager.saveBoolean("disable_analytics", true)
            
            // Disable non-essential features
            preferencesManager.saveBoolean("disable_screenshots", true)
            preferencesManager.saveBoolean("disable_remote_config", true)
            
            Log.d(TAG, "App optimized for battery usage")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to optimize for battery", e)
        }
    }
    
    /**
     * Restore normal operation
     */
    fun restoreNormalOperation() {
        try {
            // Restore normal settings
            preferencesManager.saveInt("heartbeat_interval", 30000) // 30 seconds
            preferencesManager.saveBoolean("reduce_animations", false)
            preferencesManager.saveBoolean("disable_analytics", false)
            preferencesManager.saveBoolean("disable_screenshots", false)
            preferencesManager.saveBoolean("disable_remote_config", false)
            
            Log.d(TAG, "Normal operation restored")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore normal operation", e)
        }
    }
    
    /**
     * Get power management recommendations
     */
    fun getPowerManagementRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        val batteryHealth = getBatteryHealth()
        
        if (!isBatteryOptimizationEnabled()) {
            recommendations.add("Enable battery optimization exemption for better performance")
        }
        
        if (batteryHealth.isLowBattery) {
            recommendations.add("Consider charging your device")
            recommendations.add("Reduce screen brightness")
            recommendations.add("Disable unnecessary background apps")
        }
        
        if (!batteryHealth.isCharging && batteryHealth.level < 50) {
            recommendations.add("Connect to power source for optimal performance")
        }
        
        return recommendations
    }
    
    /**
     * Check if device is in power save mode
     */
    fun isPowerSaveMode(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            powerManager.isPowerSaveMode
        } else {
            false
        }
    }
    
    /**
     * Get power management settings
     */
    fun getPowerManagementSettings(): PowerManagementSettings {
        return PowerManagementSettings(
            batteryOptimizationEnabled = isBatteryOptimizationEnabled(),
            powerSaveMode = isPowerSaveMode(),
            emergencyMode = preferencesManager.getBoolean("emergency_mode", false),
            heartbeatInterval = preferencesManager.getInt("heartbeat_interval", 30000),
            reduceAnimations = preferencesManager.getBoolean("reduce_animations", false),
            disableAnalytics = preferencesManager.getBoolean("disable_analytics", false)
        )
    }
}

/**
 * Data class for battery health information
 */
data class BatteryHealth(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val health: BatteryHealthLevel = BatteryHealthLevel.UNKNOWN,
    val isLowBattery: Boolean = false,
    val isCriticalBattery: Boolean = false
)

/**
 * Enum for battery health levels
 */
enum class BatteryHealthLevel {
    EXCELLENT,
    GOOD,
    FAIR,
    LOW,
    CRITICAL,
    UNKNOWN
}

/**
 * Data class for power management settings
 */
data class PowerManagementSettings(
    val batteryOptimizationEnabled: Boolean,
    val powerSaveMode: Boolean,
    val emergencyMode: Boolean,
    val heartbeatInterval: Int,
    val reduceAnimations: Boolean,
    val disableAnalytics: Boolean
)
