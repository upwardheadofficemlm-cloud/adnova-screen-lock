package com.adnova.screenlock.firebase

import android.content.Context
import android.util.Log
import com.adnova.screenlock.data.LockConfiguration
import com.adnova.screenlock.data.PreferencesManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseManager(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val remoteConfig = FirebaseRemoteConfig.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val preferencesManager = PreferencesManager(context)
    
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
}
