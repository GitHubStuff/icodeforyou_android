// coins/domain/CoinPriceSource.kt
// CatsCarsCoins — spec 24.2.21. Complete file.
package com.icodeforyou.catscarscoins.coins.domain

/**
 * Domain port for "what does BTC-USD cost right now, in exact cents."
 *
 * The polling engine depends on this, never on Retrofit or wire types —
 * which is what lets the engine's tests (next step) drive it with a hand
 * fake and virtual time. The data-layer adapter (Coinbase API → DTO →
 * toUsdCents) implements it.
 *
 * Failure contract: implementations throw on network or parse failure;
 * the engine treats a thrown tick as "no sample this tick" and keeps
 * polling.
 */
interface CoinPriceSource {

    suspend fun currentAmountCents(): Long
}