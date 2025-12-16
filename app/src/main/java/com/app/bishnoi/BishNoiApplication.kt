package com.app.bishnoi

import android.app.Application
import com.app.bishnoi.utils.PlacesHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BishNoiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ✅ Initialize Google Places
        PlacesHelper.initialize(this)
    }
}
