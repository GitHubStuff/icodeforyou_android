// cars/domain/Manufacturer.kt
// CatsCarsCoins — spec 24.4.1. Complete file.
package com.icodeforyou.catscarscoins.cars.domain

/**
 * One vehicle manufacturer from the NHTSA vPIC API
 * (GetAllManufacturers). Pure domain — no Room, no wire knowledge.
 *
 * [id] is vPIC's Mfr_ID (a stable integer key — unlike The Cat API's
 * String image id, vPIC keys are numeric). [country] can be blank when
 * vPIC omits it; the mapper defaults it rather than carrying a nullable
 * into the domain.
 *
 * The FTS-searchable text (spec Phase 4: Cars search) is [name] and
 * [country]; the data layer's FTS entity mirrors those two.
 */
data class Manufacturer(
    val id: Int,
    val name: String,
    val country: String,
)