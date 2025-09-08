package com.adnova.screenlock.firebase

import android.content.Context
import android.util.Log
import com.adnova.screenlock.data.LockConfiguration
import com.adnova.screenlock.data.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import android.os.Build
import android.provider.Settings

class FirebaseManager(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val remoteConfig = FirebaseRemoteConfig.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val preferencesManager = PreferencesManager(context)
    private val messaging = FirebaseMessaging.getInstance()
    
    // Real-time listeners
    private var deviceListener: ListenerRegistration? = null
    private var configListener: ListenerRegistration? = null
    
    companion object {
        private const val TAG = "FirebaseManager"
        private const val COLLECTION_DEVICES = "devices"
        private const val COLLECTION_CONFIGS = "configurations"
        private const val COLLECTION_ANALYTICS = "analytics"
        
        // Firebase project configuration
        const val PROJECT_ID = "adnova-screen-lock-90521"
        const val STORAGE_BUCKET = "gs://adnova-screen-lock-90521.firebasestorage.app"
        const val MESSAGING_SENDER_ID = "914767381345"
        const val API_KEY = "AIzaSyAz8P_dX5uTI3zF0lW2CwPh4yqFvwfUn2o"
    }
    
    init {
        setupRemoteConfig()
    }
    
    private fun setupRemoteConfig() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // 1 hour
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        
        // Set default values
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }
    
    suspend fun signInAdmin(email: String, password: String): Boolean {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user != null
        } catch (e: Exception) {
            Log.e(TAG, "Admin sign in failed", e)
            false
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    fun isAdminSignedIn(): Boolean {
        return auth.currentUser != null
    }
    
    suspend fun fetchRemoteConfig(): Boolean {
        return try {
            val fetchResult = remoteConfig.fetchAndActivate().await()
            if (fetchResult) {
                Log.d(TAG, "Remote config fetched and activated")
                processRemoteConfig()
            }
            fetchResult
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch remote config", e)
            false
        }
    }
    
    private suspend fun processRemoteConfig() {
        try {
            val configJson = remoteConfig.getString("lock_configuration")
            if (configJson.isNotEmpty()) {
                // TODO: Parse and apply remote configuration
                Log.d(TAG, "Remote config: $configJson")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing remote config", e)
        }
    }
    
    suspend fun uploadDeviceConfiguration(config: LockConfiguration): Boolean {
        return try {
            val deviceId = preferencesManager.getDeviceId() ?: return false
            
            val deviceData = hashMapOf(
                "deviceId" to deviceId,
                "configuration" to config,
                "lastUpdated" to System.currentTimeMillis(),
                "appVersion" to "1.0"
            )
            
            firestore.collection(COLLECTION_DEVICES)
                .document(deviceId)
                .set(deviceData)
                .await()
            
            preferencesManager.setLastSyncTime(System.currentTimeMillis())
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload device configuration", e)
            false
        }
    }
    
    suspend fun downloadDeviceConfiguration(): LockConfiguration? {
        return try {
            val deviceId = preferencesManager.getDeviceId() ?: return null
            
            val document = firestore.collection(COLLECTION_DEVICES)
                .document(deviceId)
                .get()
                .await()
            
            if (document.exists()) {
                val config = document.get("configuration") as? LockConfiguration
                config?.let {
                    preferencesManager.saveLockConfiguration(it)
                    preferencesManager.setLastSyncTime(System.currentTimeMillis())
                }
                config
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download device configuration", e)
            null
        }
    }
    
    suspend fun logAnalyticsEvent(eventType: String, data: Map<String, Any> = emptyMap()) {
        try {
            val deviceId = preferencesManager.getDeviceId() ?: return
            
            val analyticsData = hashMapOf(
                "deviceId" to deviceId,
                "eventType" to eventType,
                "timestamp" to System.currentTimeMillis(),
                "data" to data
            )
            
            firestore.collection(COLLECTION_ANALYTICS)
                .add(analyticsData)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log analytics event", e)
        }
    }
    
    fun getCurrentUser() = auth.currentUser
    
    // Storage methods
    suspend fun uploadDeviceLogs(logData: String): Boolean {
        return try {
            val deviceId = preferencesManager.getDeviceId() ?: return false
            val fileName = "logs/${deviceId}_${System.currentTimeMillis()}.txt"
            val storageRef = storage.reference.child(fileName)
            
            val uploadTask = storageRef.putBytes(logData.toByteArray()).await()
            Log.d(TAG, "Device logs uploaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload device logs", e)
            false
        }
    }
    
    suspend fun uploadDeviceScreenshot(imageData: ByteArray): Boolean {
        return try {
            val deviceId = preferencesManager.getDeviceId() ?: return false
            val fileName = "screenshots/${deviceId}_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child(fileName)
            
            val uploadTask = storageRef.putBytes(imageData).await()
            Log.d(TAG, "Device screenshot uploaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload device screenshot", e)
            false
        }
    }
    
    suspend fun downloadRemoteConfiguration(): String? {
        return try {
            val configRef = storage.reference.child("configurations/remote_config.json")
            val bytes = configRef.getBytes(Long.MAX_VALUE).await()
            String(bytes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download remote configuration", e)
            null
        }
    }
    
    suspend fun uploadDeviceConfiguration(config: LockConfiguration): Boolean {
        return try {
            val deviceId = preferencesManager.getDeviceId() ?: return false
            val fileName = "configurations/${deviceId}_config.json"
            val storageRef = storage.reference.child(fileName)
            
            val configJson = com.adnova.screenlock.utils.JsonUtils.toJson(config)
            val uploadTask = storageRef.putBytes(configJson.toByteArray()).await()
            Log.d(TAG, "Device configuration uploaded successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload device configuration", e)
            false
        }
    }
    
    // Enhanced Device Connection and Real-time Features
    
    /**
     * Register device with enhanced information and real-time connection
     */
    suspend fun registerDeviceWithRealTime(): Boolean {
        return try {
            val deviceId = getOrCreateDeviceId()
            val deviceInfo = getDeviceInfo()
            val fcmToken = getFCMToken()
            
            val deviceData = mapOf(
                "deviceId" to deviceId,
                "deviceName" to deviceInfo["deviceName"],
                "model" to deviceInfo["model"],
                "androidVersion" to deviceInfo["androidVersion"],
                "appVersion" to deviceInfo["appVersion"],
                "fcmToken" to fcmToken,
                "status" to "online",
                "lastSeen" to Date(),
                "registeredAt" to Date(),
                "batteryLevel" to getBatteryLevel(),
                "isCharging" to isDeviceCharging(),
                "screenResolution" to getScreenResolution(),
                "ramTotal" to getTotalRAM(),
                "storageTotal" to getTotalStorage(),
                "networkType" to getNetworkType()
            )
            
            firestore.collection(COLLECTION_DEVICES)
                .document(deviceId)
                .set(deviceData)
                .await()
            
            // Start real-time listeners
            startRealTimeListeners()
            
            Log.d(TAG, "Device registered with real-time connection")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register device", e)
            false
        }
    }
    
    /**
     * Start real-time listeners for device updates
     */
    fun startRealTimeListeners() {
        val deviceId = preferencesManager.getDeviceId() ?: return
        
        // Listen for configuration changes
        configListener = firestore.collection(COLLECTION_DEVICES)
            .document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to device updates", error)
                    return@addSnapshotListener
                }
                
                snapshot?.let { doc ->
                    if (doc.exists()) {
                        val data = doc.data
                        data?.let { handleDeviceUpdate(it) }
                    }
                }
            }
        
        // Listen for remote commands
        firestore.collection("commands")
            .whereEqualTo("deviceId", deviceId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to commands", error)
                    return@addSnapshotListener
                }
                
                snapshot?.documents?.forEach { doc ->
                    handleRemoteCommand(doc.id, doc.data)
                }
            }
    }
    
    /**
     * Stop real-time listeners
     */
    fun stopRealTimeListeners() {
        deviceListener?.remove()
        configListener?.remove()
        deviceListener = null
        configListener = null
    }
    
    /**
     * Update device status (online/offline)
     */
    suspend fun updateDeviceStatus(status: String) {
        try {
            val deviceId = preferencesManager.getDeviceId() ?: return
            val updateData = mapOf(
                "status" to status,
                "lastSeen" to Date(),
                "batteryLevel" to getBatteryLevel(),
                "isCharging" to isDeviceCharging()
            )
            
            firestore.collection(COLLECTION_DEVICES)
                .document(deviceId)
                .update(updateData)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update device status", e)
        }
    }
    
    /**
     * Send device heartbeat
     */
    suspend fun sendHeartbeat() {
        try {
            val deviceId = preferencesManager.getDeviceId() ?: return
            val heartbeatData = mapOf(
                "lastHeartbeat" to Date(),
                "batteryLevel" to getBatteryLevel(),
                "isCharging" to isDeviceCharging(),
                "status" to "online"
            )
            
            firestore.collection(COLLECTION_DEVICES)
                .document(deviceId)
                .update(heartbeatData)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send heartbeat", e)
        }
    }
    
    /**
     * Get FCM token for push notifications
     */
    private suspend fun getFCMToken(): String? {
        return try {
            messaging.token.await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }
    
    /**
     * Get comprehensive device information
     */
    private fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "deviceName" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "model" to Build.MODEL,
            "androidVersion" to Build.VERSION.RELEASE,
            "appVersion" to "1.0.0", // You can get this from BuildConfig
            "sdkVersion" to Build.VERSION.SDK_INT.toString(),
            "manufacturer" to Build.MANUFACTURER,
            "brand" to Build.BRAND,
            "deviceId" to Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        )
    }
    
    /**
     * Get or create unique device ID
     */
    private fun getOrCreateDeviceId(): String {
        var deviceId = preferencesManager.getDeviceId()
        if (deviceId == null) {
            deviceId = "device_${System.currentTimeMillis()}_${Build.MODEL.replace(" ", "_")}"
            preferencesManager.setDeviceId(deviceId)
        }
        return deviceId
    }
    
    /**
     * Handle device updates from Firebase
     */
    private fun handleDeviceUpdate(data: Map<String, Any>) {
        try {
            // Handle configuration updates
            data["configuration"]?.let { configData ->
                if (configData is Map<*, *>) {
                    val config = com.adnova.screenlock.utils.JsonUtils.fromJson(
                        configData.toString(), 
                        LockConfiguration::class.java
                    )
                    config?.let { 
                        preferencesManager.saveLockConfiguration(it)
                        // Notify UI about configuration change
                        notifyConfigurationChanged(it)
                    }
                }
            }
            
            // Handle remote commands
            data["remoteCommand"]?.let { command ->
                if (command is Map<*, *>) {
                    handleRemoteCommand("", command)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling device update", e)
        }
    }
    
    /**
     * Handle remote commands from admin
     */
    private fun handleRemoteCommand(commandId: String, commandData: Map<String, Any>) {
        try {
            val command = commandData["command"] as? String
            val parameters = commandData["parameters"] as? Map<String, Any>
            
            when (command) {
                "lock_screen" -> {
                    // Trigger screen lock
                    notifyRemoteLockCommand(parameters)
                }
                "unlock_screen" -> {
                    // Trigger screen unlock
                    notifyRemoteUnlockCommand(parameters)
                }
                "update_config" -> {
                    // Update configuration
                    parameters?.let { notifyConfigUpdate(it) }
                }
                "restart_app" -> {
                    // Restart application
                    notifyRestartCommand()
                }
                "take_screenshot" -> {
                    // Take and upload screenshot
                    notifyScreenshotCommand()
                }
            }
            
            // Mark command as processed
            if (commandId.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    markCommandAsProcessed(commandId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling remote command", e)
        }
    }
    
    /**
     * Mark command as processed
     */
    private suspend fun markCommandAsProcessed(commandId: String) {
        try {
            firestore.collection("commands")
                .document(commandId)
                .update("status", "processed", "processedAt", Date())
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark command as processed", e)
        }
    }
    
    // Device information helper methods
    private fun getBatteryLevel(): Int {
        // Implementation would require BatteryManager
        return 85 // Placeholder
    }
    
    private fun isDeviceCharging(): Boolean {
        // Implementation would require BatteryManager
        return false // Placeholder
    }
    
    private fun getScreenResolution(): String {
        val displayMetrics = context.resources.displayMetrics
        return "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
    }
    
    private fun getTotalRAM(): Long {
        // Implementation would require ActivityManager
        return 2048 * 1024 * 1024 // 2GB placeholder
    }
    
    private fun getTotalStorage(): Long {
        // Implementation would require StatFs
        return 32 * 1024 * 1024 * 1024 // 32GB placeholder
    }
    
    private fun getNetworkType(): String {
        // Implementation would require ConnectivityManager
        return "WiFi" // Placeholder
    }
    
    // Notification callbacks (to be implemented by UI)
    private fun notifyConfigurationChanged(config: LockConfiguration) {
        // This would be implemented with a callback interface
        Log.d(TAG, "Configuration changed: ${config.lockType}")
    }
    
    private fun notifyRemoteLockCommand(parameters: Map<String, Any>?) {
        Log.d(TAG, "Remote lock command received")
    }
    
    private fun notifyRemoteUnlockCommand(parameters: Map<String, Any>?) {
        Log.d(TAG, "Remote unlock command received")
    }
    
    private fun notifyConfigUpdate(parameters: Map<String, Any>) {
        Log.d(TAG, "Config update received: $parameters")
    }
    
    private fun notifyRestartCommand() {
        Log.d(TAG, "Restart command received")
    }
    
    private fun notifyScreenshotCommand() {
        Log.d(TAG, "Screenshot command received")
    }
}
