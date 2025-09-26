package com.example.signagekiosk.overlay

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.signagekiosk.R

class PinUnlockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_unlock)

        val editPin: EditText = findViewById(R.id.editPin)
        val btnUnlock: Button = findViewById(R.id.btnUnlock)

        btnUnlock.setOnClickListener {
            val pin = editPin.text?.toString()?.trim().orEmpty()
            if (pin.isEmpty()) return@setOnClickListener
            if (validatePin(pin)) {
                sendUnlock()
                finish()
            } else {
                Toast.makeText(this, R.string.invalid_pin, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validatePin(input: String): Boolean {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val prefs = EncryptedSharedPreferences.create(
            this,
            "signage_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val saved = prefs.getString("kiosk_pin", "1234")
        return input == saved
    }

    private fun sendUnlock() {
        val intent = android.content.Intent("com.example.signagekiosk.action.TOUCH_LOCK")
        intent.putExtra("enable", false)
        sendBroadcast(intent)
    }
}

