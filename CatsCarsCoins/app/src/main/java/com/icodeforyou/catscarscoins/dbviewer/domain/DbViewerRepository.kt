// dbviewer/domain/DbViewerRepository.kt
// CatsCarsCoins — spec 24.5.2. Complete file.
package com.icodeforyou.catscarscoins.dbviewer.domain

import com.icodeforyou.catscarscoins.dbviewer.data.DbRow
import kotlinx.coroutines.flow.Flow

/**
 * Domain port for extracting schemaless structural data directly from SQLite tables.
 * Decouples presentation blocks from Room's structured compile-time data objects.
 */
interface DbViewerRepository {

    /**
     * Emits the current list of human-readable physical database table names,
     * filtering out framework shadow structures (e.g., room trackers, fts backings).
     */
    fun tableNames(): Flow<List<String>>

    /**
     * Executes a raw snapshot dump of [tableName], returning its column header array
     * paired with a map-backed list of row items.
     */
    suspend fun queryTable(tableName: String): Pair<List<String>, List<DbRow>>
}