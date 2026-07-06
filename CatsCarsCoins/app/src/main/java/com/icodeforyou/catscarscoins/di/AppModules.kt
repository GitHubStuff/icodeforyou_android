// di/AppModules.kt
// CatsCarsCoins — spec 24.5.4. Complete file.
// Change from 24.4.14: Added dbViewerModule to appModules tracking array.
package com.icodeforyou.catscarscoins.di

import com.icodeforyou.catscarscoins.cats.di.catsModule
import com.icodeforyou.catscarscoins.coins.di.coinsModule
import com.icodeforyou.catscarscoins.cars.di.carsModule
import com.icodeforyou.catscarscoins.db.databaseModule
import com.icodeforyou.catscarscoins.net.networkModule
import com.icodeforyou.catscarscoins.notifier.notifierModule
import com.icodeforyou.catscarscoins.preferences.di.preferencesModule
import org.koin.core.module.Module

/**
 * The master dependency declaration token tracking collection array.
 * Controls strict component construction sequences over feature boundaries.
 */
val appModules: List<Module> = listOf(
    preferencesModule,
    networkModule,
    databaseModule,
    notifierModule,
    coinsModule,
    catsModule,
    carsModule,
    dbViewerModule
)