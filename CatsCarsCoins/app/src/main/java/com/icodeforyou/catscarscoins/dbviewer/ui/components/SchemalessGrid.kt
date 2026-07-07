// dbviewer/ui/components/SchemalessGrid.kt
// CatsCarsCoins — Complete file.
// Change from 24.5.1: rows now live in a LazyColumn (vertical scroll +
// recycling — the old forEach composed every row and could not scroll),
// the header row stays pinned above it, and headers are tappable sort
// controls: first tap sorts ascending, second tap flips, switching
// column resets to ascending. Sort is numeric-aware (numeric cells
// compare as numbers, everything else case-insensitively as text) and
// resets when the table (headers) changes.
// Change 2: cells never wrap — single line, ellipsized. Tapping a cell
// opens a modal with the column name and full value; tapping outside
// the modal (the scrim) dismisses back to the table.
// Change 3: column widths are header-driven — each column is exactly as
// wide as its header text plus reserved room for the sort mark (so the
// width never jumps when sorting toggles), with a small floor so tiny
// headers like "id" keep usable cells. Headers are never truncated;
// body cells still ellipsize within the column.
// Change 4: width smarts — when the header-driven widths sum to less
// than the screen, every column scales up proportionally so the table
// fills the full width (cats/coins). Wider tables keep exact header
// widths and scroll horizontally (manufacturers). Columns only ever
// grow, so the header never-truncate guarantee holds.
package com.icodeforyou.catscarscoins.dbviewer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.icodeforyou.catscarscoins.dbviewer.data.DbRow

private val CELL_PADDING = 8.dp
private val MIN_COLUMN_WIDTH = 64.dp
private val MODAL_PADDING = 20.dp
private val MODAL_TITLE_SPACING = 8.dp
private val MODAL_MAX_HEIGHT = 420.dp
private const val SORT_ASCENDING_MARK = " \u25B2"
private const val SORT_DESCENDING_MARK = " \u25BC"
private const val EMPTY_CELL_DISPLAY = "(empty)"

@Composable
fun SchemalessGrid(
    headers: List<String>,
    rows: List<DbRow>,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val headerStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)

    // Sort state is presentation-only and resets when the table changes
    // (keyed on headers). null column = the repository's natural order.
    var sortColumn by rememberSaveable(headers) { mutableStateOf<String?>(null) }
    var sortAscending by rememberSaveable(headers) { mutableStateOf(true) }

    // The cell whose full content is being inspected (column to value);
    // null = no modal.
    var inspectedCell by remember { mutableStateOf<Pair<String, String>?>(null) }

    if (headers.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "No table data visible")
        }
        return
    }

    val sortedRows = remember(rows, sortColumn, sortAscending) {
        val column = sortColumn ?: return@remember rows
        val sorted = rows.sortedWith(cellComparator(column))
        if (sortAscending) sorted else sorted.reversed()
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val availableWidth = maxWidth

        // Header-driven column widths: measure each header WITH a sort
        // mark (reserved up front so widths never change on sort), pad
        // both sides, floor at MIN_COLUMN_WIDTH. If the total comes up
        // short of the screen, scale every column up proportionally so
        // the table fills the full width.
        val columnWidths: Map<String, Dp> =
            remember(headers, textMeasurer, density, headerStyle, availableWidth) {
                val naturalWidths = headers.associateWith { header ->
                    val measuredWidth = textMeasurer.measure(
                        text = AnnotatedString(header + SORT_DESCENDING_MARK),
                        style = headerStyle,
                    ).size.width
                    val width = with(density) { measuredWidth.toDp() } + CELL_PADDING * 2
                    if (width < MIN_COLUMN_WIDTH) MIN_COLUMN_WIDTH else width
                }
                val totalWidth = naturalWidths.values.fold(0.dp) { sum, width -> sum + width }
                if (totalWidth > 0.dp && totalWidth < availableWidth) {
                    val scale = availableWidth.value / totalWidth.value
                    naturalWidths.mapValues { (_, width) -> width * scale }
                } else {
                    naturalWidths
                }
            }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState),
        ) {
            // Pinned header row: tap to sort; tap again to flip direction.
            Row {
                headers.forEach { header ->
                    val isSortColumn = header == sortColumn
                    val mark = when {
                        !isSortColumn -> ""
                        sortAscending -> SORT_ASCENDING_MARK
                        else -> SORT_DESCENDING_MARK
                    }
                    Text(
                        text = header + mark,
                        style = headerStyle,
                        maxLines = 1,
                        modifier = Modifier
                            .width(columnWidths.getValue(header))
                            .clickable {
                                if (isSortColumn) {
                                    sortAscending = !sortAscending
                                } else {
                                    sortColumn = header
                                    sortAscending = true
                                }
                            }
                            .padding(CELL_PADDING),
                    )
                }
            }

            // Rows: lazy for vertical scroll and recycling. Cells never wrap;
            // long content ellipsizes, and a tap opens the full value.
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items = sortedRows, key = { row -> row.id }) { row ->
                    Row {
                        headers.forEach { header ->
                            val cellValue = row.cells[header] ?: ""
                            Text(
                                text = cellValue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .width(columnWidths.getValue(header))
                                    .clickable { inspectedCell = header to cellValue }
                                    .padding(CELL_PADDING),
                            )
                        }
                    }
                }
            }
        }
    }

    inspectedCell?.let { (column, value) ->
        CellInspectorModal(
            column = column,
            value = value,
            onDismiss = { inspectedCell = null },
        )
    }
}

/**
 * Full-content view for one cell. Tapping outside the card (the scrim)
 * dismisses — Dialog's default onDismissRequest behavior. Long values
 * scroll inside the card.
 */
@Composable
private fun CellInspectorModal(
    column: String,
    value: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier
                    .heightIn(max = MODAL_MAX_HEIGHT)
                    .verticalScroll(rememberScrollState())
                    .padding(MODAL_PADDING),
            ) {
                Text(
                    text = column,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = MODAL_TITLE_SPACING),
                )
                Text(
                    text = value.ifBlank { EMPTY_CELL_DISPLAY },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

/**
 * Numeric-aware cell comparison: when both cells parse as numbers they
 * compare numerically (so 9 sorts before 100 instead of after "10"),
 * otherwise case-insensitively as text. Missing cells compare as "".
 */
private fun cellComparator(column: String): Comparator<DbRow> =
    Comparator { first, second ->
        val firstValue = first.cells[column].orEmpty()
        val secondValue = second.cells[column].orEmpty()
        val firstNumber = firstValue.toDoubleOrNull()
        val secondNumber = secondValue.toDoubleOrNull()
        if (firstNumber != null && secondNumber != null) {
            firstNumber.compareTo(secondNumber)
        } else {
            firstValue.compareTo(secondValue, ignoreCase = true)
        }
    }