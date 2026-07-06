// cats/data/remote/CatApiRemoteSource.kt
// CatsCarsCoins — spec 24.3.14. Complete file.
package com.icodeforyou.catscarscoins.cats.data.remote

import com.icodeforyou.catscarscoins.cats.domain.Cat
import com.icodeforyou.catscarscoins.cats.domain.CatsRemoteSource

/**
 * Data-layer adapter for [CatsRemoteSource]: The Cat API page → domain
 * cats. All wire knowledge ends here. [toDomainOrNull]'s guard does the
 * filtering — a breedless entry (wire misbehavior despite has_breeds=1)
 * is dropped, never crashed on (pinned by 24.3.11).
 *
 * Failure contract (per the port): Retrofit's network/HTTP exceptions
 * propagate; the repository lets callers present them.
 */
class CatApiRemoteSource(
    private val catApi: CatApi,
) : CatsRemoteSource {

    override suspend fun fetchPage(limit: Int): List<Cat> =
        catApi.searchImages(limit = limit)
            .mapNotNull { dto -> dto.toDomainOrNull() }
}