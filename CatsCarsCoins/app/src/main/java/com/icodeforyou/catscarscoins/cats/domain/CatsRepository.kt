// cats/domain/CatsRepository.kt
// CatsCarsCoins — spec 24.3.2. Complete file.
package com.icodeforyou.catscarscoins.cats.domain

import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for stored cats (spec Phase 3: The Cat API + Room FTS4
 * search). Pure Kotlin — the data layer implements over Room + Retrofit;
 * consumers see only this interface (DIP).
 */
interface CatsRepository {

    /**
     * The stored cats matching [query], emitting on every change.
     *
     * - Blank [query] → every stored cat.
     * - Non-blank → FTS4 match over the four indexed breed fields
     *   (spec/24.3.1: name, origin, temperament, description).
     *
     * One observable, not a cats-val + search pair: the Cats screen is a
     * single list driven by a single search field.
     */
    fun cats(query: String): Flow<List<Cat>>

    /**
     * Fetches one page ([FETCH_LIMIT]) from The Cat API and upserts by
     * [Cat.id] — repeated refreshes never duplicate rows. Throws on
     * network or parse failure; presentation of failure (spec: read-only
     * toast, 750 ms) is the caller's decision, not this layer's.
     */
    suspend fun refresh()

    /** Reset App hook (spec §16) — each feature clears its own tables. */
    suspend fun clearAll()

    companion object {

        /** Page size per fetch (locked in the Cat API exploration). */
        const val FETCH_LIMIT: Int = 20
    }
}