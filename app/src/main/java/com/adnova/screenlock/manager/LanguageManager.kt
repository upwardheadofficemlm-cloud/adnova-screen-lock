package com.adnova.screenlock.manager

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.adnova.screenlock.data.PreferencesManager
import java.util.*

/**
 * Multi-language support manager
 */
class LanguageManager(private val context: Context) {
    
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val TAG = "LanguageManager"
        private const val LANGUAGE_KEY = "selected_language"
        private const val DEFAULT_LANGUAGE = "en"
    }
    
    /**
     * Supported languages
     */
    val supportedLanguages = mapOf(
        "en" to "English",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "it" to "Italiano",
        "pt" to "Português",
        "ru" to "Русский",
        "zh" to "中文",
        "ja" to "日本語",
        "ko" to "한국어",
        "ar" to "العربية",
        "hi" to "हिन्दी"
    )
    
    /**
     * Set application language
     */
    fun setLanguage(languageCode: String) {
        try {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)
            
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            
            preferencesManager.saveString(LANGUAGE_KEY, languageCode)
            
            Log.d(TAG, "Language set to: $languageCode")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set language", e)
        }
    }
    
    /**
     * Get current language
     */
    fun getCurrentLanguage(): String {
        return preferencesManager.getString(LANGUAGE_KEY) ?: DEFAULT_LANGUAGE
    }
    
    /**
     * Get localized string
     */
    fun getString(key: String, vararg args: Any): String {
        return try {
            val resourceId = context.resources.getIdentifier(key, "string", context.packageName)
            if (resourceId != 0) {
                context.getString(resourceId, *args)
            } else {
                key // Return key if string not found
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get localized string", e)
            key
        }
    }
}
