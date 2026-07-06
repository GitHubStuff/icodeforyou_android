// cars/data/CarDto.kt
// Main sources.
package com.icodeforyou.catscarscoins.cars.data

import com.icodeforyou.catscarscoins.cars.domain.Manufacturer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val NO_COUNTRY = ""

/**
 * Envelope for vPIC `vehicles/GetAllManufacturers?format=json&page=N`.
 *
 * Only [results] is declared; the remaining envelope keys
 * (`Count`, `Message`, `SearchCriteria`) are absorbed by the shared
 * `Json { ignoreUnknownKeys = true }` configuration.
 */
@Serializable
data class ManufacturersResponseDto(
    @SerialName("Results") val results: List<CarDto>,
)

/**
 * One manufacturer row from the vPIC dataset.
 *
 * All fields are nullable with `null` defaults: vPIC ships explicit
 * nulls (notably `Country`) and rows may omit keys entirely. Extra
 * keys (`Mfr_CommonName`, `VehicleTypes[]`, ...) are never declared
 * here and are dropped during decoding.
 */
@Serializable
data class CarDto(
    @SerialName("Mfr_ID") val mfrId: Int? = null,
    @SerialName("Mfr_Name") val mfrName: String? = null,
    @SerialName("Country") val country: String? = null,
)

/**
 * Maps a wire row to the domain, or rejects it.
 *
 * Rejection rules:
 * - missing [CarDto.mfrId] -> null
 * - missing or blank [CarDto.mfrName] -> null
 *
 * Tolerance rule:
 * - missing or blank [CarDto.country] -> [NO_COUNTRY] (blank default)
 */
fun CarDto.toDomainOrNull(): Manufacturer? {
    val id = mfrId ?: return null
    val name = mfrName?.takeUnless { it.isBlank() } ?: return null
    val resolvedCountry = country?.takeUnless { it.isBlank() } ?: NO_COUNTRY

    return Manufacturer(
        id = id,
        name = name,
        country = resolvedCountry,
    )
}