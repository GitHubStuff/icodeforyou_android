// coins/ui/UsdDisplay.kt
// CatsCarsCoins — spec 24.2.44. Complete file.
package com.icodeforyou.catscarscoins.coins.ui

import java.util.Locale

private const val CENTS_PER_DOLLAR = 100L

/**
 * Exact integer rendering of cents as dollars — the never-Double rule
 * extends to display: 6_712_345 → "$67,123.45". Promoted from a private
 * CoinsScreen helper at its second user (the coin toast, spec: 1250 ms),
 * per the rule of two.
 *
 * Locale.US pins grouping and decimal separators — a reference blueprint
 * renders deterministically regardless of device locale.
 */
fun Long.toUsdDisplay(): String {
    val dollars = this / CENTS_PER_DOLLAR
    val cents = this % CENTS_PER_DOLLAR
    return "$%,d.%02d".format(Locale.US, dollars, cents)
}