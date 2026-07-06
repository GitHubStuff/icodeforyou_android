// db/DatabaseModule.kt
// CatsCarsCoins — spec 24.4.14. Complete file.
// Change from 24.3.15: CarDao single added (Phase 4).
package com.icodeforyou.catscarscoins.db

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.icodeforyou.catscarscoins.cars.data.CarDao
import com.icodeforyou.catscarscoins.cats.data.CatDao
import com.icodeforyou.catscarscoins.coins.data.CoinDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** On-disk database file name — appears exactly once. */
private const val DATABASE_NAME = "cats_cars_coins.db"

/**
 * Database wiring (spec §5: one CatsCarsDatabase). Room 3 requires an
 * explicit SQLiteDriver on the builder; BundledSQLiteDriver gives a
 * deterministic SQLite version on every device (24.2.6 decision). The
 * builder's coroutine context is left at its default (Dispatchers.IO).
 *
 * DAOs are provided here so features inject exactly the DAO they own —
 * nothing outside this file ever asks Koin for the whole database.
 */
val databaseModule = module {

    single<CatsCarsDatabase> {
        val context = androidContext().applicationContext
        Room.databaseBuilder<CatsCarsDatabase>(
            context = context,
            name = context.getDatabasePath(DATABASE_NAME).absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    single<CoinDao> {
        get<CatsCarsDatabase>().coinDao()
    }

    single<CatDao> {
        get<CatsCarsDatabase>().catDao()
    }

    single<CarDao> {
        get<CatsCarsDatabase>().carDao()
    }
}