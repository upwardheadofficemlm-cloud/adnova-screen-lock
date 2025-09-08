package com.adnova.screenlock.manager

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.adnova.screenlock.firebase.FirebaseManager
import com.adnova.screenlock.service.LockOverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Manages device connection, heartbeat, and real-time communication with Firebase
 */
class DeviceConnectionManager(private val context: Context) {
    
    private val firebaseManager = FirebaseManager(context)
    private val handler = Handler(Looper.getMainLooper())
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    // Heartbeat and connection management
    private var heartbeatJob: Job? = null
    private var isConnected = false
    private var connectionRetryCount = 0
    private val maxRetryCount = 5
    
    // Callbacks
    private var connectionCallback: ConnectionCallback? = null
    private var commandCallback: CommandCallback? = null
    
    companion object {
        private const val TAG = "DeviceConnectionManager"
        private const val HEARTBEAT_INTERVAL = 30_000L // 30 seconds
        private const val CONNECTION_TIMEOUT = 60_000L // 1 minute
    }
    
    interface ConnectionCallback {
        fun onConnected()
        fun onDisconnected()
        fun onConnectionError(error: String)
    }
    
    interface CommandCallback {
        fun onRemoteLockCommand(parameters: Map<String, Any>?)
        fun onRemoteUnlockCommand(parameters: Map<String, Any>?)
        fun onConfigUpdate(parameters: Map<String, Any>)
        fun onRestartCommand()
        fun onScreenshotCommand()
    }
    
    /**
     * Initialize device connection
     */
    suspend fun initializeConnection(): Boolean {
        return try {
            Log.d(TAG, "Initializing device connection...")
            
            // Register device with Firebase
            val registered = firebaseManager.registerDeviceWithRealTime()
            if (registered) {
                isConnected = true
                connectionRetryCount = 0
                startHeartbeat()
                connectionCallback?.onConnected()
                Log.d(TAG, "Device connection initialized successfully")
                true
            } else {
                handleConnectionError("Failed to register device")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing connection", e)
            handleConnectionError(e.message ?: "Unknown error")
            false
        }
    }
    
    /**
     * Start heartbeat to maintain connection
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = coroutineScope.launch {
            while (isConnected) {
                try {
                    firebaseManager.sendHeartbeat()
                    Log.d(TAG, "Heartbeat sent")
                    delay(HEARTBEAT_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Heartbeat failed", e)
                    handleConnectionError("Heartbeat failed: ${e.message}")
                    break
                }
            }
        }
    }
    
    /**
     * Stop heartbeat and disconnect
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting device...")
        isConnected = false
        heartbeatJob?.cancel()
        heartbeatJob = null
        
        coroutineScope.launch {
            try {
                firebaseManager.updateDeviceStatus("offline")
                firebaseManager.stopRealTimeListeners()
            } catch (e: Exception) {
                Log.e(TAG, "Error during disconnect", e)
            }
        }
        
        connectionCallback?.onDisconnected()
    }
    
    /**
     * Handle connection errors and retry logic
     */
    private fun handleConnectionError(error: String) {
        Log.e(TAG, "Connection error: $error")
        isConnected = false
        connectionRetryCount++
        
        if (connectionRetryCount <= maxRetryCount) {
            Log.d(TAG, "Retrying connection (attempt $connectionRetryCount/$maxRetryCount)")
            handler.postDelayed({
                coroutineScope.launch {
                    initializeConnection()
                }
            }, TimeUnit.SECONDS.toMillis(connectionRetryCount * 2))
        } else {
            Log.e(TAG, "Max retry attempts reached. Connection failed.")
            connectionCallback?.onConnectionError("Connection failed after $maxRetryCount attempts")
        }
    }
    
    /**
     * Send device status update
     */
    suspend fun updateStatus(status: String) {
        if (isConnected) {
            try {
                firebaseManager.updateDeviceStatus(status)
                Log.d(TAG, "Status updated to: $status")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update status", e)
            }
        }
    }
    
    /**
     * Send lock event to Firebase
     */
    suspend fun sendLockEvent(lockType: String, isLocked: Boolean) {
        if (isConnected) {
            try {
                val eventData = mapOf(
                    "event" to "lock_status_changed",
                    "lockType" to lockType,
                    "isLocked" to isLocked,
                    "timestamp" to System.currentTimeMillis()
                )
                
                // This would be implemented in FirebaseManager
                // firebaseManager.logEvent("lock_event", eventData)
                Log.d(TAG, "Lock event sent: $eventData")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send lock event", e)
            }
        }
    }
    
    /**
     * Send unlock event to Firebase
     */
    suspend fun sendUnlockEvent(unlockMethod: String) {
        if (isConnected) {
            try {
                val eventData = mapOf(
                    "event" to "unlock_event",
                    "unlockMethod" to unlockMethod,
                    "timestamp" to System.currentTimeMillis()
                )
                
                // This would be implemented in FirebaseManager
                // firebaseManager.logEvent("unlock_event", eventData)
                Log.d(TAG, "Unlock event sent: $eventData")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send unlock event", e)
            }
        }
    }
    
    /**
     * Send error event to Firebase
     */
    suspend fun sendErrorEvent(error: String, details: String? = null) {
        if (isConnected) {
            try {
                val eventData = mapOf(
                    "event" to "error",
                    "error" to error,
                    "details" to (details ?: ""),
                    "timestamp" to System.currentTimeMillis()
                )
                
                // This would be implemented in FirebaseManager
                // firebaseManager.logEvent("error_event", eventData)
                Log.d(TAG, "Error event sent: $eventData")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send error event", e)
            }
        }
    }
    
    /**
     * Handle remote lock command
     */
    fun handleRemoteLockCommand(parameters: Map<String, Any>?) {
        Log.d(TAG, "Handling remote lock command: $parameters")
        commandCallback?.onRemoteLockCommand(parameters)
        
        // Start lock service
        val intent = Intent(context, LockOverlayService::class.java)
        intent.putExtra("action", "lock")
        parameters?.let { params ->
            params["lockType"]?.let { intent.putExtra("lockType", it.toString()) }
            params["duration"]?.let { intent.putExtra("duration", it.toString().toLongOrNull()) }
        }
        context.startForegroundService(intent)
    }
    
    /**
     * Handle remote unlock command
     */
    fun handleRemoteUnlockCommand(parameters: Map<String, Any>?) {
        Log.d(TAG, "Handling remote unlock command: $parameters")
        commandCallback?.onRemoteUnlockCommand(parameters)
        
        // Stop lock service
        val intent = Intent(context, LockOverlayService::class.java)
        intent.putExtra("action", "unlock")
        context.stopService(intent)
    }
    
    /**
     * Handle configuration update command
     */
    fun handleConfigUpdate(parameters: Map<String, Any>) {
        Log.d(TAG, "Handling config update: $parameters")
        commandCallback?.onConfigUpdate(parameters)
        
        // Update local configuration
        // This would be implemented with PreferencesManager
    }
    
    /**
     * Handle restart command
     */
    fun handleRestartCommand() {
        Log.d(TAG, "Handling restart command")
        commandCallback?.onRestartCommand()
        
        // Restart the application
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }
    
    /**
     * Handle screenshot command
     */
    fun handleScreenshotCommand() {
        Log.d(TAG, "Handling screenshot command")
        commandCallback?.onScreenshotCommand()
        
        // Take screenshot and upload to Firebase
        // This would be implemented with screenshot functionality
    }
    
    /**
     * Set connection callback
     */
    fun setConnectionCallback(callback: ConnectionCallback) {
        this.connectionCallback = callback
    }
    
    /**
     * Set command callback
     */
    fun setCommandCallback(callback: CommandCallback) {
        this.commandCallback = callback
    }
    
    /**
     * Check if device is connected
     */
    fun isConnected(): Boolean = isConnected
    
    /**
     * Get connection status
     */
    fun getConnectionStatus(): String {
        return when {
            isConnected -> "Connected"
            connectionRetryCount > 0 -> "Reconnecting..."
            else -> "Disconnected"
        }
    }
    
    /**
     * Force reconnection
     */
    fun forceReconnect() {
        Log.d(TAG, "Forcing reconnection...")
        disconnect()
        connectionRetryCount = 0
        
        coroutineScope.launch {
            delay(1000) // Wait 1 second before reconnecting
            initializeConnection()
        }
    }
}
