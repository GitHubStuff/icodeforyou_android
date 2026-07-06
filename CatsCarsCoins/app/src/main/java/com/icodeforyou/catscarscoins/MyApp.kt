// MyApp.kt
// CatsCarsCoins — spec 24.2.51. Complete file.
// Change from 24.2.26: CoinToastPresenter started at bootstrap — the
// fourth and final Phase 2 bootstrap duty.
package com.icodeforyou.catscarscoins

import android.app.Application
import com.icodeforyou.catscarscoins.coins.domain.CoinPollingEngine
import com.icodeforyou.catscarscoins.coins.ui.CoinToastPresenter
import com.icodeforyou.catscarscoins.di.appModules
import com.icodeforyou.catscarscoins.preferences.PreferencesSnapshot
import com.icodeforyou.catscarscoins.preferences.domain.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Application entry point (spec 0.5). Bootstrap responsibilities only:
 * start Koin, start the preferences snapshot feed (spec §17 accessors),
 * start the coin polling engine (spec §19 — app-lifetime polling; the
 * pause preference, not screen visibility, gates it), and start the coin
 * toast presenter (spec Notifier — toasts must fire on any destination).
 */
class MyApp : Application() {

    /**
     * Process-lifetime scope: SupervisorJob so a failed child never kills
     * the feed's siblings, Default dispatcher — nothing here touches the
     * main thread. Never cancelled; it dies with the process.
     */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@MyApp)
            modules(appModules)
        }
        startPreferencesSnapshotFeed()
        startCoinPolling()
        startCoinToasts()
    }

    /**
     * The single production writer of [PreferencesSnapshot]: every
     * repository emission — including the DEFAULTS emitted by an empty
     * store — lands in the snapshot, so accessor reads are current from
     * first launch on.
     */
    private fun startPreferencesSnapshotFeed() {
        val repository = get<PreferencesRepository>()
        applicationScope.launch {
            repository.preferences.collect { preferences ->
                PreferencesSnapshot.update(preferences)
            }
        }
    }

    /**
     * Polling runs for the app's lifetime on its own scope (supplied by
     * the Koin binding); whether any HTTP actually happens is governed
     * entirely by the pause preference — spec default is paused, so a
     * fresh install polls nothing until the user unpauses in Settings.
     */
    private fun startCoinPolling() {
        get<CoinPollingEngine>().start()
    }

    /**
     * Coin toasts are app-lifetime like the polling that feeds them: a
     * new sample toasts no matter which destination is showing — the
     * NotifierHost sits above NavDisplay in the locked 0.5 tree for
     * exactly this reason.
     */
    private fun startCoinToasts() {
        get<CoinToastPresenter>().start()
    }
}