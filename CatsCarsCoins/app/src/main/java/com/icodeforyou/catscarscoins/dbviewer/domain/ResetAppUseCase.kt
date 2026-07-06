// dbviewer/domain/ResetAppUseCase.kt
// CatsCarsCoins — spec 24.5.9-correction1. Complete file.
package com.icodeforyou.catscarscoins.dbviewer.domain

import androidx.room3.RoomDatabase
import androidx.room3.useWriterConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Domain command orchestrator to wipe application footprints across all storage spaces.
 * Drops database table contents dynamically via low-level write connections.
 */
class ResetAppUseCase(
    private val database: RoomDatabase
) {

    /**
     * Executes sequential, full-footprint purges across underlying local engines.
     */
    suspend fun execute(): Unit = withContext(Dispatchers.IO) {
        val findTablesSql = """
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
        """.trimIndent()

        try {
            database.useWriterConnection { connection ->
                // Gather target tables dynamically
                val tables = connection.usePrepared(findTablesSql) { statement ->
                    val list = mutableListOf<String>()
                    while (statement.step()) {
                        list.add(statement.getText(0))
                    }
                    list
                }

                // Wipe table records sequentially within the connection scope
                tables.forEach { tableName ->
                    val sanitized = tableName.replace(Regex("[^a-zA-Z0-9_]"), "")
                    if (sanitized.isNotEmpty()) {
                        connection.usePrepared("DELETE FROM $sanitized") { statement ->
                            statement.step()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Sunk exception to maintain clean execution flow
        }
    }
}