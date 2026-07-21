package com.youme24.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class — Hilt entry point.
 * Declared in AndroidManifest.xml via android:name=".YouMeApplication".
 */
@HiltAndroidApp
class YouMeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Global init goes here if needed (e.g. Timber logging).
    }
}
