// cats/ui/CatsScreen.kt
// CatsCarsCoins — spec 24.3.38. Complete file.
// Change from 24.3.38: refresh (button and pull) dismisses the soft
// keyboard; the empty state gains a missing-API-key branch (blank
// BuildConfig.CAT_API_KEY silently yields breedless public-tier rows that
// the mapper drops — the screen now says so instead of looking broken).
package com.icodeforyou.catscarscoins.cats.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.icodeforyou.catscarscoins.BuildConfig
import com.icodeforyou.catscarscoins.cats.domain.Cat
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
private val CAT_IMAGE_SIZE = 64.dp
private val CAT_IMAGE_CORNER = 8.dp

/** Cats spinner: 1.5x the Material default, purple. */
private const val CAT_SPINNER_SCALE = 1.5f
private val CAT_SPINNER_COLOR = Color(0xFF9C27B0)

/**
 * Cats destination (Phase 3 UI): debounced FTS search over Coil-rendered
 * rows, pull-to-refresh via the shared SwipeableRefreshableList, refresh
 * failures surfaced as a brief toast (750 ms). Tapping a row raises
 * [onCatSelected]; the shell pushes the detail key. Stateless below the
 * ViewModel seam.
 */
@Composable
fun CatsScreen(
    onCatSelected: (String) -> Unit,
    viewModel: CatsViewModel = koinViewModel(),
    notifier: Notifier = koinInject(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val cats by viewModel.cats.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.refreshFailures.collect {
            notifier.show(durationMillis = READ_ONLY_TOAST_MILLIS) {
                Text(text = "Refresh failed — check your connection")
            }
        }
    }

    CatsContent(
        query = query,
        cats = cats,
        isRefreshing = isRefreshing,
        onQueryChanged = viewModel::onQueryChanged,
        // Refresh dismisses the keyboard — covers both the button and
        // pull-to-refresh, which share this lambda.
        onRefresh = {
            keyboard?.hide()
            viewModel.onRefresh()
        },
        onCatSelected = onCatSelected,
    )
}

@Composable
private fun CatsContent(
    query: String,
    cats: List<Cat>,
    isRefreshing: Boolean,
    onQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onCatSelected: (String) -> Unit,
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
                label = { Text(text = "Search breeds") },
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
            items = cats,
            key = { cat -> cat.id },
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            emptyContent = { CatsEmptyState(query = query) },
            modifier = Modifier.weight(1f),
            indicatorScale = CAT_SPINNER_SCALE,
            indicatorColor = CAT_SPINNER_COLOR,
        ) { cat ->
            CatRow(cat = cat, onClick = { onCatSelected(cat.id) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun CatsEmptyState(query: String) {
    val (title, detail) = when {
        // Keyless requests hit The Cat API's public tier, whose rows lack
        // breed data and are dropped by the mapper — the list stays empty
        // with no error. Say so instead of looking broken.
        BuildConfig.CAT_API_KEY.isBlank() ->
            "Cat API key missing" to
                    "Add CAT_API_KEY=your_key (no quotes) to local.properties and rebuild."
        query.isBlank() ->
            "No cats yet" to "Tap Refresh to fetch a page from The Cat API."
        else ->
            "No matches" to "No breed matches \"$query\"."
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = detail, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CatRow(cat: Cat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = ROW_VERTICAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(ROW_SPACING),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = cat.imageUrl,
            contentDescription = cat.breed.name,
            modifier = Modifier
                .size(CAT_IMAGE_SIZE)
                .clip(RoundedCornerShape(CAT_IMAGE_CORNER)),
            contentScale = ContentScale.Crop,
        )
        Column {
            Text(
                text = cat.breed.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = cat.breed.temperament,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}