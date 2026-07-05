// db/CatsCarsDatabase.kt
// CatsCarsCoins — spec 24.2.5. Complete file.
package com.icodeforyou.catscarscoins.db

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.icodeforyou.catscarscoins.coins.data.CoinDao
import com.icodeforyou.catscarscoins.coins.data.CoinEntity

/**
 * The one app database (spec §5: single CatsCarsDatabase). Version 1
 * carries only the coins table; Cats (FTS4) and Cars entities join in
 * their phases with schema version bumps — the exported schemas/ history
 * (spec 0.7) records each step.
 *
 * Room 3: androidx.room3 package; instantiation happens in the Koin module
 * via Room.databaseBuilder with an explicit SQLiteDriver (required in
 * Room 3 — no driver, no database).
 */
@Database(
    entities = [CoinEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class CatsCarsDatabase : RoomDatabase() {

    abstract fun coinDao(): CoinDao
}