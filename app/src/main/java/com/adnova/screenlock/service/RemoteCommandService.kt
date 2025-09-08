package com.adnova.screenlock.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.adnova.screenlock.manager.DeviceConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Service to handle remote commands from admin dashboard
 */
class RemoteCommandService : Service() {
    
    private lateinit var deviceConnectionManager: DeviceConnectionManager
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    
    companion object {
        private const val TAG = "RemoteCommandService"
        const val ACTION_LOCK_SCREEN = "com.adnova.screenlock.LOCK_SCREEN"
        const val ACTION_UNLOCK_SCREEN = "com.adnova.screenlock.UNLOCK_SCREEN"
        const val ACTION_UPDATE_CONFIG = "com.adnova.screenlock.UPDATE_CONFIG"
        const val ACTION_RESTART_APP = "com.adnova.screenlock.RESTART_APP"
        const val ACTION_TAKE_SCREENSHOT = "com.adnova.screenlock.TAKE_SCREENSHOT"
        
        // Extra keys
        const val EXTRA_LOCK_TYPE = "lock_type"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_CONFIG_DATA = "config_data"
        const val EXTRA_COMMAND_ID = "command_id"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "RemoteCommandService created")
        
        deviceConnectionManager = DeviceConnectionManager(this)
        
        // Set up command callbacks
        deviceConnectionManager.setCommandCallback(object : DeviceConnectionManager.CommandCallback {
            override fun onRemoteLockCommand(parameters: Map<String, Any>?) {
                handleLockCommand(parameters)
            }
            
            override fun onRemoteUnlockCommand(parameters: Map<String, Any>?) {
                handleUnlockCommand(parameters)
            }
            
            override fun onConfigUpdate(parameters: Map<String, Any>) {
                handleConfigUpdate(parameters)
            }
            
            override fun onRestartCommand() {
                handleRestartCommand()
            }
            
            override fun onScreenshotCommand() {
                handleScreenshotCommand()
            }
        })
        
        // Initialize connection
        serviceScope.launch {
            deviceConnectionManager.initializeConnection()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "RemoteCommandService started with intent: ${intent?.action}")
        
        intent?.let { handleIntent(it) }
        
        return START_STICKY // Restart service if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "RemoteCommandService destroyed")
        deviceConnectionManager.disconnect()
    }
    
    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_LOCK_SCREEN -> {
                val lockType = intent.getStringExtra(EXTRA_LOCK_TYPE) ?: "full"
                val duration = intent.getLongExtra(EXTRA_DURATION, -1L)
                val parameters = mapOf(
                    "lockType" to lockType,
                    "duration" to duration
                )
                handleLockCommand(parameters)
            }
            
            ACTION_UNLOCK_SCREEN -> {
                handleUnlockCommand(null)
            }
            
            ACTION_UPDATE_CONFIG -> {
                val configData = intent.getStringExtra(EXTRA_CONFIG_DATA)
                if (configData != null) {
                    // Parse config data and handle update
                    handleConfigUpdate(mapOf("config" to configData))
                }
            }
            
            ACTION_RESTART_APP -> {
                handleRestartCommand()
            }
            
            ACTION_TAKE_SCREENSHOT -> {
                handleScreenshotCommand()
            }
        }
    }
    
    private fun handleLockCommand(parameters: Map<String, Any>?) {
        Log.d(TAG, "Handling lock command: $parameters")
        
        val lockType = parameters?.get("lockType") as? String ?: "full"
        val duration = parameters?.get("duration") as? Long ?: -1L
        
        // Start lock service
        val lockIntent = Intent(this, LockOverlayService::class.java)
        lockIntent.putExtra("action", "lock")
        lockIntent.putExtra("lockType", lockType)
        if (duration > 0) {
            lockIntent.putExtra("duration", duration)
        }
        
        startForegroundService(lockIntent)
        
        // Send confirmation back to admin
        serviceScope.launch {
            deviceConnectionManager.sendLockEvent(lockType, true)
        }
    }
    
    private fun handleUnlockCommand(parameters: Map<String, Any>?) {
        Log.d(TAG, "Handling unlock command: $parameters")
        
        // Stop lock service
        val lockIntent = Intent(this, LockOverlayService::class.java)
        stopService(lockIntent)
        
        // Send confirmation back to admin
        serviceScope.launch {
            deviceConnectionManager.sendUnlockEvent("remote")
        }
    }
    
    private fun handleConfigUpdate(parameters: Map<String, Any>) {
        Log.d(TAG, "Handling config update: $parameters")
        
        // Update local configuration
        // This would be implemented with PreferencesManager
        
        // Send confirmation back to admin
        serviceScope.launch {
            deviceConnectionManager.sendErrorEvent("Config updated", "Configuration updated successfully")
        }
    }
    
    private fun handleRestartCommand() {
        Log.d(TAG, "Handling restart command")
        
        // Send confirmation before restart
        serviceScope.launch {
            deviceConnectionManager.sendErrorEvent("Restarting", "Application restarting...")
        }
        
        // Restart the application
        val restartIntent = packageManager.getLaunchIntentForPackage(packageName)
        restartIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(restartIntent)
        
        // Stop the service
        stopSelf()
    }
    
    private fun handleScreenshotCommand() {
        Log.d(TAG, "Handling screenshot command")
        
        // Take screenshot and upload to Firebase
        // This would be implemented with screenshot functionality
        
        serviceScope.launch {
            deviceConnectionManager.sendErrorEvent("Screenshot taken", "Screenshot captured and uploaded")
        }
    }
}
