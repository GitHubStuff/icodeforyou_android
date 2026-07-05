// coins/data/CoinEntity.kt
// CatsCarsCoins — spec 24.2.3. Complete file.
package com.icodeforyou.catscarscoins.coins.data

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.icodeforyou.catscarscoins.coins.domain.Coin

/**
 * Room row for one BTC-USD price sample (spec: coins table
 * { id, amount_cents, recorded_at }). Room 3 — androidx.room3 imports.
 *
 * CREATE TABLE equivalent:
 * ```
 * CREATE TABLE coins (
 *     id           INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
 *     amount_cents INTEGER NOT NULL,
 *     recorded_at  INTEGER NOT NULL
 * )
 * ```
 *
 * Data-layer type only: everything above the repository sees [Coin].
 */
@Entity(tableName = "coins")
data class CoinEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = Coin.UNSAVED_ID,

    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,

    @ColumnInfo(name = "recorded_at")
    val recordedAt: Long,
)

fun CoinEntity.toDomain(): Coin = Coin(
    id = id,
    amountCents = amountCents,
    recordedAtEpochMillis = recordedAt,
)