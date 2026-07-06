// cats/data/CatDao.kt
// CatsCarsCoins — spec 24.3.4. Complete file.
package com.icodeforyou.catscarscoins.cats.data

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Room 3 DAO for the cats table and its FTS4 shadow (spec Phase 3).
 * Both observations share one deterministic ordering — breed name
 * alphabetical, id tie-break — so the full list and a search result can
 * never disagree about order.
 *
 * [observeMatching] takes a raw FTS MATCH expression: translating a
 * user's search text into FTS syntax (prefix stars, quoting) is
 * repository policy, not SQL's job.
 */
@Dao
interface CatDao {

    @Query("SELECT * FROM cats ORDER BY breed_name ASC, id ASC")
    fun observeAll(): Flow<List<CatEntity>>

    @Query(
        """
        SELECT cats.* FROM cats
        JOIN cats_fts ON cats.rowid = cats_fts.rowid
        WHERE cats_fts MATCH :match
        ORDER BY breed_name ASC, id ASC
        """,
    )
    fun observeMatching(match: String): Flow<List<CatEntity>>

    /**
     * Insert-or-update by primary key (the remote id) — the 24.3.2
     * upsert contract as a single Room 3 annotation.
     */
    @Upsert
    suspend fun upsertAll(entities: List<CatEntity>)

    /** Reset App hook (spec §16). The FTS shadow follows via triggers. */
    @Query("DELETE FROM cats")
    suspend fun deleteAll()
}