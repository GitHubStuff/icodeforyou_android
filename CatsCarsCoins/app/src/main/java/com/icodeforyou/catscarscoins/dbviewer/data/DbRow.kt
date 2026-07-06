// dbviewer/data/DbRow.kt
// CatsCarsCoins — spec 24.5.1. Complete file.
package com.icodeforyou.catscarscoins.dbviewer.data

/**
 * Representation of a schemaless database row payload.
 * Maps column headers to row values dynamically as raw strings for flat-grid rendering.
 */
data class DbRow(
    val id: String,
    val cells: Map<String, String>
)