// coins/ui/CoinToastPresenter.kt
// CatsCarsCoins — spec 24.2.49. Complete file.
package com.icodeforyou.catscarscoins.coins.ui

import androidx.compose.material3.Text
import com.icodeforyou.catscarscoins.coins.domain.CoinsRepository
import com.icodeforyou.catscarscoins.notifier.COIN_TOAST_MILLIS
import com.icodeforyou.catscarscoins.notifier.Notifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
                notifier.show(durationMillis = COIN_TOAST_MILLIS) {
                    Text(text = head.amountCents.toUsdDisplay())
                }
            }
        }
    }
}