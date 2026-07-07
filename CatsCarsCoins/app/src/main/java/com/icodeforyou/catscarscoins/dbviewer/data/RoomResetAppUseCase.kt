// dbviewer/data/RoomResetAppUseCase.kt
// CatsCarsCoins — spec 24.5.14. Complete file.
// Relocation of the 24.5.9-correction1 body from dbviewer/domain into the
// data layer, now implementing the ResetAppUseCase contract. One safety
// fix folded in: CancellationException is rethrown, never swallowed.
// Behavior otherwise identical; the §16 rewrite (repositories composed,
// preferences reset, Room invalidation, failure signaling) is queued.
package com.icodeforyou.catscarscoins.dbviewer.data

import androidx.room3.RoomDatabase
import androidx.room3.useWriterConnection
import com.icodeforyou.catscarscoins.dbviewer.domain.ResetAppUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [ResetAppUseCase] over raw Room 3 writer connections: discovers user
 * tables from sqlite_master (framework and FTS shadow tables excluded)
 * and deletes their contents sequentially.
 *
 * Known limitations, addressed by the queued rewrite: bypasses Room's
 * invalidation tracker (observing screens go stale until the next
 * write), does not reset preferences, and swallows non-cancellation
 * failures silently.
 */
class RoomResetAppUseCase(
    private val database: RoomDatabase,
) : ResetAppUseCase {

    override suspend fun execute(): Unit = withContext(Dispatchers.IO) {
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
                val tables = connection.usePrepared(findTablesSql) { statement ->
                    val list = mutableListOf<String>()
                    while (statement.step()) {
                        list.add(statement.getText(0))
                    }
                    list
                }

                tables.forEach { tableName ->
                    val sanitized = tableName.replace(Regex("[^a-zA-Z0-9_]"), "")
                    if (sanitized.isNotEmpty()) {
                        connection.usePrepared("DELETE FROM $sanitized") { statement ->
                            statement.step()
                        }
                    }
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // Failure signaling lands with the queued §16 rewrite.
        }
    }
}