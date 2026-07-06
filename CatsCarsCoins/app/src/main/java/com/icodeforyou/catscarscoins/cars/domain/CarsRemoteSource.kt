// cars/domain/CarsRemoteSource.kt
// CatsCarsCoins — spec 24.4.7. Complete file.
package com.icodeforyou.catscarscoins.cars.domain

/**
 * Domain port for "fetch a page of manufacturers" (spec Phase 4: NHTSA
 * vPIC GetAllManufacturers). The repository depends on this, never on
 * Retrofit or wire types — its instrumented tests run real Room + FTS
 * with this port hand-faked, exactly the CatsRemoteSource pattern
 * (24.3.8).
 *
 * Failure contract: implementations throw on network or parse failure;
 * the repository lets it propagate (24.4.2 — failure presentation is the
 * caller's).
 */
interface CarsRemoteSource {

    suspend fun fetchPage(page: Int): List<Manufacturer>
}