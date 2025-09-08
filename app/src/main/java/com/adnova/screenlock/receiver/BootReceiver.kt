package com.adnova.screenlock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.adnova.screenlock.data.PreferencesManager
import com.adnova.screenlock.service.LockOverlayService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                Log.d(TAG, "Boot completed, checking auto-lock settings")
                checkAutoLock(context)
            }
        }
    }
    
    private fun checkAutoLock(context: Context) {
        val preferencesManager = PreferencesManager(context)
        val serviceScope = CoroutineScope(Dispatchers.Main)
        
        serviceScope.launch {
            try {
                val config = preferencesManager.getLockConfiguration()
                
                if (config.autoLockOnBoot) {
                    Log.d(TAG, "Auto-lock enabled, starting lock service")
                    val lockIntent = Intent(context, LockOverlayService::class.java)
                    lockIntent.putExtra("lock_config", config)
                    context.startForegroundService(lockIntent)
                } else {
                    Log.d(TAG, "Auto-lock disabled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking auto-lock settings", e)
            }
        }
    }
}
