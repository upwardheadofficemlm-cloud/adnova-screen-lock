package com.example.signagekiosk.kiosk

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.signagekiosk.admin.SignageDeviceAdminReceiver

class KioskManager(private val context: Context) {
    private val dpm: DevicePolicyManager? =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
    private val admin: ComponentName = ComponentName(context, SignageDeviceAdminReceiver::class.java)

    fun ensureLockTask(activity: Activity) {
        if (dpm?.isDeviceOwnerApp(context.packageName) == true) {
            dpm.setLockTaskPackages(admin, arrayOf(context.packageName))
        }
        try {
            activity.startLockTask()
        } catch (_: Exception) {
        }
    }

    fun lockTouch(enable: Boolean) {
        val intent = Intent("com.example.signagekiosk.action.TOUCH_LOCK")
        intent.putExtra("enable", enable)
        context.sendBroadcast(intent)
    }

    fun rebootDevice() {
        if (dpm?.isDeviceOwnerApp(context.packageName) == true) {
            dpm.reboot(admin)
        }
    }

    fun sleepScreen() {
        if (dpm?.isDeviceOwnerApp(context.packageName) == true) {
            dpm.lockNow()
        }
    }

    fun wakeScreen() {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SignageKiosk:Wake"
        )
        wl.acquire(3000)
        wl.release()
    }
}

