package com.freeapp.freetrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity schema for GPS and Barometer breadcrumb track points.
 */
@Entity(tableName = "gps_points")
data class GpsPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val timestamp: Long,          // Epoch millis
    val latitude: Double,         // WGS84 coordinates from FusedLocationProvider
    val longitude: Double,
    val altitudeBaro: Double,     // Calculated from hardware pressure sensor (meters)
    val altitudeGps: Double,      // Native GPS altitude from satellites (meters)
    val pressureHpa: Float,       // Atmospheric pressure in hPa/millibar
    val accuracy: Float,          // GPS horizontal accuracy in meters
    val speedKmh: Float,          // Velocity in km/h
    val distanceKm: Double        // Cumulative distance traversed
)