// MyApp.kt
package com.icodeforyou.catscarscoins

import android.app.Application
import com.icodeforyou.catscarscoins.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application entry point (spec 0.5). Sole responsibility: start Koin with
 * the aggregated feature modules (SRP — no other app-wide state lives here).
 */
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApp)
            modules(appModules)
        }
    }
}