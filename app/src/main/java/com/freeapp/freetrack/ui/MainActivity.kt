package com.freeapp.freetrack.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.freeapp.freetrack.R
import androidx.lifecycle.lifecycleScope
import com.freeapp.freetrack.data.AppDatabase
import com.freeapp.freetrack.databinding.ActivityMainBinding
import com.freeapp.freetrack.service.TrackingService
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val database by lazy { AppDatabase.getDatabase(this) }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (fineLocationGranted) {
            startTrackingService()
        } else {
            Toast.makeText(this, "Fine Location permission is required for GPS tracking", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Bottom Navigation between: Map, Elevation Chart, Weight Log
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, MapFragment())
                        .commit()
                    true
                }
                R.id.nav_elevation -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ElevationChartFragment())
                        .commit()
                    true
                }
                R.id.nav_weight -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, WeightLogFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        // Start button toggles foreground GPS + Barometer service
        binding.fabTrackingToggle.setOnClickListener {
            if (TrackingService.isTrackingActive) {
                stopTrackingService()
            } else {
                checkPermissionsAndStartTracking()
            }
        }

        binding.fabResetRoute.setOnClickListener {
            resetRoute()
        }

        binding.fabResetRoute.visibility = View.VISIBLE

        // Default screen is Map
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, MapFragment())
                .commit()
        }
    }

    private fun resetRoute() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset Route")
            .setMessage("Are you sure you want to clear all tracked data and reset the distance?")
            .setPositiveButton("Reset") { _, _ ->
                lifecycleScope.launch {
                    database.gpsPointDao().clearAll()
                    val intent = Intent(this@MainActivity, TrackingService::class.java).apply {
                        action = TrackingService.ACTION_RESET_TRACKING
                    }
                    startService(intent)
                    Toast.makeText(this@MainActivity, "Route Reset", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkPermissionsAndStartTracking() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            startTrackingService()
        } else {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun startTrackingService() {
        val intent = Intent(this, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START_TRACKING
        }
        ContextCompat.startForegroundService(this, intent)
        binding.fabTrackingToggle.setImageResource(R.drawable.ic_pause)
        Toast.makeText(this, "Started Native GPS & Barometer Tracking", Toast.LENGTH_SHORT).show()
    }

    private fun stopTrackingService() {
        val intent = Intent(this, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP_TRACKING
        }
        startService(intent)
        binding.fabTrackingToggle.setImageResource(R.drawable.ic_play)
        Toast.makeText(this, "Tracking Stopped", Toast.LENGTH_SHORT).show()
    }
}