// cars/ui/CarsScreen.kt
// CatsCarsCoins — spec 24.4.17. Complete file.
package com.icodeforyou.catscarscoins.cars.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.icodeforyou.catscarscoins.cars.domain.Manufacturer
import com.icodeforyou.catscarscoins.notifier.Notifier
import com.icodeforyou.catscarscoins.notifier.READ_ONLY_TOAST_MILLIS
import com.icodeforyou.catscarscoins.preferences.buttonFontSize
import com.icodeforyou.catscarscoins.preferences.hapticsEnabled
import com.icodeforyou.catscarscoins.ui.components.AppButton
import com.icodeforyou.catscarscoins.ui.components.SwipeableRefreshableList
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private val SCREEN_PADDING = 16.dp
private val ROW_SPACING = 12.dp
private val ROW_VERTICAL_PADDING = 8.dp

/**
 * Cars destination (Phase 4 UI): debounced FTS search over manufacturer
 * rows, pull-to-refresh via the shared SwipeableRefreshableList, refresh
 * failures surfaced as a brief toast (750 ms). Parallel to CatsScreen,
 * two deliberate deltas: rows are text-only (vPIC ships no images) and
 * carry no tap action (manufacturer detail is spec-optional, skipped).
 * Stateless below the ViewModel seam.
 */
@Composable
fun CarsScreen(
    viewModel: CarsViewModel = koinViewModel(),
    notifier: Notifier = koinInject(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val manufacturers by viewModel.manufacturers.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshFailures.collect {
            notifier.show(durationMillis = READ_ONLY_TOAST_MILLIS) {
                Text(text = "Refresh failed — check your connection")
            }
        }
    }

    CarsContent(
        query = query,
        manufacturers = manufacturers,
        isRefreshing = isRefreshing,
        onQueryChanged = viewModel::onQueryChanged,
        onRefresh = viewModel::onRefresh,
    )
}

@Composable
private fun CarsContent(
    query: String,
    manufacturers: List<Manufacturer>,
    isRefreshing: Boolean,
    onQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SCREEN_PADDING),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ROW_SPACING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                label = { Text(text = "Search manufacturers") },
                singleLine = true,
            )
            AppButton(
                text = "Refresh",
                onClick = onRefresh,
                useHaptics = hapticsEnabled(),
                fontSize = buttonFontSize(),
            )
        }

        SwipeableRefreshableList(
            items = manufacturers,
            key = { manufacturer -> manufacturer.id },
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            emptyContent = { CarsEmptyState(query = query) },
            modifier = Modifier.weight(1f),
        ) { manufacturer ->
            ManufacturerRow(manufacturer = manufacturer)
            HorizontalDivider()
        }
    }
}

@Composable
private fun CarsEmptyState(query: String) {
    val (title, detail) = if (query.isBlank()) {
        "No manufacturers yet" to "Tap Refresh to fetch a page from NHTSA vPIC."
    } else {
        "No matches" to "No manufacturer matches \"$query\"."
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = detail, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ManufacturerRow(manufacturer: Manufacturer) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = ROW_VERTICAL_PADDING),
    ) {
        Text(
            text = manufacturer.name,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = manufacturer.country,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}