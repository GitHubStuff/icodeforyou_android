// coins/domain/Coin.kt
// CatsCarsCoins — spec 24.2.1. Complete file.
package com.icodeforyou.catscarscoins.coins.domain

/**
 * One recorded BTC-USD price sample (spec: coins table
 * { id, amount_cents, recorded_at }).
 *
 * Money is integer minor units — [amountCents] is a Long, never a Double
 * (spec Decision Log). $67,123.45 is 6_712_345 cents; equality on this
 * field is exact, which is what the consecutive-only dedup compares.
 *
 * Pure domain model: no Room, no Android. The data layer maps this to and
 * from the Room entity.
 */
data class Coin(
    val id: Long,
    val amountCents: Long,
    val recordedAtEpochMillis: Long,
) {
    companion object {

        /** Room assigns the real id on insert (spec PK convention: `id`). */
        const val UNSAVED_ID: Long = 0L

        /**
         * FIFO row cap (spec: 500). On overflow the oldest rows go; the
         * data layer's trim enforces it after every insert.
         */
        const val ROW_CAP: Int = 500
    }
}