package com.adnova.screenlock.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class PinManager(context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PIN_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private const val PIN_PREFS_NAME = "pin_preferences"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_PIN_ATTEMPTS = "pin_attempts"
        private const val KEY_PIN_LOCKOUT_TIME = "pin_lockout_time"
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_DURATION = 30000L // 30 seconds
    }
    
    fun setPin(pin: String): Boolean {
        return try {
            if (!isValidPin(pin)) return false
            
            val salt = generateSalt()
            val hash = hashPin(pin, salt)
            
            sharedPreferences.edit()
                .putString(KEY_PIN_HASH, hash)
                .putString(KEY_PIN_SALT, salt)
                .putInt(KEY_PIN_ATTEMPTS, 0)
                .putLong(KEY_PIN_LOCKOUT_TIME, 0)
                .apply()
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun verifyPin(pin: String): PinVerificationResult {
        if (!isValidPin(pin)) {
            return PinVerificationResult.INVALID_FORMAT
        }
        
        val lockoutTime = sharedPreferences.getLong(KEY_PIN_LOCKOUT_TIME, 0)
        if (System.currentTimeMillis() < lockoutTime) {
            val remainingTime = (lockoutTime - System.currentTimeMillis()) / 1000
            return PinVerificationResult.LOCKED_OUT(remainingTime.toInt())
        }
        
        val storedHash = sharedPreferences.getString(KEY_PIN_HASH, null)
        val storedSalt = sharedPreferences.getString(KEY_PIN_SALT, null)
        
        if (storedHash == null || storedSalt == null) {
            return PinVerificationResult.NOT_SET
        }
        
        val inputHash = hashPin(pin, storedSalt)
        
        return if (inputHash == storedHash) {
            // Reset attempts on successful verification
            sharedPreferences.edit()
                .putInt(KEY_PIN_ATTEMPTS, 0)
                .putLong(KEY_PIN_LOCKOUT_TIME, 0)
                .apply()
            PinVerificationResult.SUCCESS
        } else {
            incrementFailedAttempts()
            PinVerificationResult.INCORRECT
        }
    }
    
    fun isPinSet(): Boolean {
        return sharedPreferences.getString(KEY_PIN_HASH, null) != null
    }
    
    fun clearPin() {
        sharedPreferences.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .putInt(KEY_PIN_ATTEMPTS, 0)
            .putLong(KEY_PIN_LOCKOUT_TIME, 0)
            .apply()
    }
    
    fun getRemainingAttempts(): Int {
        val attempts = sharedPreferences.getInt(KEY_PIN_ATTEMPTS, 0)
        return maxOf(0, MAX_ATTEMPTS - attempts)
    }
    
    fun isLockedOut(): Boolean {
        val lockoutTime = sharedPreferences.getLong(KEY_PIN_LOCKOUT_TIME, 0)
        return System.currentTimeMillis() < lockoutTime
    }
    
    fun getLockoutRemainingTime(): Long {
        val lockoutTime = sharedPreferences.getLong(KEY_PIN_LOCKOUT_TIME, 0)
        return maxOf(0, lockoutTime - System.currentTimeMillis())
    }
    
    private fun isValidPin(pin: String): Boolean {
        return pin.length in 4..6 && pin.all { it.isDigit() }
    }
    
    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltedPin = pin + salt
        val hash = digest.digest(saltedPin.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }
    
    private fun incrementFailedAttempts() {
        val currentAttempts = sharedPreferences.getInt(KEY_PIN_ATTEMPTS, 0) + 1
        
        if (currentAttempts >= MAX_ATTEMPTS) {
            // Lock out for specified duration
            val lockoutTime = System.currentTimeMillis() + LOCKOUT_DURATION
            sharedPreferences.edit()
                .putInt(KEY_PIN_ATTEMPTS, currentAttempts)
                .putLong(KEY_PIN_LOCKOUT_TIME, lockoutTime)
                .apply()
        } else {
            sharedPreferences.edit()
                .putInt(KEY_PIN_ATTEMPTS, currentAttempts)
                .apply()
        }
    }
}

sealed class PinVerificationResult {
    object SUCCESS : PinVerificationResult()
    object INCORRECT : PinVerificationResult()
    object INVALID_FORMAT : PinVerificationResult()
    object NOT_SET : PinVerificationResult()
    data class LOCKED_OUT(val remainingSeconds: Int) : PinVerificationResult()
}
