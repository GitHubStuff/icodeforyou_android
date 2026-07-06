// coins/domain/FakeCoinsRepository.kt
// CatsCarsCoins — spec 24.2.46. Complete file. Test sources.
package com.icodeforyou.catscarscoins.coins.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Canonical in-memory fake of [CoinsRepository] for unit tests. Lives in
 * the test source set, same package as the contract (the
 * FakePreferencesRepository pattern). Behaves like the real thing at the
 * contract level: newest-first, [record] prepends with a fresh
 * auto-incremented id, [clearAll] empties.
 *
 * [state] is exposed so tests can script arbitrary scenarios directly —
 * trim-style re-emissions, emptying, and post-Reset id reuse — without
 * bending the honest write path.
 */
class FakeCoinsRepository(
    initial: List<Coin> = emptyList(),
) : CoinsRepository {

    val state = MutableStateFlow(initial)

    private var nextId: Long =
        (initial.maxOfOrNull { it.id } ?: Coin.UNSAVED_ID) + 1L

    override val coins: Flow<List<Coin>> = state

    override suspend fun record(amountCents: Long, recordedAtEpochMillis: Long) {
        val recorded = Coin(
            id = nextId++,
            amountCents = amountCents,
            recordedAtEpochMillis = recordedAtEpochMillis,
        )
        state.update { current -> listOf(recorded) + current }
    }

    override suspend fun clearAll() {
        state.value = emptyList()
    }
}