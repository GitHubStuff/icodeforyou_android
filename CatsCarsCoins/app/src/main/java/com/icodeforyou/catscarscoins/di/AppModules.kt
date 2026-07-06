// di/AppModules.kt
// CatsCarsCoins — spec 24.4.14. Complete file.
// Change from 24.3.17: carsModule wired in (Phase 4).
package com.icodeforyou.catscarscoins.di

import com.icodeforyou.catscarscoins.cars.di.carsModule
import com.icodeforyou.catscarscoins.cats.di.catsModule
import com.icodeforyou.catscarscoins.coins.di.coinsModule
import com.icodeforyou.catscarscoins.db.databaseModule
import com.icodeforyou.catscarscoins.net.networkModule
import com.icodeforyou.catscarscoins.notifier.notifierModule
import com.icodeforyou.catscarscoins.preferences.di.preferencesModule
import org.koin.core.module.Module

/**
 * Aggregates every feature's Koin module (spec §3: one module per feature,
 * AppModules aggregates). Each feature phase appends its module here — the
 * only DI file MyApp ever needs to know about.
 */
val appModules: List<Module> = listOf(
    preferencesModule,
    databaseModule,
    networkModule,
    coinsModule,
    catsModule,
    carsModule,
    notifierModule,
)