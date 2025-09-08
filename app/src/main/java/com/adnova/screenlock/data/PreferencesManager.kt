package com.adnova.screenlock.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.adnova.screenlock.utils.JsonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PreferencesManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val PREFS_NAME = "adnova_screen_lock_prefs"
        private const val KEY_LOCK_CONFIG = "lock_configuration"
        private const val KEY_LOCK_STATUS = "lock_status"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_FIREBASE_TOKEN = "firebase_token"
        private const val KEY_LAST_SYNC = "last_sync_time"
        private const val KEY_OVERLAY_PERMISSION_GRANTED = "overlay_permission_granted"
        private const val KEY_SCHEDULE_CONFIG = "schedule_configuration"
    }
    
    suspend fun saveLockConfiguration(config: LockConfiguration) = withContext(Dispatchers.IO) {
        val json = JsonUtils.toJson(config)
        sharedPreferences.edit().putString(KEY_LOCK_CONFIG, json).apply()
    }
    
    suspend fun getLockConfiguration(): LockConfiguration = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(KEY_LOCK_CONFIG, null)
        if (json != null) {
            JsonUtils.fromJson(json, LockConfiguration::class.java) ?: LockConfiguration()
        } else {
            LockConfiguration()
        }
    }
    
    suspend fun saveLockStatus(status: LockStatus) = withContext(Dispatchers.IO) {
        val json = JsonUtils.toJson(status)
        sharedPreferences.edit().putString(KEY_LOCK_STATUS, json).apply()
    }
    
    suspend fun getLockStatus(): LockStatus = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(KEY_LOCK_STATUS, null)
        if (json != null) {
            JsonUtils.fromJson(json, LockStatus::class.java) ?: LockStatus()
        } else {
            LockStatus()
        }
    }
    
    fun setDeviceId(deviceId: String) {
        sharedPreferences.edit().putString(KEY_DEVICE_ID, deviceId).apply()
    }
    
    fun getDeviceId(): String? {
        return sharedPreferences.getString(KEY_DEVICE_ID, null)
    }
    
    fun setFirebaseToken(token: String) {
        sharedPreferences.edit().putString(KEY_FIREBASE_TOKEN, token).apply()
    }
    
    fun getFirebaseToken(): String? {
        return sharedPreferences.getString(KEY_FIREBASE_TOKEN, null)
    }
    
    fun setLastSyncTime(time: Long) {
        sharedPreferences.edit().putLong(KEY_LAST_SYNC, time).apply()
    }
    
    fun getLastSyncTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_SYNC, 0L)
    }
    
    fun setOverlayPermissionGranted(granted: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_OVERLAY_PERMISSION_GRANTED, granted).apply()
    }
    
    fun isOverlayPermissionGranted(): Boolean {
        return sharedPreferences.getBoolean(KEY_OVERLAY_PERMISSION_GRANTED, false)
    }
    
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
    
    // Enhanced methods for new features
    
    suspend fun saveScheduleConfig(config: com.adnova.screenlock.manager.ScheduleConfig) = withContext(Dispatchers.IO) {
        val json = JsonUtils.toJson(config)
        sharedPreferences.edit().putString(KEY_SCHEDULE_CONFIG, json).apply()
    }
    
    suspend fun getScheduleConfig(): com.adnova.screenlock.manager.ScheduleConfig = withContext(Dispatchers.IO) {
        val json = sharedPreferences.getString(KEY_SCHEDULE_CONFIG, null)
        if (json != null) {
            JsonUtils.fromJson(json, com.adnova.screenlock.manager.ScheduleConfig::class.java) ?: com.adnova.screenlock.manager.ScheduleConfig()
        } else {
            com.adnova.screenlock.manager.ScheduleConfig()
        }
    }
    
    fun saveString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }
    
    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }
    
    fun saveStringList(key: String, value: List<String>) {
        val json = JsonUtils.toJson(value)
        sharedPreferences.edit().putString(key, json).apply()
    }
    
    fun getStringList(key: String): List<String>? {
        val json = sharedPreferences.getString(key, null)
        return if (json != null) {
            JsonUtils.fromJson(json, Array<String>::class.java)?.toList()
        } else {
            null
        }
    }
    
    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
    
    fun saveInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }
    
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }
    
    fun saveLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }
    
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }
    
    fun saveFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }
    
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }
}
