package com.freeapp.freetrack.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.freeapp.freetrack.R
import com.freeapp.freetrack.data.AppDatabase
import com.freeapp.freetrack.data.GpsPointEntity
import com.freeapp.freetrack.ui.MainActivity
import com.freeapp.freetrack.util.BarometerHelper
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Foreground Service running 100% locally on Android.
 * Integrates:
 * 1. FusedLocationProviderClient for high-accuracy GPS coordinates and speed
 * 2. SensorManager Barometer (Sensor.TYPE_PRESSURE) for accurate elevation without external APIs
 * 3. Room Local Database insertion for persistent zero-cloud tracking
 */
class TrackingService : Service(), SensorEventListener {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var sensorManager: SensorManager
    private var pressureSensor: Sensor? = null

    private var latestPressureHpa: Float = 1013.25f
    private var smoothedPressureHpa: Float = 1013.25f
    private var isFirstPressureReading = true

    private var lastLocation: Location? = null
    private var totalDistanceKm: Double = 0.0

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }

    companion object {
        const val ACTION_START_TRACKING = "ACTION_START_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        const val NOTIFICATION_CHANNEL_ID = "freetrack_location_channel"
        const val NOTIFICATION_ID = 4201

        var isTrackingActive = false
            private set
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        createNotificationChannel()
        initLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TRACKING -> startTracking()
            ACTION_STOP_TRACKING -> stopTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        if (isTrackingActive) return
        isTrackingActive = true

        startForeground(NOTIFICATION_ID, buildNotification("GPS & Barometer Active", "Tracking route and altitude..."))

        // Register hardware Barometer sensor
        pressureSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        // Request high-accuracy GPS updates via FusedLocationProviderClient
        requestLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateIntervalMillis(1000L)
            .setMinUpdateDistanceMeters(1.5f)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun initLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    handleNewLocation(location)
                }
            }
        }
    }

    private fun handleNewLocation(location: Location) {
        // Calculate incremental distance
        lastLocation?.let { prev ->
            val distMeters = prev.distanceTo(location)
            totalDistanceKm += (distMeters / 1000.0)
        }
        lastLocation = location

        // Calculate accurate barometric altitude using standard formula:
        // h = 44330 * (1 - (P / P0)^(1/5.255))
        val baroAltitude = BarometerHelper.calculateAltitudeFromPressure(smoothedPressureHpa)
        val gpsAltitude = location.altitude
        val speedKmh = location.speed * 3.6f

        // Log entry locally into Room Database
        serviceScope.launch {
            val point = GpsPointEntity(
                timestamp = System.currentTimeMillis(),
                latitude = location.latitude,
                longitude = location.longitude,
                altitudeBaro = baroAltitude,
                altitudeGps = gpsAltitude,
                pressureHpa = smoothedPressureHpa,
                accuracy = location.accuracy,
                speedKmh = speedKmh,
                distanceKm = totalDistanceKm
            )
            database.gpsPointDao().insert(point)
        }

        // Update notification status
        val title = "Elevation: ${baroAltitude.roundToInt()}m | ${smoothedPressureHpa.roundToInt()} hPa"
        val text = "Distance: %.2f km | Speed: %.1f km/h".format(totalDistanceKm, speedKmh)
        updateNotification(title, text)
    }

    // Barometer Sensor Listener
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_PRESSURE) {
            val rawPressure = event.values[0]
            latestPressureHpa = rawPressure

            // Apply low-pass filter to smooth micro-turbulences
            if (isFirstPressureReading) {
                smoothedPressureHpa = rawPressure
                isFirstPressureReading = false
            } else {
                smoothedPressureHpa = BarometerHelper.applyLowPassFilter(smoothedPressureHpa, rawPressure, alpha = 0.12f)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun stopTracking() {
        isTrackingActive = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "FreeTrack GPS & Barometer Tracker",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live elevation and distance tracking"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_navigation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(title: String, content: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(title, content))
    }
}