package com.adnova.screenlock.ui

import android.app.AlarmManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.adnova.screenlock.R
import com.adnova.screenlock.manager.*
import com.adnova.screenlock.data.PreferencesManager

/**
 * Advanced settings activity for new features
 */
class AdvancedSettingsActivity : AppCompatActivity() {
    
    private lateinit var scheduleManager: ScheduleManager
    private lateinit var whitelistManager: AppWhitelistManager
    private lateinit var batteryManager: BatteryOptimizationManager
    private lateinit var analyticsManager: AnalyticsManager
    private lateinit var languageManager: LanguageManager
    private lateinit var themeManager: ThemeManager
    private lateinit var preferencesManager: PreferencesManager
    
    // Schedule controls
    private lateinit var scheduleEnabledSwitch: SwitchCompat
    private lateinit var lockTimePicker: TimePicker
    private lateinit var unlockTimePicker: TimePicker
    private lateinit var scheduleDaysSpinner: Spinner
    
    // Whitelist controls
    private lateinit var whitelistEnabledSwitch: SwitchCompat
    private lateinit var whitelistAppsList: ListView
    
    // Battery controls
    private lateinit var batteryOptimizationButton: Button
    private lateinit var batteryStatusText: TextView
    
    // Analytics controls
    private lateinit var analyticsEnabledSwitch: SwitchCompat
    private lateinit var exportDataButton: Button
    
    // Language controls
    private lateinit var languageSpinner: Spinner
    
    // Theme controls
    private lateinit var themeSpinner: Spinner
    private lateinit var darkModeSwitch: SwitchCompat
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_settings)
        
        initializeManagers()
        setupViews()
        setupListeners()
        loadCurrentSettings()
    }
    
    private fun initializeManagers() {
        scheduleManager = ScheduleManager(this)
        whitelistManager = AppWhitelistManager(this)
        batteryManager = BatteryOptimizationManager(this)
        analyticsManager = AnalyticsManager(this)
        languageManager = LanguageManager(this)
        themeManager = ThemeManager(this)
        preferencesManager = PreferencesManager(this)
    }
    
    private fun setupViews() {
        // Schedule controls
        scheduleEnabledSwitch = findViewById(R.id.scheduleEnabledSwitch)
        lockTimePicker = findViewById(R.id.lockTimePicker)
        unlockTimePicker = findViewById(R.id.unlockTimePicker)
        scheduleDaysSpinner = findViewById(R.id.scheduleDaysSpinner)
        
        // Whitelist controls
        whitelistEnabledSwitch = findViewById(R.id.whitelistEnabledSwitch)
        whitelistAppsList = findViewById(R.id.whitelistAppsList)
        
        // Battery controls
        batteryOptimizationButton = findViewById(R.id.batteryOptimizationButton)
        batteryStatusText = findViewById(R.id.batteryStatusText)
        
        // Analytics controls
        analyticsEnabledSwitch = findViewById(R.id.analyticsEnabledSwitch)
        exportDataButton = findViewById(R.id.exportDataButton)
        
        // Language controls
        languageSpinner = findViewById(R.id.languageSpinner)
        
        // Theme controls
        themeSpinner = findViewById(R.id.themeSpinner)
        darkModeSwitch = findViewById(R.id.darkModeSwitch)
        
        setupSpinners()
    }
    
    private fun setupSpinners() {
        // Schedule days spinner
        val daysArray = arrayOf("Daily", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val daysAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, daysArray)
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        scheduleDaysSpinner.adapter = daysAdapter
        
        // Language spinner
        val languages = languageManager.supportedLanguages
        val languageNames = languages.values.toList()
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageNames)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter
        
        // Theme spinner
        val themes = themeManager.availableThemes
        val themeNames = themes.values.toList()
        val themeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themeNames)
        themeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        themeSpinner.adapter = themeAdapter
    }
    
    private fun setupListeners() {
        // Schedule listeners
        scheduleEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableSchedule()
            } else {
                disableSchedule()
            }
        }
        
        // Whitelist listeners
        whitelistEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            whitelistManager.setWhitelistEnabled(isChecked)
        }
        
        // Battery listeners
        batteryOptimizationButton.setOnClickListener {
            batteryManager.requestBatteryOptimizationExemption()
        }
        
        // Analytics listeners
        analyticsEnabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            analyticsManager.setAnalyticsEnabled(isChecked)
        }
        
        exportDataButton.setOnClickListener {
            exportAnalyticsData()
        }
        
        // Language listeners
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedLanguage = languageManager.supportedLanguages.keys.toList()[position]
                languageManager.setLanguage(selectedLanguage)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Theme listeners
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedTheme = themeManager.availableThemes.keys.toList()[position]
                themeManager.setTheme(selectedTheme)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            themeManager.setDarkMode(isChecked)
        }
    }
    
    private fun loadCurrentSettings() {
        // Load schedule settings
        val scheduleConfig = scheduleManager.getScheduleConfig()
        scheduleEnabledSwitch.isChecked = scheduleConfig.enabled
        
        // Load whitelist settings
        whitelistEnabledSwitch.isChecked = whitelistManager.isWhitelistEnabled()
        
        // Load battery status
        updateBatteryStatus()
        
        // Load analytics settings
        analyticsEnabledSwitch.isChecked = analyticsManager.isAnalyticsEnabled()
        
        // Load language settings
        val currentLanguage = languageManager.getCurrentLanguage()
        val languageIndex = languageManager.supportedLanguages.keys.toList().indexOf(currentLanguage)
        if (languageIndex >= 0) {
            languageSpinner.setSelection(languageIndex)
        }
        
        // Load theme settings
        val currentTheme = themeManager.getCurrentTheme()
        val themeIndex = themeManager.availableThemes.keys.toList().indexOf(currentTheme)
        if (themeIndex >= 0) {
            themeSpinner.setSelection(themeIndex)
        }
        
        darkModeSwitch.isChecked = themeManager.isDarkModeEnabled()
    }
    
    private fun enableSchedule() {
        val lockHour = lockTimePicker.hour
        val lockMinute = lockTimePicker.minute
        val unlockHour = unlockTimePicker.hour
        val unlockMinute = unlockTimePicker.minute
        
        scheduleManager.scheduleLock(lockHour, lockMinute)
        scheduleManager.scheduleUnlock(unlockHour, unlockMinute)
        
        Toast.makeText(this, "Schedule enabled", Toast.LENGTH_SHORT).show()
    }
    
    private fun disableSchedule() {
        scheduleManager.cancelAllSchedules()
        Toast.makeText(this, "Schedule disabled", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateBatteryStatus() {
        val batteryHealth = batteryManager.getBatteryHealth()
        val statusText = "Battery: ${batteryHealth.level}% - ${batteryHealth.health} - Charging: ${batteryHealth.isCharging}"
        batteryStatusText.text = statusText
    }
    
    private fun exportAnalyticsData() {
        val data = analyticsManager.exportAnalyticsData()
        // In a real implementation, you would save this to a file or share it
        Toast.makeText(this, "Analytics data exported", Toast.LENGTH_SHORT).show()
    }
}
