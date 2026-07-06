// coins/data/remote/CoinbaseApi.kt
// CatsCarsCoins — spec 24.2.17. Complete file.
package com.icodeforyou.catscarscoins.coins.data.remote

import retrofit2.http.GET

/** Relative endpoint path — compile-time const so @GET can reference it. */
private const val SPOT_PRICE_PATH = "v2/prices/BTC-USD/spot"

/**
 * Coinbase public spot-price API (spec §5: Retrofit; no auth, no API key).
 * One endpoint, BTC-USD fixed — the spec polls exactly one pair, so the
 * pair is a constant, not a parameter (YAGNI).
 *
 * suspend per Retrofit's coroutine support; the kotlinx-serialization
 * converter (already wired) decodes the envelope DTO.
 */
interface CoinbaseApi {

    @GET(SPOT_PRICE_PATH)
    suspend fun getSpotPrice(): CoinbaseSpotPriceResponseDto

    companion object {

        /** Consumed by the network Koin module's Retrofit builder. */
        const val BASE_URL = "https://api.coinbase.com/"
    }
}