package com.example.signagekiosk.messaging

import android.content.Intent
import com.example.signagekiosk.kiosk.KioskManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class SignageFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val action = data["action"]
        val kiosk = KioskManager(applicationContext)
        when (action) {
            "touchLock" -> kiosk.lockTouch(true)
            "touchUnlock" -> kiosk.lockTouch(false)
            "sleep" -> kiosk.sleepScreen()
            "wake" -> kiosk.wakeScreen()
            "reboot" -> kiosk.rebootDevice()
            else -> {}
        }
    }

    override fun onNewToken(token: String) {
        // TODO: upload token to Firestore with device ID
        super.onNewToken(token)
    }
}

