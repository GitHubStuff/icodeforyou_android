// coins/ui/CoinToastPresenter.kt
// CatsCarsCoins — spec 24.2.49. Complete file.
// Change: coin toast text enlarged and money-green; 2dp purple border
// on the toast surface (per-notification border, other toasts untouched).
package com.icodeforyou.catscarscoins.coins.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.icodeforyou.catscarscoins.coins.domain.CoinsRepository
import com.icodeforyou.catscarscoins.notifier.COIN_TOAST_MILLIS
import com.icodeforyou.catscarscoins.notifier.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Coin toast text size in sp — larger than the default body text. */
private val COIN_TOAST_FONT_SIZE = 22.sp

/** Money green — readable on both light and dark toast surfaces. */
private val COIN_TOAST_COLOR = Color(0xFF2E9E4F)

/** Coin toast outline: 2dp, the app's purple (matches the Cats spinner). */
private val COIN_TOAST_BORDER_WIDTH = 2.dp
private val COIN_TOAST_BORDER_COLOR = Color(0xFF9C27B0)

/**
 * Bridges the coins table to the Notifier (spec: coin toast, 1250 ms):
 * every NEWLY recorded sample toasts its price; nothing else does.
 * Behavior pinned by 24.2.48:
 *
 * - The first emission after start initializes silently — a relaunch
 *   never toasts history (which is why a bare `drop(1)` is wrong: it
 *   would also eat the first real sample into an empty table).
 * - A list that empties resets the last-seen id — SQLite reuses rowids
 *   after a full clear, so the first sample after Reset App must not be
 *   compared against a pre-Reset id.
 * - A same-head re-emission (trim reshaping the tail) is not news.
 */
class CoinToastPresenter(
    private val coinsRepository: CoinsRepository,
    private val notifier: Notifier,
    private val scope: CoroutineScope,
) {

    private var started = false

    /** Idempotent; launches the observer into [scope]. */
    fun start() {
        if (started) return
        started = true
        scope.launch { toastNewHeads() }
    }

    private suspend fun toastNewHeads() {
        var initialized = false
        var lastSeenHeadId: Long? = null

        coinsRepository.coins.collect { samples ->
            val head = samples.firstOrNull()

            if (!initialized) {
                initialized = true
                lastSeenHeadId = head?.id
                return@collect
            }

            if (head == null) {
                lastSeenHeadId = null
                return@collect
            }

            if (head.id != lastSeenHeadId) {
                lastSeenHeadId = head.id
                notifier.show(
                    durationMillis = COIN_TOAST_MILLIS,
                    border = BorderStroke(COIN_TOAST_BORDER_WIDTH, COIN_TOAST_BORDER_COLOR),
                ) {
                    Text(
                        text = head.amountCents.toUsdDisplay(),
                        fontSize = COIN_TOAST_FONT_SIZE,
                        color = COIN_TOAST_COLOR,
                    )
                }
            }
        }
    }
}