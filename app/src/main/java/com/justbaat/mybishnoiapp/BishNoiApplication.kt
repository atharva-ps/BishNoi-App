package com.justbaat.mybishnoiapp

import android.app.Application
import com.justbaat.mybishnoiapp.utils.PlacesHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BishNoiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ✅ Initialize Google Places
        PlacesHelper.initialize(this)
    }
}
