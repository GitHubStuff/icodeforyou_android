// dbviewer/ui/DbViewerScreen.kt
// CatsCarsCoins — spec 24.5.13. Complete file.
// Change from 24.5.8: Added administrative application reset trigger action control.
package com.icodeforyou.catscarscoins.dbviewer.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.icodeforyou.catscarscoins.dbviewer.ui.components.SchemalessGrid
import org.koin.androidx.compose.koinViewModel

/**
 * Main Composable entry point for the Phase 5 Database Viewer feature vertical.
 */
@Composable
fun DbViewerScreen(
    modifier: Modifier = Modifier,
    viewModel: DbViewerViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val tableScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Database Tables",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Horizontal picker selector bar deck
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(tableScrollState)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.tables.forEach { tableName ->
                val isSelected = tableName == uiState.selectedTable
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable {
                            viewModel.selectTable(tableName)
                            Toast.makeText(context, "Inspecting table: $tableName", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tableName,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Action command controls row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {
                viewModel.refreshData()
                Toast.makeText(context, "Forced global data refresh triggered", Toast.LENGTH_SHORT).show()
            }) {
                Text(text = "Forced Refresh")
            }

            Button(
                onClick = {
                    viewModel.resetApplication()
                    Toast.makeText(context, "Application storage wiped clean", Toast.LENGTH_LONG).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(text = "Reset App")
            }

            if (uiState.selectedTable.isNotEmpty()) {
                Text(
                    text = "${uiState.rows.size} rows",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }

        // Dynamic Flat Results Output Display Frame Block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceContainerLow, RoundedCornerShape(8.dp))
                .padding(4.dp)
        ) {
            if (uiState.selectedTable.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select a table above to inspect raw contents",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                SchemalessGrid(
                    headers = uiState.headers,
                    rows = uiState.rows
                )
            }
        }
    }
}