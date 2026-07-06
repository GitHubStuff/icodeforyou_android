// cars/data/CarApiRemoteSource.kt
// Main sources.
package com.icodeforyou.catscarscoins.cars.data

import com.icodeforyou.catscarscoins.cars.domain.CarsRemoteSource
import com.icodeforyou.catscarscoins.cars.domain.Manufacturer

/**
 * [CarsRemoteSource] backed by the vPIC [CarApi].
 *
 * Decodes one page of the `GetAllManufacturers` envelope and maps
 * rows into the domain, silently dropping rows that fail the
 * `toDomainOrNull` contract (null id, null/blank name).
 */
class CarApiRemoteSource(
    private val api: CarApi,
) : CarsRemoteSource {

    override suspend fun fetchPage(page: Int): List<Manufacturer> =
        api.getAllManufacturers(page)
            .results
            .mapNotNull { it.toDomainOrNull() }
}