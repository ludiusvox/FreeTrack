package com.freeapp.freetrack

import android.app.Application
import org.osmdroid.config.Configuration
import androidx.preference.PreferenceManager

class FreeTrackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize OpenStreetMap configuration (100% Free - no API key)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName
    }
}
