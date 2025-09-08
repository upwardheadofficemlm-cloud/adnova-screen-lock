package com.adnova.screenlock.manager

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.adnova.screenlock.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages app whitelist for selective access during screen lock
 */
class AppWhitelistManager(private val context: Context) {
    
    private val packageManager = context.packageManager
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val TAG = "AppWhitelistManager"
        private const val WHITELIST_KEY = "app_whitelist"
        private const val WHITELIST_ENABLED_KEY = "whitelist_enabled"
    }
    
    /**
     * Get all installed applications
     */
    fun getAllInstalledApps(): List<AppInfo> {
        return try {
            val apps = mutableListOf<AppInfo>()
            val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            packages.forEach { appInfo ->
                // Filter out system apps and our own app
                if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 || 
                    appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0) {
                    
                    if (appInfo.packageName != context.packageName) {
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val icon = packageManager.getApplicationIcon(appInfo)
                        
                        apps.add(AppInfo(
                            packageName = appInfo.packageName,
                            appName = appName,
                            icon = icon,
                            isSystemApp = appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                        ))
                    }
                }
            }
            
            apps.sortedBy { it.appName }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get installed apps", e)
            emptyList()
        }
    }
    
    /**
     * Get whitelisted apps
     */
    fun getWhitelistedApps(): List<String> {
        return preferencesManager.getStringList(WHITELIST_KEY) ?: emptyList()
    }
    
    /**
     * Add app to whitelist
     */
    fun addToWhitelist(packageName: String) {
        try {
            val currentWhitelist = getWhitelistedApps().toMutableList()
            if (!currentWhitelist.contains(packageName)) {
                currentWhitelist.add(packageName)
                preferencesManager.saveStringList(WHITELIST_KEY, currentWhitelist)
                Log.d(TAG, "Added $packageName to whitelist")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add app to whitelist", e)
        }
    }
    
    /**
     * Remove app from whitelist
     */
    fun removeFromWhitelist(packageName: String) {
        try {
            val currentWhitelist = getWhitelistedApps().toMutableList()
            currentWhitelist.remove(packageName)
            preferencesManager.saveStringList(WHITELIST_KEY, currentWhitelist)
            Log.d(TAG, "Removed $packageName from whitelist")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove app from whitelist", e)
        }
    }
    
    /**
     * Check if app is whitelisted
     */
    fun isAppWhitelisted(packageName: String): Boolean {
        return getWhitelistedApps().contains(packageName)
    }
    
    /**
     * Enable/disable whitelist feature
     */
    fun setWhitelistEnabled(enabled: Boolean) {
        preferencesManager.saveBoolean(WHITELIST_ENABLED_KEY, enabled)
        Log.d(TAG, "Whitelist enabled: $enabled")
    }
    
    /**
     * Check if whitelist is enabled
     */
    fun isWhitelistEnabled(): Boolean {
        return preferencesManager.getBoolean(WHITELIST_ENABLED_KEY, false)
    }
    
    /**
     * Get whitelist configuration
     */
    fun getWhitelistConfig(): WhitelistConfig {
        return WhitelistConfig(
            enabled = isWhitelistEnabled(),
            whitelistedApps = getWhitelistedApps()
        )
    }
    
    /**
     * Save whitelist configuration
     */
    fun saveWhitelistConfig(config: WhitelistConfig) {
        setWhitelistEnabled(config.enabled)
        preferencesManager.saveStringList(WHITELIST_KEY, config.whitelistedApps)
        Log.d(TAG, "Whitelist config saved")
    }
    
    /**
     * Clear all whitelisted apps
     */
    fun clearWhitelist() {
        preferencesManager.saveStringList(WHITELIST_KEY, emptyList())
        Log.d(TAG, "Whitelist cleared")
    }
    
    /**
     * Get apps by category
     */
    fun getAppsByCategory(): Map<String, List<AppInfo>> {
        val allApps = getAllInstalledApps()
        val categories = mutableMapOf<String, MutableList<AppInfo>>()
        
        allApps.forEach { app ->
            val category = getAppCategory(app.packageName)
            categories.getOrPut(category) { mutableListOf() }.add(app)
        }
        
        return categories
    }
    
    /**
     * Determine app category based on package name
     */
    private fun getAppCategory(packageName: String): String {
        return when {
            packageName.contains("browser") || packageName.contains("chrome") || 
            packageName.contains("firefox") || packageName.contains("safari") -> "Browsers"
            
            packageName.contains("music") || packageName.contains("player") || 
            packageName.contains("spotify") || packageName.contains("youtube") -> "Media"
            
            packageName.contains("camera") || packageName.contains("photo") || 
            packageName.contains("gallery") -> "Camera & Photos"
            
            packageName.contains("game") || packageName.contains("play") -> "Games"
            
            packageName.contains("office") || packageName.contains("word") || 
            packageName.contains("excel") || packageName.contains("pdf") -> "Productivity"
            
            packageName.contains("social") || packageName.contains("facebook") || 
            packageName.contains("twitter") || packageName.contains("instagram") -> "Social"
            
            packageName.contains("bank") || packageName.contains("payment") || 
            packageName.contains("wallet") -> "Finance"
            
            packageName.contains("weather") || packageName.contains("news") -> "News & Weather"
            
            packageName.contains("map") || packageName.contains("navigation") || 
            packageName.contains("gps") -> "Navigation"
            
            else -> "Other"
        }
    }
    
    /**
     * Get recommended apps for whitelist
     */
    fun getRecommendedApps(): List<AppInfo> {
        val allApps = getAllInstalledApps()
        val recommendedPackages = listOf(
            "com.android.chrome",
            "com.android.camera2",
            "com.android.gallery3d",
            "com.android.music",
            "com.android.calculator2",
            "com.android.calendar",
            "com.android.contacts",
            "com.android.mms"
        )
        
        return allApps.filter { app ->
            recommendedPackages.contains(app.packageName)
        }
    }
    
    /**
     * Search apps by name
     */
    fun searchApps(query: String): List<AppInfo> {
        val allApps = getAllInstalledApps()
        return allApps.filter { app ->
            app.appName.contains(query, ignoreCase = true) ||
            app.packageName.contains(query, ignoreCase = true)
        }
    }
    
    /**
     * Export whitelist configuration
     */
    fun exportWhitelistConfig(): String {
        val config = getWhitelistConfig()
        return """
            {
                "enabled": ${config.enabled},
                "whitelistedApps": ${config.whitelistedApps.joinToString("\"", "\"", "\"") { it }},
                "exportDate": "${System.currentTimeMillis()}"
            }
        """.trimIndent()
    }
    
    /**
     * Import whitelist configuration
     */
    fun importWhitelistConfig(configJson: String): Boolean {
        return try {
            // Simple JSON parsing (in production, use proper JSON library)
            val enabled = configJson.contains("\"enabled\": true")
            val appsStart = configJson.indexOf("\"whitelistedApps\": [") + 20
            val appsEnd = configJson.indexOf("]", appsStart)
            val appsString = configJson.substring(appsStart, appsEnd)
            
            val apps = appsString.split(",").map { it.trim().replace("\"", "") }
                .filter { it.isNotEmpty() }
            
            saveWhitelistConfig(WhitelistConfig(enabled, apps))
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import whitelist config", e)
            false
        }
    }
}

/**
 * Data class for app information
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: android.graphics.drawable.Drawable,
    val isSystemApp: Boolean = false
)

/**
 * Data class for whitelist configuration
 */
data class WhitelistConfig(
    val enabled: Boolean,
    val whitelistedApps: List<String>
)
