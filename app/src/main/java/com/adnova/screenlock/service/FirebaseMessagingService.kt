package com.adnova.screenlock.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.adnova.screenlock.R
import com.adnova.screenlock.data.LockConfiguration
import com.adnova.screenlock.data.PreferencesManager
import com.adnova.screenlock.utils.JsonUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FirebaseMessagingService"
        private const val CHANNEL_ID = "remote_config_channel"
        private const val NOTIFICATION_ID = 2001
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "From: ${remoteMessage.from}")
        
        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }
        
        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title ?: "AdNova Screen Lock", it.body ?: "")
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // Save token to preferences
        val preferencesManager = PreferencesManager(this)
        preferencesManager.setFirebaseToken(token)
        
        // TODO: Send token to server
        sendTokenToServer(token)
    }
    
    private fun handleDataMessage(data: Map<String, String>) {
        when (data["type"]) {
            "lock_config" -> {
                handleLockConfigUpdate(data["config"])
            }
            "lock_command" -> {
                handleLockCommand(data["command"])
            }
            "remote_config" -> {
                handleRemoteConfigUpdate(data["config"])
            }
        }
    }
    
    private fun handleLockConfigUpdate(configJson: String?) {
        configJson?.let { json ->
            try {
                val config = JsonUtils.fromJson<LockConfiguration>(json)
                config?.let {
                    val preferencesManager = PreferencesManager(this)
                    val serviceScope = CoroutineScope(Dispatchers.IO)
                    
                    serviceScope.launch {
                        preferencesManager.saveLockConfiguration(it)
                        preferencesManager.setLastSyncTime(System.currentTimeMillis())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing lock config", e)
            }
        }
    }
    
    private fun handleLockCommand(command: String?) {
        when (command) {
            "start_lock" -> {
                // TODO: Start lock service
            }
            "stop_lock" -> {
                // TODO: Stop lock service
            }
            "restart_lock" -> {
                // TODO: Restart lock service
            }
        }
    }
    
    private fun handleRemoteConfigUpdate(configJson: String?) {
        configJson?.let { json ->
            // TODO: Handle remote config updates
            Log.d(TAG, "Remote config update: $json")
        }
    }
    
    private fun sendTokenToServer(token: String) {
        // TODO: Implement token sending to server
        Log.d(TAG, "Sending token to server: $token")
    }
    
    private fun showNotification(title: String, body: String) {
        createNotificationChannel()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_lock)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Remote Configuration",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for remote configuration updates"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
