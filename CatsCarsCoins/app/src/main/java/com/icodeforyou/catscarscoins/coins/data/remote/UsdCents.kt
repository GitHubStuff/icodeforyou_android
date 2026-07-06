// coins/data/remote/UsdCents.kt
// CatsCarsCoins — spec 24.2.16. Complete file.
package com.icodeforyou.catscarscoins.coins.data.remote

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Parses a Coinbase USD amount string to exact integer cents
 * ("4343.17" → 434317). The single wire→domain conversion point for money.
 *
 * Decimal-exact by construction (spec Decision Log: never Double):
 * `toDouble() * 100` misrounds float-hostile values by a whole cent, which
 * would silently corrupt the consecutive-only dedup comparing exact cents.
 *
 * Over-precise input rounds HALF_UP (pinned by 24.2.15); malformed input
 * throws [NumberFormatException] — a bad wire amount never becomes a
 * stored sample.
 */
fun String.toUsdCents(): Long =
    BigDecimal(this)
        .movePointRight(2)
        .setScale(0, RoundingMode.HALF_UP)
        .longValueExact()