// cars/data/CarApi.kt
// CatsCarsCoins — spec 24.4.12-correction1. Complete file.
// Change from 24.4.12: BASE_URL companion added (carsModule reads it,
// mirroring CatApi.BASE_URL).
package com.icodeforyou.catscarscoins.cars.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit surface for the NHTSA vPIC `GetAllManufacturers` endpoint.
 *
 * The path is relative (no leading slash) so it resolves against
 * [BASE_URL]. The static query carries `format=json`; Retrofit appends
 * [ page], producing `vehicles/GetAllManufacturers?format=json&page=N`.
 *
 * vPIC is keyless: no auth headers, no interceptor on the client.
 * Results are paged at 100 rows per page.
 */
interface CarApi {

    @GET("vehicles/GetAllManufacturers?format=json")
    suspend fun getAllManufacturers(
        @Query("page") page: Int,
    ): ManufacturersResponseDto

    companion object {
        /** vPIC host — appears exactly once, right here. */
        const val BASE_URL = "https://vpic.nhtsa.dot.gov/api/"
    }
}