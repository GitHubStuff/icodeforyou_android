// coins/domain/CoinRefresher.kt
// CatsCarsCoins — spec 24.2.27. Complete file.
package com.icodeforyou.catscarscoins.coins.domain

/**
 * The single capability UI needs from the polling engine (spec §19
 * Refresh: cancel the in-flight wait, poll now, reset the next poll,
 * never touch pause). ISP: consumers of refresh must not also see
 * start() — CoinPollingEngine implements this; ViewModels depend on it.
 */
interface CoinRefresher {

    fun refresh()
}