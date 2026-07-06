// cars/data/RoomCarsRepository.kt
// CatsCarsCoins — spec 24.4.9. Complete file.
package com.icodeforyou.catscarscoins.cars.data

import com.icodeforyou.catscarscoins.cars.domain.CarsRemoteSource
import com.icodeforyou.catscarscoins.cars.domain.CarsRepository
import com.icodeforyou.catscarscoins.cars.domain.Manufacturer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val WHITESPACE = Regex("\\s+")

/**
 * Room-backed [CarsRepository]: DAO for storage and FTS,
 * [CarsRemoteSource] for pages (24.4.7 seam — exactly what the 24.4.8
 * instrumented tests fake). Upsert-by-id is the schema's doing (Mfr_ID is
 * the PK); this class adds only the text→MATCH policy. Parallel to
 * RoomCatsRepository.
 */
class RoomCarsRepository(
    private val carDao: CarDao,
    private val remoteSource: CarsRemoteSource,
) : CarsRepository {

    override fun manufacturers(query: String): Flow<List<Manufacturer>> {
        val match = query.toFtsMatch()
        val entities = if (match.isEmpty()) {
            carDao.observeAll()
        } else {
            carDao.observeMatching(match)
        }
        return entities.map { list -> list.map { entity -> entity.toDomain() } }
    }

    override suspend fun refresh() {
        val page = remoteSource.fetchPage(CarsRepository.FETCH_PAGE)
        carDao.upsertAll(page.map { manufacturer -> manufacturer.toEntity() })
    }

    override suspend fun clearAll() {
        carDao.deleteAll()
    }
}

/**
 * The repository's MATCH policy (pinned by 24.4.8), identical in shape to
 * the cats repository's: tokenize on whitespace, sanitize each token to
 * letters and digits — user input must never become FTS syntax — then
 * star every token for prefix matching ("tes" finds Tesla) and space-join,
 * which FTS4 treats as AND ("united kingdom" requires both). Blank or
 * fully-sanitized-away input yields "" — the caller's blank-means-all
 * branch.
 */
private fun String.toFtsMatch(): String =
    split(WHITESPACE)
        .map { token -> token.filter(Char::isLetterOrDigit) }
        .filter { token -> token.isNotEmpty() }
        .joinToString(" ") { token -> "$token*" }