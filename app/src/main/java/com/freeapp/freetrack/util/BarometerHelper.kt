package com.freeapp.freetrack.util

import kotlin.math.pow

object BarometerHelper {

    /**
     * Standard sea level atmospheric pressure in hPa / millibar (QNH default).
     */
    const val STANDARD_SEA_LEVEL_PRESSURE = 1013.25f

    /**
     * Calculates elevation in meters from measured barometric pressure:
     * h = 44330 * (1 - (P / P0) ^ (1 / 5.255))
     *
     * @param pressureHpa Current ambient pressure in hectopascals
     * @param seaLevelPressure Calibrated sea-level pressure (defaults to 1013.25)
     */
    fun calculateAltitudeFromPressure(
        pressureHpa: Float,
        seaLevelPressure: Float = STANDARD_SEA_LEVEL_PRESSURE
    ): Double {
        if (pressureHpa <= 0f) return 0.0
        val ratio = (pressureHpa / seaLevelPressure).toDouble()
        return 44330.0 * (1.0 - ratio.pow(1.0 / 5.255))
    }

    /**
     * Calculates expected pressure in hPa from a known elevation.
     */
    fun calculatePressureFromAltitude(
        altitudeMeters: Double,
        seaLevelPressure: Float = STANDARD_SEA_LEVEL_PRESSURE
    ): Float {
        val ratio = 1.0 - (altitudeMeters / 44330.0)
        return (seaLevelPressure * ratio.pow(5.255)).toFloat()
    }

    /**
     * Calibrate local sea-level pressure (QNH) using known current elevation.
     */
    fun calibrateQnh(
        currentPressureHpa: Float,
        knownAltitudeMeters: Double
    ): Float {
        val denominator = (1.0 - (knownAltitudeMeters / 44330.0)).pow(5.255)
        return (currentPressureHpa / denominator).toFloat()
    }

    /**
     * Low-pass filter for eliminating micro-sensor noise and hand jitter.
     */
    fun applyLowPassFilter(
        currentSmoothed: Float,
        newRaw: Float,
        alpha: Float = 0.15f
    ): Float {
        return currentSmoothed + alpha * (newRaw - currentSmoothed)
    }
}