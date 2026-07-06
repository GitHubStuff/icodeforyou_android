// coins/data/remote/CoinbaseSpotPriceDto.kt
// CatsCarsCoins — spec 24.2.14. Complete file.
package com.icodeforyou.catscarscoins.coins.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Coinbase spot-price wire shape (GET /v2/prices/BTC-USD/spot):
 * ```
 * { "data": { "amount": "4343.17", "base": "BTC", "currency": "USD" } }
 * ```
 * Every payload field is a String on the wire and stays a String here —
 * DTOs mirror the wire verbatim, no interpretation. The amount's
 * String → Long-cents conversion is the mapper's job (next steps), never
 * the DTO's, and never via Double (spec Decision Log: exact cents).
 */
@Serializable
data class CoinbaseSpotPriceResponseDto(

    @SerialName("data")
    val data: CoinbaseSpotPriceDto,
)

@Serializable
data class CoinbaseSpotPriceDto(

    @SerialName("amount")
    val amount: String,

    @SerialName("base")
    val base: String,

    @SerialName("currency")
    val currency: String,
)