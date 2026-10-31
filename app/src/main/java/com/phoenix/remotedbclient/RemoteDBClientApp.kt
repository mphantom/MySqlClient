package com.phoenix.remotedbclient

import android.app.Application
import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point annotated for Hilt dependency injection.
 * Extends [MultiDexApplication] because the JDBC driver libraries
 * push the method count beyond the single-dex limit.
 *
 * [改动前] 类名为 RemoteDBApp，包名为 com.example.remotedbexplorer
 * [改动后] 类名改为 RemoteDBClientApp，包名改为 com.phoenix.remotedbclient，继承关系与逻辑不变
 * [原因] 统一应用命名，并与 AndroidManifest 中的 .RemoteDBClientApp 对应
 */
@HiltAndroidApp
class RemoteDBClientApp : MultiDexApplication()
