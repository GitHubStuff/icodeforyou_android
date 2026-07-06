// cats/data/RoomCatsRepository.kt
// CatsCarsCoins — spec 24.3.10. Complete file.
package com.icodeforyou.catscarscoins.cats.data

import com.icodeforyou.catscarscoins.cats.domain.Cat
import com.icodeforyou.catscarscoins.cats.domain.CatsRemoteSource
import com.icodeforyou.catscarscoins.cats.domain.CatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val WHITESPACE = Regex("\\s+")

/**
 * Room-backed [CatsRepository]: DAO for storage and FTS, [CatsRemoteSource]
 * for pages (24.3.8 seam — exactly what the 24.3.9 instrumented tests
 * fake). Upsert-by-id is the schema's doing (remote id is the PK); this
 * class adds only the text→MATCH policy.
 */
class RoomCatsRepository(
    private val catDao: CatDao,
    private val remoteSource: CatsRemoteSource,
) : CatsRepository {

    override fun cats(query: String): Flow<List<Cat>> {
        val match = query.toFtsMatch()
        val entities = if (match.isEmpty()) {
            catDao.observeAll()
        } else {
            catDao.observeMatching(match)
        }
        return entities.map { list -> list.map { entity -> entity.toDomain() } }
    }

    override suspend fun refresh() {
        val page = remoteSource.fetchPage(CatsRepository.FETCH_LIMIT)
        catDao.upsertAll(page.map { cat -> cat.toEntity() })
    }

    override suspend fun clearAll() {
        catDao.deleteAll()
    }
}

/**
 * The repository's MATCH policy (pinned by 24.3.9): tokenize on
 * whitespace, sanitize each token to letters and digits — user input must
 * never become FTS syntax — then star every token for prefix matching
 * ("agi" finds Agile) and space-join, which FTS4 treats as AND
 * ("alert agile" requires both). Blank or fully-sanitized-away input
 * yields "" — the caller's blank-means-all branch.
 */
private fun String.toFtsMatch(): String =
    split(WHITESPACE)
        .map { token -> token.filter(Char::isLetterOrDigit) }
        .filter { token -> token.isNotEmpty() }
        .joinToString(" ") { token -> "$token*" }