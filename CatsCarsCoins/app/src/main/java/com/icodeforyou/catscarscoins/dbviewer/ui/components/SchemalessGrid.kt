// dbviewer/ui/components/SchemalessGrid.kt
// CatsCarsCoins — spec 24.5.1. Complete file.
package com.icodeforyou.catscarscoins.dbviewer.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.icodeforyou.catscarscoins.dbviewer.data.DbRow

@Composable
fun SchemalessGrid(
    headers: List<String>,
    rows: List<DbRow>,
    modifier: Modifier = Modifier,
    columnWidthDp: Int = 120
) {
    val horizontalScrollState = rememberScrollState()

    if (headers.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text(text = "No table data visible")
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
    ) {
        // Table Header
        Row {
            headers.forEach { header ->
                Text(
                    text = header,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .width(columnWidthDp.dp)
                        .padding(8.dp)
                )
            }
        }

        // Table Rows
        rows.forEach { row ->
            Row {
                headers.forEach { header ->
                    val cellValue = row.cells[header] ?: ""
                    Text(
                        text = cellValue,
                        modifier = Modifier
                            .width(columnWidthDp.dp)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}