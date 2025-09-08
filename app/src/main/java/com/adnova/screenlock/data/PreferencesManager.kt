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
}
