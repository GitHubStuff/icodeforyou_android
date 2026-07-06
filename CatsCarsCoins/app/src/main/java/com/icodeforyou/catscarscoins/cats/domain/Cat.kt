// cats/domain/Cat.kt
// CatsCarsCoins — spec 24.3.1. Complete file.
package com.icodeforyou.catscarscoins.cats.domain

/**
 * One cat from The Cat API (spec Phase 3): an image plus its breed.
 * The fetch uses has_breeds=1, so [breed] is guaranteed present and
 * therefore non-nullable — an API contract encoded as a type.
 *
 * [id] is the remote image id (String — The Cat API's key, not a Room
 * rowid). Pure domain: no Room, no Coil, no wire knowledge.
 */
data class Cat(
    val id: String,
    val imageUrl: String,
    val breed: Breed,
)

/**
 * Breed facts, lean per YAGNI — exactly what the detail screen renders
 * and the search indexes. The FTS4 searchable text (spec: Cats search)
 * is drawn from [name], [origin], [temperament], and [description]; the
 * data layer's FTS entity mirrors those four.
 */
data class Breed(
    val id: String,
    val name: String,
    val origin: String,
    val temperament: String,
    val lifeSpan: String,
    val description: String,
)