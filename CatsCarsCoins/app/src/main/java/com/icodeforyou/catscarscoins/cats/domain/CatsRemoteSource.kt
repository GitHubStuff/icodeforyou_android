// cats/domain/CatsRemoteSource.kt
// CatsCarsCoins — spec 24.3.8. Complete file.
package com.icodeforyou.catscarscoins.cats.domain

/**
 * Domain port for "fetch a page of cats with breeds" (spec Phase 3:
 * The Cat API, has_breeds=1). The repository depends on this, never on
 * Retrofit or wire types — its instrumented tests run real Room + FTS
 * with this port hand-faked, exactly the CoinPriceSource pattern
 * (24.2.21).
 *
 * Failure contract: implementations throw on network or parse failure;
 * the repository lets it propagate (24.3.2 — failure presentation is the
 * caller's).
 */
interface CatsRemoteSource {

    suspend fun fetchPage(limit: Int): List<Cat>
}