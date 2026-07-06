// cats/domain/FakeCatsRepository.kt
// CatsCarsCoins — spec 24.3.36. Complete file. Test sources.
// Change from 24.3.27: optional refreshGate — when set, refresh() suspends
// on it, letting tests observe an in-flight state deterministically.
// Additive; default null = prior behavior, existing users untouched.
package com.icodeforyou.catscarscoins.cats.domain

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Canonical in-memory fake of [CatsRepository] for unit tests. Test
 * source set, contract's package — the FakePreferencesRepository /
 * FakeCoinsRepository pattern; promoted at its second user
 * (CatDetailViewModel's tests) from CatsViewModelTest's nested fake.
 *
 * - [state] is exposed for direct scenario scripting.
 * - [cats] records every query in [queriesSeen] (debounce assertions) and
 *   filters by breed-name contains — a simple stand-in for FTS; the real
 *   MATCH behavior is owned by RoomCatsRepositoryTest.
 * - [refresh] counts calls and can fail once ([failNextRefresh]).
 */
class FakeCatsRepository(
    initial: List<Cat> = emptyList(),
) : CatsRepository {

    val state = MutableStateFlow(initial)
    val queriesSeen = mutableListOf<String>()
    var refreshCount = 0
    var failNextRefresh = false

    /** When set, [refresh] suspends here until completed — lets a test
     *  hold a refresh in-flight to observe transient state. */
    var refreshGate: CompletableDeferred<Unit>? = null

    override fun cats(query: String): Flow<List<Cat>> {
        queriesSeen.add(query)
        return state.map { cats ->
            if (query.isBlank()) {
                cats
            } else {
                cats.filter { cat ->
                    cat.breed.name.contains(query, ignoreCase = true)
                }
            }
        }
    }

    override suspend fun refresh() {
        refreshCount++
        refreshGate?.await()
        if (failNextRefresh) {
            failNextRefresh = false
            throw IOException("fake network failure")
        }
    }

    override suspend fun clearAll() {
        state.value = emptyList()
    }
}