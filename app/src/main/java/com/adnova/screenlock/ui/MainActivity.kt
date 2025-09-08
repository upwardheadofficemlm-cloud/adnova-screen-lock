package com.adnova.screenlock.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.adnova.screenlock.R
import com.adnova.screenlock.data.LockConfiguration
import com.adnova.screenlock.data.LockStatus
import com.adnova.screenlock.data.LockType
import com.adnova.screenlock.data.EdgeType
import com.adnova.screenlock.data.PreferencesManager
import com.adnova.screenlock.databinding.ActivityMainBinding
import com.adnova.screenlock.service.LockOverlayService
import com.adnova.screenlock.utils.PermissionUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager
    private var isLockActive = false
    
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            checkOverlayPermission()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        preferencesManager = PreferencesManager(this)
        
        setupToolbar()
        setupClickListeners()
        setupRadioGroupListeners()
        setupSwitchListeners()
        
        // Check permissions and update UI
        checkOverlayPermission()
        updateLockStatus()
        loadConfiguration()
    }
    
    override fun onResume() {
        super.onResume()
        checkOverlayPermission()
        updateLockStatus()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }
    
    private fun setupClickListeners() {
        binding.btnToggleLock.setOnClickListener {
            if (isLockActive) {
                stopLock()
            } else {
                startLock()
            }
        }
        
        binding.btnGrantPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PermissionUtils.requestOverlayPermission(this, OVERLAY_PERMISSION_REQUEST_CODE)
            }
        }
        
        binding.fabSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun setupRadioGroupListeners() {
        binding.radioGroupLockType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_full_screen -> {
                    binding.layoutEdgeOptions.visibility = View.GONE
                }
                R.id.radio_edge_lock -> {
                    binding.layoutEdgeOptions.visibility = View.VISIBLE
                }
                R.id.radio_custom_area -> {
                    binding.layoutEdgeOptions.visibility = View.GONE
                    showCustomAreaDialog()
                }
            }
        }
        
        // Edge type chips
        binding.chipLeftEdge.setOnClickListener {
            updateEdgeSelection(EdgeType.LEFT_EDGE)
        }
        binding.chipRightEdge.setOnClickListener {
            updateEdgeSelection(EdgeType.RIGHT_EDGE)
        }
        binding.chipBothEdges.setOnClickListener {
            updateEdgeSelection(EdgeType.BOTH_EDGES)
        }
    }
    
    private fun setupSwitchListeners() {
        binding.switchFloatingButton.setOnCheckedChangeListener { _, isChecked ->
            saveConfiguration()
        }
        
        binding.switchPinUnlock.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showPinSetupDialog()
            } else {
                saveConfiguration()
            }
        }
        
        binding.switchAutoLock.setOnCheckedChangeListener { _, isChecked ->
            saveConfiguration()
        }
    }
    
    private fun checkOverlayPermission() {
        val hasPermission = PermissionUtils.hasOverlayPermission(this)
        preferencesManager.setOverlayPermissionGranted(hasPermission)
        
        if (hasPermission) {
            binding.permissionIndicator.setBackgroundResource(R.drawable.circle_status_unlocked)
            binding.permissionText.text = getString(R.string.permission_granted)
            binding.btnGrantPermission.visibility = View.GONE
        } else {
            binding.permissionIndicator.setBackgroundResource(R.drawable.circle_status_locked)
            binding.permissionText.text = getString(R.string.permission_denied)
            binding.btnGrantPermission.visibility = View.VISIBLE
        }
    }
    
    private fun updateLockStatus() {
        lifecycleScope.launch {
            val status = preferencesManager.getLockStatus()
            isLockActive = status.isLocked
            
            if (isLockActive) {
                binding.statusIndicator.setBackgroundResource(R.drawable.circle_status_locked)
                binding.statusText.text = getString(R.string.locked)
                binding.btnToggleLock.text = getString(R.string.stop_lock)
                binding.btnToggleLock.setIconResource(R.drawable.ic_lock_open)
            } else {
                binding.statusIndicator.setBackgroundResource(R.drawable.circle_status_unlocked)
                binding.statusText.text = getString(R.string.unlocked)
                binding.btnToggleLock.text = getString(R.string.start_lock)
                binding.btnToggleLock.setIconResource(R.drawable.ic_lock)
            }
        }
    }
    
    private fun startLock() {
        if (!PermissionUtils.hasOverlayPermission(this)) {
            Toast.makeText(this, R.string.error_overlay_permission, Toast.LENGTH_LONG).show()
            return
        }
        
        lifecycleScope.launch {
            val config = preferencesManager.getLockConfiguration()
            val status = LockStatus(
                isLocked = true,
                lockType = config.lockType,
                startTime = System.currentTimeMillis()
            )
            
            preferencesManager.saveLockStatus(status)
            
            val intent = Intent(this@MainActivity, LockOverlayService::class.java)
            intent.putExtra("lock_config", config)
            startForegroundService(intent)
            
            updateLockStatus()
        }
    }
    
    private fun stopLock() {
        val intent = Intent(this, LockOverlayService::class.java)
        stopService(intent)
        
        lifecycleScope.launch {
            val status = LockStatus(isLocked = false)
            preferencesManager.saveLockStatus(status)
            updateLockStatus()
        }
    }
    
    private fun loadConfiguration() {
        lifecycleScope.launch {
            val config = preferencesManager.getLockConfiguration()
            
            // Set radio button selection
            when (config.lockType) {
                LockType.FULL_SCREEN -> binding.radioFullScreen.isChecked = true
                LockType.EDGE_LOCK -> {
                    binding.radioEdgeLock.isChecked = true
                    binding.layoutEdgeOptions.visibility = View.VISIBLE
                }
                LockType.CUSTOM_AREA -> binding.radioCustomArea.isChecked = true
            }
            
            // Set edge type chips
            when (config.edgeType) {
                EdgeType.LEFT_EDGE -> binding.chipLeftEdge.isChecked = true
                EdgeType.RIGHT_EDGE -> binding.chipRightEdge.isChecked = true
                EdgeType.BOTH_EDGES -> binding.chipBothEdges.isChecked = true
            }
            
            // Set switches
            binding.switchFloatingButton.isChecked = config.floatingButtonEnabled
            binding.switchPinUnlock.isChecked = config.pinUnlockEnabled
            binding.switchAutoLock.isChecked = config.autoLockOnBoot
        }
    }
    
    private fun saveConfiguration() {
        lifecycleScope.launch {
            val currentConfig = preferencesManager.getLockConfiguration()
            
            val lockType = when (binding.radioGroupLockType.checkedRadioButtonId) {
                R.id.radio_full_screen -> LockType.FULL_SCREEN
                R.id.radio_edge_lock -> LockType.EDGE_LOCK
                R.id.radio_custom_area -> LockType.CUSTOM_AREA
                else -> LockType.FULL_SCREEN
            }
            
            val edgeType = when {
                binding.chipLeftEdge.isChecked -> EdgeType.LEFT_EDGE
                binding.chipRightEdge.isChecked -> EdgeType.RIGHT_EDGE
                binding.chipBothEdges.isChecked -> EdgeType.BOTH_EDGES
                else -> EdgeType.BOTH_EDGES
            }
            
            val newConfig = currentConfig.copy(
                lockType = lockType,
                edgeType = edgeType,
                floatingButtonEnabled = binding.switchFloatingButton.isChecked,
                pinUnlockEnabled = binding.switchPinUnlock.isChecked,
                autoLockOnBoot = binding.switchAutoLock.isChecked
            )
            
            preferencesManager.saveLockConfiguration(newConfig)
        }
    }
    
    private fun updateEdgeSelection(edgeType: EdgeType) {
        // Uncheck other chips
        binding.chipLeftEdge.isChecked = false
        binding.chipRightEdge.isChecked = false
        binding.chipBothEdges.isChecked = false
        
        // Check selected chip
        when (edgeType) {
            EdgeType.LEFT_EDGE -> binding.chipLeftEdge.isChecked = true
            EdgeType.RIGHT_EDGE -> binding.chipRightEdge.isChecked = true
            EdgeType.BOTH_EDGES -> binding.chipBothEdges.isChecked = true
        }
        
        saveConfiguration()
    }
    
    private fun showCustomAreaDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Custom Area Lock")
            .setMessage("Custom area selection will be implemented in the settings screen.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                binding.radioFullScreen.isChecked = true
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                binding.radioFullScreen.isChecked = true
            }
            .show()
    }
    
    private fun showPinSetupDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("PIN Setup")
            .setMessage("PIN setup will be implemented in the settings screen.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                binding.switchPinUnlock.isChecked = false
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                binding.switchPinUnlock.isChecked = false
            }
            .show()
    }
    
    companion object {
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1001
    }
}