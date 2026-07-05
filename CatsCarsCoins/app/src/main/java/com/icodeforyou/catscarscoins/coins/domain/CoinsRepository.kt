// coins/domain/CoinsRepository.kt
// CatsCarsCoins — spec 24.2.2. Complete file.
package com.icodeforyou.catscarscoins.coins.domain

import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for stored BTC-USD price samples (spec: coins table,
 * FIFO cap 500). Pure Kotlin — the data layer implements it over Room;
 * consumers depend only on this interface (DIP).
 *
 * Scope note: **no dedup here.** The spec's Decision Log places the
 * consecutive-only dedup in the polling pipeline
 * (`distinctUntilChanged` on amountCents), so this contract is storage
 * only — a second dedup layer would be a second source of truth for the
 * feature's core rule.
 */
interface CoinsRepository {

    /**
     * All stored samples, newest first ([Coin.recordedAtEpochMillis]
     * descending), emitting on every change.
     */
    val coins: Flow<List<Coin>>

    /**
     * Appends one sample. Implementations MUST trim oldest-first to
     * [Coin.ROW_CAP] rows after the insert (spec: FIFO 500) — the cap is
     * a persistence invariant, not a caller courtesy.
     */
    suspend fun record(amountCents: Long, recordedAtEpochMillis: Long)

    /**
     * Deletes every sample. Invoked by ResetAppUseCase (spec §16: each
     * feature repository clears its own tables) and by nothing else with
     * confirmation — spec-wide rule: no confirm on destructive actions.
     */
    suspend fun clearAll()
}