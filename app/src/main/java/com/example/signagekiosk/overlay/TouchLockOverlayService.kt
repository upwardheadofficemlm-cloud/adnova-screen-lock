package com.example.signagekiosk.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import com.example.signagekiosk.R

class TouchLockOverlayService : Service() {
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var cornerTapCount: Int = 0
    private var lastTapMillis: Long = 0

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.signagekiosk.action.TOUCH_LOCK") {
                val enable = intent.getBooleanExtra("enable", false)
                if (enable) showOverlay() else hideOverlay()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        registerReceiver(receiver, IntentFilter("com.example.signagekiosk.action.TOUCH_LOCK"))
        startForeground(1, buildNotification())
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        hideOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val channelId = "touch_lock"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(
                NotificationChannel(channelId, "Touch Lock", NotificationManager.IMPORTANCE_MIN)
            )
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else Notification.Builder(this)
        return builder.setContentTitle(getString(R.string.locked_touch))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
    }

    private fun showOverlay() {
        if (overlayView != null) return
        val inflater = LayoutInflater.from(this)
        val container = FrameLayout(this)
        container.setOnTouchListener { _, _ -> true }
        val corner = View(this)
        corner.setBackgroundColor(0x00000000)
        val size = (48 * resources.displayMetrics.density).toInt()
        val paramsCorner = FrameLayout.LayoutParams(size, size)
        paramsCorner.gravity = Gravity.TOP or Gravity.END
        corner.setOnClickListener { handleCornerTap() }
        container.addView(corner, paramsCorner)
        overlayView = container
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        windowManager.addView(overlayView, params)
    }

    private fun hideOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
        }
        overlayView = null
        cornerTapCount = 0
    }

    private fun handleCornerTap() {
        val now = System.currentTimeMillis()
        if (now - lastTapMillis > 2000) {
            cornerTapCount = 0
        }
        lastTapMillis = now
        cornerTapCount++
        if (cornerTapCount >= 5) {
            cornerTapCount = 0
            val intent = Intent(this, PinUnlockActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}

