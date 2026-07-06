// cars/domain/FakeCarsRepository.kt
// CatsCarsCoins — spec 24.4.15. Complete file. Test sources.
package com.icodeforyou.catscarscoins.cars.domain

import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Canonical in-memory fake of [CarsRepository] for unit tests. Test
 * source set, contract's package — the FakePreferencesRepository /
 * FakeCoinsRepository / FakeCatsRepository pattern.
 *
 * - [state] is exposed for direct scenario scripting.
 * - [manufacturers] records every query in [queriesSeen] (debounce
 *   assertions) and filters by contains over BOTH indexed fields
 *   (24.4.1: name, country) — a simple stand-in for FTS; the real
 *   MATCH behavior is owned by RoomCarsRepositoryTest.
 * - [refresh] counts calls and can fail once ([failNextRefresh]).
 */
class FakeCarsRepository(
    initial: List<Manufacturer> = emptyList(),
) : CarsRepository {

    val state = MutableStateFlow(initial)
    val queriesSeen = mutableListOf<String>()
    var refreshCount = 0
    var failNextRefresh = false

    /** When set, [refresh] suspends here until completed — lets a test
     *  hold a refresh in-flight to observe transient state. */
    var refreshGate: CompletableDeferred<Unit>? = null

    override fun manufacturers(query: String): Flow<List<Manufacturer>> {
        queriesSeen.add(query)
        return state.map { manufacturers ->
            if (query.isBlank()) {
                manufacturers
            } else {
                manufacturers.filter { manufacturer ->
                    manufacturer.name.contains(query, ignoreCase = true) ||
                            manufacturer.country.contains(query, ignoreCase = true)
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