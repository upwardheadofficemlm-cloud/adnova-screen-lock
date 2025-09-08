package com.adnova.screenlock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.adnova.screenlock.R
import com.adnova.screenlock.data.LockConfiguration
import com.adnova.screenlock.data.LockStatus
import com.adnova.screenlock.data.LockType
import com.adnova.screenlock.data.PreferencesManager
import com.adnova.screenlock.databinding.OverlayLockBinding
import com.adnova.screenlock.ui.MainActivity
import com.adnova.screenlock.utils.LockAreaCalculator
import com.adnova.screenlock.utils.PinManager
import com.adnova.screenlock.utils.PinVerificationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

class LockOverlayService : Service() {
    
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var binding: OverlayLockBinding? = null
    private var preferencesManager: PreferencesManager? = null
    private var lockConfiguration: LockConfiguration? = null
    private var lockAreaCalculator: LockAreaCalculator? = null
    private var pinManager: PinManager? = null
    
    private var tapCount = 0
    private var lastTapTime = 0L
    private val tapTimeout = 2000L // 2 seconds
    private var lockAreas: List<android.graphics.Rect> = emptyList()
    
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "screen_lock_channel"
        private const val ACTION_STOP_LOCK = "stop_lock"
    }
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        preferencesManager = PreferencesManager(this)
        lockAreaCalculator = LockAreaCalculator(this)
        pinManager = PinManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_LOCK -> {
                stopLock()
                return START_NOT_STICKY
            }
        }
        
        lockConfiguration = intent?.getParcelableExtra("lock_config")
        if (lockConfiguration == null) {
            serviceScope.launch {
                lockConfiguration = preferencesManager?.getLockConfiguration()
            }
        }
        
        startForeground(NOTIFICATION_ID, createNotification())
        showOverlay()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val stopIntent = Intent(this, LockOverlayService::class.java).apply {
            action = ACTION_STOP_LOCK
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_lock)
            .setContentIntent(mainPendingIntent)
            .addAction(R.drawable.ic_lock_open, "Stop Lock", stopPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun showOverlay() {
        if (overlayView != null) return
        
        val inflater = LayoutInflater.from(this)
        binding = OverlayLockBinding.inflate(inflater)
        overlayView = binding?.root
        
        setupOverlayView()
        setupTouchHandling()
        
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        
        layoutParams.gravity = Gravity.TOP or Gravity.START
        
        try {
            windowManager?.addView(overlayView, layoutParams)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to show overlay: ${e.message}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }
    
    private fun setupOverlayView() {
        val config = lockConfiguration ?: return
        
        // Calculate lock areas
        lockAreas = lockAreaCalculator?.calculateLockAreas(
            config.lockType,
            config.edgeType,
            config.customArea
        ) ?: emptyList()
        
        when (config.lockType) {
            LockType.FULL_SCREEN -> {
                binding?.overlayView?.setBackgroundColor(
                    resources.getColor(R.color.lock_overlay, null)
                )
            }
            LockType.EDGE_LOCK -> {
                setupEdgeLock(config)
            }
            LockType.CUSTOM_AREA -> {
                setupCustomAreaLock(config)
            }
        }
        
        // Setup floating unlock button
        if (config.floatingButtonEnabled) {
            binding?.floatingUnlockButton?.visibility = View.VISIBLE
            binding?.floatingUnlockButton?.setOnClickListener {
                handleUnlockAttempt()
            }
        } else {
            binding?.floatingUnlockButton?.visibility = View.GONE
        }
        
        // Setup PIN dialog
        setupPinDialog()
    }
    
    private fun setupEdgeLock(config: LockConfiguration) {
        // TODO: Implement edge-specific locking
        binding?.overlayView?.setBackgroundColor(
            resources.getColor(R.color.lock_overlay, null)
        )
    }
    
    private fun setupCustomAreaLock(config: LockConfiguration) {
        // TODO: Implement custom area locking
        binding?.overlayView?.setBackgroundColor(
            resources.getColor(R.color.lock_overlay, null)
        )
    }
    
    private fun setupTouchHandling() {
        binding?.overlayView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleTouch(event)
                    true
                }
                else -> false
            }
        }
    }
    
    private fun handleTouch(event: MotionEvent) {
        val config = lockConfiguration ?: return
        val currentTime = System.currentTimeMillis()
        
        // Check if touch is in a locked area
        if (!lockAreaCalculator?.isPointInLockArea(event.x, event.y, lockAreas) == true) {
            return // Touch is not in a locked area, ignore
        }
        
        // Reset tap count if too much time has passed
        if (currentTime - lastTapTime > tapTimeout) {
            tapCount = 0
        }
        
        tapCount++
        lastTapTime = currentTime
        
        // Show floating button after required taps
        if (config.floatingButtonEnabled && tapCount >= config.tapsToReveal) {
            binding?.floatingUnlockButton?.visibility = View.VISIBLE
        }
        
        // Show block icon if enabled
        if (config.blockIconEnabled) {
            showBlockIcon(event.x, event.y)
        }
        
        // Show animation if enabled
        if (config.animationEnabled) {
            showTouchAnimation(event.x, event.y)
        }
    }
    
    private fun showBlockIcon(x: Float, y: Float) {
        // TODO: Implement block icon display
    }
    
    private fun showTouchAnimation(x: Float, y: Float) {
        // TODO: Implement touch animation
    }
    
    private fun handleUnlockAttempt() {
        val config = lockConfiguration ?: return
        
        if (config.pinUnlockEnabled) {
            showPinDialog()
        } else {
            unlockScreen()
        }
    }
    
    private fun setupPinDialog() {
        binding?.btnPinOk?.setOnClickListener {
            val pin = binding?.editPinInput?.text?.toString() ?: ""
            verifyPin(pin)
        }
        
        binding?.btnPinCancel?.setOnClickListener {
            hidePinDialog()
        }
    }
    
    private fun showPinDialog() {
        binding?.pinDialog?.visibility = View.VISIBLE
        binding?.editPinInput?.text?.clear()
        binding?.editPinInput?.requestFocus()
    }
    
    private fun hidePinDialog() {
        binding?.pinDialog?.visibility = View.GONE
        binding?.editPinInput?.text?.clear()
    }
    
    private fun verifyPin(pin: String) {
        val result = pinManager?.verifyPin(pin)
        
        when (result) {
            is PinVerificationResult.SUCCESS -> {
                hidePinDialog()
                unlockScreen()
            }
            is PinVerificationResult.INCORRECT -> {
                Toast.makeText(this, R.string.pin_incorrect, Toast.LENGTH_SHORT).show()
                binding?.editPinInput?.text?.clear()
            }
            is PinVerificationResult.LOCKED_OUT -> {
                Toast.makeText(this, "Locked out for ${result.remainingSeconds} seconds", Toast.LENGTH_LONG).show()
                hidePinDialog()
            }
            is PinVerificationResult.NOT_SET -> {
                unlockScreen() // No PIN set, allow unlock
            }
            else -> {
                Toast.makeText(this, "Invalid PIN format", Toast.LENGTH_SHORT).show()
                binding?.editPinInput?.text?.clear()
            }
        }
    }
    
    private fun unlockScreen() {
        serviceScope.launch {
            val status = LockStatus(isLocked = false)
            preferencesManager?.saveLockStatus(status)
        }
        
        stopLock()
    }
    
    private fun stopLock() {
        overlayView?.let { view ->
            try {
                windowManager?.removeView(view)
            } catch (e: Exception) {
                // View might already be removed
            }
        }
        
        overlayView = null
        binding = null
        
        stopForeground(true)
        stopSelf()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopLock()
    }
    
    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(pin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
