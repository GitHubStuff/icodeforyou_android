// cats/data/remote/CatDto.kt
// CatsCarsCoins — spec 24.3.12. Complete file.
package com.icodeforyou.catscarscoins.cats.data.remote

import com.icodeforyou.catscarscoins.cats.domain.Breed
import com.icodeforyou.catscarscoins.cats.domain.Cat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The Cat API wire shape for GET /v1/images/search (spec Phase 3):
 * a JSON array of image objects, each carrying its breeds inline —
 * one call serves list and detail. DTOs mirror the wire verbatim;
 * optional breed fields default to "" so an incomplete record never
 * fails deserialization (shapes locked in the Cat API exploration).
 */
@Serializable
data class CatImageDto(

    @SerialName("id")
    val id: String,

    @SerialName("url")
    val url: String,

    @SerialName("breeds")
    val breeds: List<BreedDto> = emptyList(),
)

@Serializable
data class BreedDto(

    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("origin")
    val origin: String = "",

    @SerialName("temperament")
    val temperament: String = "",

    @SerialName("life_span")
    val lifeSpan: String = "",

    @SerialName("description")
    val description: String = "",
)

/**
 * Wire → domain, guarded (pinned by 24.3.11): [Cat.breed] is
 * non-nullable, and has_breeds=1 is a request parameter — not a wire
 * guarantee. A breedless entry maps to null and the adapter drops it;
 * the first breed wins when several arrive.
 */
fun CatImageDto.toDomainOrNull(): Cat? {
    val firstBreed = breeds.firstOrNull() ?: return null
    return Cat(
        id = id,
        imageUrl = url,
        breed = Breed(
            id = firstBreed.id,
            name = firstBreed.name,
            origin = firstBreed.origin,
            temperament = firstBreed.temperament,
            lifeSpan = firstBreed.lifeSpan,
            description = firstBreed.description,
        ),
    )
}