package com.adnova.screenlock.manager

import android.content.Context
import android.util.Log
import com.adnova.screenlock.data.PreferencesManager

/**
 * Theme and UI customization manager
 */
class ThemeManager(private val context: Context) {
    
    private val preferencesManager = PreferencesManager(context)
    
    companion object {
        private const val TAG = "ThemeManager"
        private const val THEME_KEY = "selected_theme"
        private const val ACCENT_COLOR_KEY = "accent_color"
        private const val FONT_SIZE_KEY = "font_size"
        private const val DARK_MODE_KEY = "dark_mode"
    }
    
    /**
     * Available themes
     */
    val availableThemes = mapOf(
        "default" to "Default",
        "dark" to "Dark",
        "light" to "Light",
        "blue" to "Blue",
        "green" to "Green",
        "purple" to "Purple",
        "orange" to "Orange"
    )
    
    /**
     * Set application theme
     */
    fun setTheme(themeName: String) {
        try {
            preferencesManager.saveString(THEME_KEY, themeName)
            Log.d(TAG, "Theme set to: $themeName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set theme", e)
        }
    }
    
    /**
     * Get current theme
     */
    fun getCurrentTheme(): String {
        return preferencesManager.getString(THEME_KEY) ?: "default"
    }
    
    /**
     * Set accent color
     */
    fun setAccentColor(color: Int) {
        try {
            preferencesManager.saveInt(ACCENT_COLOR_KEY, color)
            Log.d(TAG, "Accent color set to: $color")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set accent color", e)
        }
    }
    
    /**
     * Get accent color
     */
    fun getAccentColor(): Int {
        return preferencesManager.getInt(ACCENT_COLOR_KEY, 0xFF007BFF.toInt())
    }
    
    /**
     * Set font size
     */
    fun setFontSize(size: Float) {
        try {
            preferencesManager.saveFloat(FONT_SIZE_KEY, size)
            Log.d(TAG, "Font size set to: $size")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set font size", e)
        }
    }
    
    /**
     * Get font size
     */
    fun getFontSize(): Float {
        return preferencesManager.getFloat(FONT_SIZE_KEY, 14f)
    }
    
    /**
     * Enable/disable dark mode
     */
    fun setDarkMode(enabled: Boolean) {
        try {
            preferencesManager.saveBoolean(DARK_MODE_KEY, enabled)
            Log.d(TAG, "Dark mode: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set dark mode", e)
        }
    }
    
    /**
     * Check if dark mode is enabled
     */
    fun isDarkModeEnabled(): Boolean {
        return preferencesManager.getBoolean(DARK_MODE_KEY, false)
    }
}
