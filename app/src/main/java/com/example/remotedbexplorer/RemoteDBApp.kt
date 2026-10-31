package com.example.remotedbexplorer

import android.app.Application
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point annotated for Hilt dependency injection.
 * Extends [MultiDexApplication] because the JDBC driver libraries
 * push the method count beyond the single-dex limit.
 */
@HiltAndroidApp
class RemoteDBApp : MultiDexApplication()
