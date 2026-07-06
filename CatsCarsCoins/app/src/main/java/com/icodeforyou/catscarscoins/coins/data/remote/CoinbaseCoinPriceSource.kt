// coins/data/remote/CoinbaseCoinPriceSource.kt
// CatsCarsCoins — spec 24.2.24. Complete file.
package com.icodeforyou.catscarscoins.coins.data.remote

import com.icodeforyou.catscarscoins.coins.domain.CoinPriceSource

/**
 * Data-layer adapter for [CoinPriceSource]: Coinbase spot price →
 * envelope unwrap → exact cents. All wire knowledge (DTO shape, string
 * amounts) ends here; the engine sees only a Long.
 *
 * Failure contract (per the port's KDoc): Retrofit throws on
 * network/HTTP failure and [toUsdCents] throws on a malformed amount —
 * both propagate, and the engine skips that tick.
 */
class CoinbaseCoinPriceSource(
    private val coinbaseApi: CoinbaseApi,
) : CoinPriceSource {

    override suspend fun currentAmountCents(): Long =
        coinbaseApi.getSpotPrice().data.amount.toUsdCents()
}