package com.adnova.screenlock.utils

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.adnova.screenlock.receiver.DeviceAdminReceiver

class KioskModeManager(private val context: Context) {
    
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
    
    companion object {
        private const val TAG = "KioskModeManager"
    }
    
    fun isDeviceAdmin(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }
    
    fun requestDeviceAdminPermission(activity: Activity, launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
            "AdNova Screen Lock needs device admin permission to enable kiosk mode")
        launcher.launch(intent)
    }
    
    fun enableKioskMode(activity: Activity): Boolean {
        if (!isDeviceAdmin()) {
            Log.w(TAG, "Device admin permission not granted")
            return false
        }
        
        return try {
            // Lock the task to prevent users from switching apps
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.startLockTask()
            }
            
            // Disable keyguard if possible
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                devicePolicyManager.setKeyguardDisabled(adminComponent, true)
            }
            
            // Hide status bar and navigation bar
            hideSystemUI(activity)
            
            Log.d(TAG, "Kiosk mode enabled")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable kiosk mode", e)
            false
        }
    }
    
    fun disableKioskMode(activity: Activity): Boolean {
        return try {
            // Stop lock task
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activity.stopLockTask()
            }
            
            // Re-enable keyguard
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                devicePolicyManager.setKeyguardDisabled(adminComponent, false)
            }
            
            // Show system UI
            showSystemUI(activity)
            
            Log.d(TAG, "Kiosk mode disabled")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable kiosk mode", e)
            false
        }
    }
    
    fun isKioskModeActive(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.isInLockTaskMode
        } else {
            false
        }
    }
    
    fun blockSystemButtons(): Boolean {
        if (!isDeviceAdmin()) {
            return false
        }
        
        return try {
            // Disable home button (requires system-level permissions)
            // This is limited on non-rooted devices
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                devicePolicyManager.setLockTaskPackages(adminComponent, arrayOf(context.packageName))
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to block system buttons", e)
            false
        }
    }
    
    fun enableStayAwake(): Boolean {
        if (!isDeviceAdmin()) {
            return false
        }
        
        return try {
            // Keep screen on (requires WAKE_LOCK permission)
            devicePolicyManager.setMaximumTimeToLock(adminComponent, 0)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable stay awake", e)
            false
        }
    }
    
    private fun hideSystemUI(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.let { controller ->
                controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
    }
    
    private fun showSystemUI(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.let { controller ->
                controller.show(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
    
    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
    
    fun isAccessibilityServiceEnabled(): Boolean {
        // Check if our accessibility service is enabled
        // This would require implementing an accessibility service
        return false
    }
}
