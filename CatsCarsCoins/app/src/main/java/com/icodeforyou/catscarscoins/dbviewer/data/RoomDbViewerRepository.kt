// dbviewer/data/RoomDbViewerRepository.kt
// CatsCarsCoins — spec 24.5.3-correction2. Complete file.
package com.icodeforyou.catscarscoins.dbviewer.data

import androidx.room3.RoomDatabase
import androidx.room3.useReaderConnection
import androidx.sqlite.SQLiteStatement
import com.icodeforyou.catscarscoins.dbviewer.data.DbRow
import com.icodeforyou.catscarscoins.dbviewer.domain.DbViewerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Concrete implementation of [DbViewerRepository] leveraging Room 3 driver APIs
 * to read columns and table names dynamically without pre-compiled maps.
 */
@Suppress("unused", "UNUSED_PARAMETER")
class RoomDbViewerRepository(
    private val database: RoomDatabase
) : DbViewerRepository {

    override fun tableNames(): Flow<List<String>> = flow {
        val sql = """
            SELECT name FROM sqlite_master 
            WHERE type='table' 
              AND name NOT LIKE 'sqlite_%' 
              AND name NOT LIKE 'room_%'
              AND name NOT LIKE '%_fts%'
              AND name NOT LIKE '%_percent%'
              AND name NOT LIKE '%_segdir%'
              AND name NOT LIKE '%_segments%'
              AND name NOT LIKE '%_docsize%'
              AND name NOT LIKE '%_stat%'
            ORDER BY name ASC
        """.trimIndent()

        val tables = try {
            database.useReaderConnection { connection ->
                connection.usePrepared(sql) { statement ->
                    val list = mutableListOf<String>()
                    while (statement.step()) {
                        list.add(statement.getText(0))
                    }
                    list
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
        emit(tables)
    }.flowOn(Dispatchers.IO)

    override suspend fun queryTable(tableName: String): Pair<List<String>, List<DbRow>> = withContext(Dispatchers.IO) {
        val sanitizedTableName = tableName.replace(Regex("[^a-zA-Z0-9_]"), "")
        if (sanitizedTableName.isEmpty()) return@withContext emptyList<String>() to emptyList()

        val sql = "SELECT * FROM $sanitizedTableName"

        try {
            database.useReaderConnection { connection ->
                connection.usePrepared(sql) { statement ->
                    val headers = mutableListOf<String>()
                    val rows = mutableListOf<DbRow>()

                    val columnCount = statement.getColumnCount()
                    for (i in 0 until columnCount) {
                        headers.add(statement.getColumnName(i))
                    }

                    var rowIdCounter = 0
                    while (statement.step()) {
                        val cells = mutableMapOf<String, String>()
                        for (i in 0 until columnCount) {
                            val columnName = headers[i]
                            cells[columnName] = getCellText(statement, i)
                        }
                        rows.add(DbRow(id = "${sanitizedTableName}_${rowIdCounter++}", cells = cells))
                    }
                    headers to rows
                }
            }
        } catch (e: Exception) {
            emptyList<String>() to emptyList()
        }
    }

    private fun getCellText(statement: SQLiteStatement, index: Int): String {
        return try {
            statement.getText(index)
        } catch (e: Exception) {
            try {
                statement.getLong(index).toString()
            } catch (ex: Exception) {
                ""
            }
        }
    }
}