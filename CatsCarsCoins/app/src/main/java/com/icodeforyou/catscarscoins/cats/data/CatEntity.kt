// cats/data/CatEntity.kt
// CatsCarsCoins — spec 24.3.3. Complete file.
package com.icodeforyou.catscarscoins.cats.data

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Fts4
import androidx.room3.PrimaryKey
import com.icodeforyou.catscarscoins.cats.domain.Breed
import com.icodeforyou.catscarscoins.cats.domain.Cat

/**
 * Room row for one cat (spec Phase 3). Flat storage of the nested domain
 * shape; PK is The Cat API's String image id — upsert-by-id (the 24.3.2
 * refresh contract) falls straight out of the primary key.
 *
 * CREATE TABLE equivalent:
 * ```
 * CREATE TABLE cats (
 *     id          TEXT PRIMARY KEY NOT NULL,
 *     image_url   TEXT NOT NULL,
 *     breed_id    TEXT NOT NULL,
 *     breed_name  TEXT NOT NULL,
 *     origin      TEXT NOT NULL,
 *     temperament TEXT NOT NULL,
 *     life_span   TEXT NOT NULL,
 *     description TEXT NOT NULL
 * )
 * ```
 */
@Entity(tableName = "cats")
data class CatEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "image_url")
    val imageUrl: String,

    @ColumnInfo(name = "breed_id")
    val breedId: String,

    @ColumnInfo(name = "breed_name")
    val breedName: String,

    @ColumnInfo(name = "origin")
    val origin: String,

    @ColumnInfo(name = "temperament")
    val temperament: String,

    @ColumnInfo(name = "life_span")
    val lifeSpan: String,

    @ColumnInfo(name = "description")
    val description: String,
)

/**
 * FTS4 shadow of [CatEntity] (spec: Cats search). External-content FTS —
 * Room generates the sync triggers; the index stores no duplicate text.
 * Indexes EXACTLY the four searchable fields fixed at 24.3.1/24.3.2:
 * breed name, origin, temperament, description. Column names must match
 * the content entity's.
 */
@Fts4(contentEntity = CatEntity::class)
@Entity(tableName = "cats_fts")
data class CatFtsEntity(

    @ColumnInfo(name = "breed_name")
    val breedName: String,

    @ColumnInfo(name = "origin")
    val origin: String,

    @ColumnInfo(name = "temperament")
    val temperament: String,

    @ColumnInfo(name = "description")
    val description: String,
)

fun CatEntity.toDomain(): Cat = Cat(
    id = id,
    imageUrl = imageUrl,
    breed = Breed(
        id = breedId,
        name = breedName,
        origin = origin,
        temperament = temperament,
        lifeSpan = lifeSpan,
        description = description,
    ),
)

fun Cat.toEntity(): CatEntity = CatEntity(
    id = id,
    imageUrl = imageUrl,
    breedId = breed.id,
    breedName = breed.name,
    origin = breed.origin,
    temperament = breed.temperament,
    lifeSpan = breed.lifeSpan,
    description = breed.description,
)