// cats/data/remote/CatApi.kt
// CatsCarsCoins — spec 24.3.13. Complete file.
package com.icodeforyou.catscarscoins.cats.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Static query baked into the path: has_breeds=1 is a constant of the
 * contract (spec: one call serves list and detail; the domain's
 * non-nullable breed depends on requesting it) — not a parameter anyone
 * can pass 0 to.
 */
private const val IMAGES_SEARCH_PATH = "v1/images/search?has_breeds=1"

/**
 * The Cat API (spec Phase 3). One endpoint; the bare-array response maps
 * straight to List<CatImageDto> — no envelope.
 *
 * Auth deliberately absent from the signature: the x-api-key header is
 * wiring, applied by an OkHttp interceptor on the cats Retrofit's client
 * (Koin module) — BuildConfig.CAT_API_KEY appears exactly once, there.
 */
interface CatApi {

    @GET(IMAGES_SEARCH_PATH)
    suspend fun searchImages(
        @Query("limit") limit: Int,
    ): List<CatImageDto>

    companion object {

        /** Consumed by the cats Retrofit builder in the Koin module. */
        const val BASE_URL = "https://api.thecatapi.com/"
    }
}