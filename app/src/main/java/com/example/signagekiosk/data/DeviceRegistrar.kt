package com.example.signagekiosk.data

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object DeviceRegistrar {
    fun deviceId(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId ?: (Build.SERIAL ?: Build.MODEL ?: "unknown")
    }

    fun register(context: Context) {
        try {
            FirebaseApp.initializeApp(context)
        } catch (_: Throwable) { }
        val id = deviceId(context)
        val db = FirebaseFirestore.getInstance()
        val doc = db.collection("devices").document(id)
        val data = hashMapOf(
            "id" to id,
            "model" to Build.MODEL,
            "manufacturer" to Build.MANUFACTURER,
            "sdk" to Build.VERSION.SDK_INT,
            "lastSeen" to com.google.firebase.Timestamp.now()
        )
        doc.set(data)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            doc.update("fcmToken", token)
        }
    }

    fun updateToken(context: Context, token: String) {
        val id = deviceId(context)
        FirebaseFirestore.getInstance().collection("devices").document(id).update("fcmToken", token)
    }
}

