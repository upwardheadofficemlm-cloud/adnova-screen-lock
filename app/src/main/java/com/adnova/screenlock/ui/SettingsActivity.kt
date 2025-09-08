package com.adnova.screenlock.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.adnova.screenlock.R
import com.adnova.screenlock.data.PreferencesManager
import com.adnova.screenlock.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        
        setupToolbar()
        setupClickListeners()
        setupSliderListeners()
        loadSettings()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSetPin.setOnClickListener {
            showPinSetupDialog()
        }
        
        binding.btnAdminLogin.setOnClickListener {
            showAdminLoginDialog()
        }
        
        binding.btnSyncConfig.setOnClickListener {
            syncRemoteConfiguration()
        }
        
        // Switch listeners
        binding.switchFloatingButton.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }
        
        binding.switchDoubleTap.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }
        
        binding.switchPinUnlock.setOnCheckedChangeListener { _, isChecked ->
            binding.btnSetPin.isEnabled = isChecked
            saveSettings()
        }
        
        binding.switchBlockIcon.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }
        
        binding.switchAnimation.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }
        
        binding.switchVolumeLock.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }
        
        binding.switchAutoLock.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }
        
        binding.switchKioskMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showKioskModeWarning()
            }
            saveSettings()
        }
    }
    
    private fun setupSliderListeners() {
        binding.sliderTapsToReveal.addOnChangeListener { _, value, _ ->
            binding.textTapsValue.text = value.toInt().toString()
            saveSettings()
        }
    }
    
    private fun loadSettings() {
        lifecycleScope.launch {
            val config = preferencesManager.getLockConfiguration()
            
            binding.switchFloatingButton.isChecked = config.floatingButtonEnabled
            binding.switchDoubleTap.isChecked = config.doubleTapUnlock
            binding.sliderTapsToReveal.value = config.tapsToReveal.toFloat()
            binding.textTapsValue.text = config.tapsToReveal.toString()
            binding.switchPinUnlock.isChecked = config.pinUnlockEnabled
            binding.btnSetPin.isEnabled = config.pinUnlockEnabled
            binding.switchBlockIcon.isChecked = config.blockIconEnabled
            binding.switchAnimation.isChecked = config.animationEnabled
            binding.switchVolumeLock.isChecked = config.volumeButtonLock
            binding.switchAutoLock.isChecked = config.autoLockOnBoot
            binding.switchKioskMode.isChecked = config.kioskMode
        }
    }
    
    private fun saveSettings() {
        lifecycleScope.launch {
            val currentConfig = preferencesManager.getLockConfiguration()
            
            val newConfig = currentConfig.copy(
                floatingButtonEnabled = binding.switchFloatingButton.isChecked,
                doubleTapUnlock = binding.switchDoubleTap.isChecked,
                tapsToReveal = binding.sliderTapsToReveal.value.toInt(),
                pinUnlockEnabled = binding.switchPinUnlock.isChecked,
                blockIconEnabled = binding.switchBlockIcon.isChecked,
                animationEnabled = binding.switchAnimation.isChecked,
                volumeButtonLock = binding.switchVolumeLock.isChecked,
                autoLockOnBoot = binding.switchAutoLock.isChecked,
                kioskMode = binding.switchKioskMode.isChecked
            )
            
            preferencesManager.saveLockConfiguration(newConfig)
        }
    }
    
    private fun showPinSetupDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Set PIN")
            .setMessage("Enter a 4-6 digit PIN for unlocking the screen")
            .setView(R.layout.dialog_pin_setup)
            .setPositiveButton("Set PIN") { dialog, _ ->
                // TODO: Implement PIN setup logic
                Toast.makeText(this, "PIN setup will be implemented", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun showAdminLoginDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Admin Login")
            .setMessage("Firebase admin authentication will be implemented")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    private fun syncRemoteConfiguration() {
        Toast.makeText(this, "Syncing remote configuration...", Toast.LENGTH_SHORT).show()
        // TODO: Implement Firebase Remote Config sync
    }
    
    private fun showKioskModeWarning() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Kiosk Mode")
            .setMessage("Kiosk mode will prevent users from accessing system buttons and exiting the app. This requires additional device admin permissions.")
            .setPositiveButton("Enable") { dialog, _ ->
                // TODO: Request device admin permissions
                Toast.makeText(this, "Device admin setup will be implemented", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                binding.switchKioskMode.isChecked = false
                dialog.dismiss()
            }
            .show()
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
