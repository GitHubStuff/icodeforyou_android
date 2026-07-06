// coins/domain/CoinPollingEngine.kt
// CatsCarsCoins — spec 24.2.28. Complete file.
// Change from 24.2.23: implements CoinRefresher (ISP seam for UI refresh);
// declaration-only — refresh() gains override, behavior untouched.
package com.icodeforyou.catscarscoins.coins.domain

import com.icodeforyou.catscarscoins.preferences.domain.PreferencesRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * The Coins polling engine (spec §19, Decision Log). Behavior — every
 * clause pinned by 24.2.22:
 *
 * - Polls [CoinPriceSource] on the preference interval (5–30 s, live).
 * - Paused (the spec default) polls nothing; unpausing polls immediately.
 * - Consecutive-only dedup on cents via the recorder's literal
 *   [distinctUntilChanged] — placed OUTSIDE the restartable sampler so
 *   dedup state survives preference changes.
 * - [refresh]: polls now (even while paused), resets the next tick, never
 *   touches the pause setting.
 * - A failed fetch skips its tick; the loop survives.
 */
class CoinPollingEngine(
    private val priceSource: CoinPriceSource,
    private val coinsRepository: CoinsRepository,
    private val preferencesRepository: PreferencesRepository,
    private val scope: CoroutineScope,
    private val recordedAtProvider: () -> Long,
) : CoinRefresher {

    private val fetchedAmounts = MutableSharedFlow<Long>()
    private val refreshSignal = Channel<Unit>(Channel.CONFLATED)
    private var started = false

    /** Idempotent; launches the recorder and the sampler into [scope]. */
    fun start() {
        if (started) return
        started = true
        scope.launch { recordDedupedAmounts() }
        scope.launch { samplePerPreferences() }
    }

    /**
     * Spec §19 Refresh: cancel the in-flight wait, poll now, reset the
     * next poll. Non-suspending (UI-callable); the conflated channel means
     * a refresh is never lost and never queues more than one poll.
     */
    override fun refresh() {
        refreshSignal.trySend(Unit)
    }

    /** The Decision Log's dedup, verbatim: consecutive-only on cents. */
    private suspend fun recordDedupedAmounts() {
        fetchedAmounts
            .distinctUntilChanged()
            .collect { amountCents ->
                coinsRepository.record(amountCents, recordedAtProvider())
            }
    }

    /**
     * Restarts on every (interval, paused) change — and only on those
     * (theme or haptics writes cannot restart the loop). Each restart in
     * the unpaused branch polls immediately, so unpausing and interval
     * changes both produce an immediate sample (pinned behavior; dedup
     * makes the extra poll harmless).
     */
    private suspend fun samplePerPreferences() {
        preferencesRepository.preferences
            .map { preferences ->
                preferences.pollingIntervalSeconds to preferences.pollingPaused
            }
            .distinctUntilChanged()
            .collectLatest { (intervalSeconds, paused) ->
                if (paused) {
                    pollOnlyOnRefresh()
                } else {
                    pollOnInterval(intervalSeconds.seconds)
                }
            }
    }

    private suspend fun pollOnlyOnRefresh() {
        while (true) {
            refreshSignal.receive()
            pollOnce()
        }
    }

    private suspend fun pollOnInterval(interval: Duration) {
        while (true) {
            pollOnce()
            awaitTickOrRefresh(interval)
        }
    }

    /**
     * Waits one interval OR returns early on [refresh] — which is exactly
     * "cancel the in-flight wait and reset the next poll."
     */
    private suspend fun awaitTickOrRefresh(interval: Duration) {
        withTimeoutOrNull(interval) {
            refreshSignal.receive()
        }
    }

    /**
     * One fetch. Failure skips the tick ([CoinPriceSource] contract);
     * cancellation is rethrown — swallowing it would keep a cancelled
     * loop half-alive.
     */
    private suspend fun pollOnce() {
        val amountCents = try {
            priceSource.currentAmountCents()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            return
        }
        fetchedAmounts.emit(amountCents)
    }
}