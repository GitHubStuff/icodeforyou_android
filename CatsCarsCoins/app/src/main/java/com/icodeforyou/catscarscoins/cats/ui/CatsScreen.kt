// cats/ui/CatsScreen.kt
// CatsCarsCoins — spec 24.3.21. Complete file.
package com.icodeforyou.catscarscoins.cats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.icodeforyou.catscarscoins.cats.domain.Cat
import com.icodeforyou.catscarscoins.notifier.Notifier
import com.icodeforyou.catscarscoins.notifier.READ_ONLY_TOAST_MILLIS
import com.icodeforyou.catscarscoins.preferences.buttonFontSize
import com.icodeforyou.catscarscoins.preferences.hapticsEnabled
import com.icodeforyou.catscarscoins.ui.components.AppButton
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private val SCREEN_PADDING = 16.dp
private val ROW_SPACING = 12.dp
private val ROW_VERTICAL_PADDING = 8.dp
private val CAT_IMAGE_SIZE = 64.dp
private val CAT_IMAGE_CORNER = 8.dp

/**
 * Cats destination (Phase 3 UI): debounced FTS search over the stored
 * cats, Coil-rendered breed images, refresh with failure surfaced as a
 * brief informational toast (750 ms — the Notifier's short duration).
 * Stateless below the ViewModel seam, same shape as Coins and Settings.
 */
@Composable
fun CatsScreen(
    viewModel: CatsViewModel = koinViewModel(),
    notifier: Notifier = koinInject(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val cats by viewModel.cats.collectAsStateWithLifecycle()

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
        onQueryChanged = viewModel::onQueryChanged,
        onRefresh = viewModel::onRefresh,
    )
}

@Composable
private fun CatsContent(
    query: String,
    cats: List<Cat>,
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

        when {
            cats.isEmpty() && query.isBlank() -> EmptyState(
                title = "No cats yet",
                detail = "Tap Refresh to fetch a page from The Cat API.",
            )
            cats.isEmpty() -> EmptyState(
                title = "No matches",
                detail = "No breed matches \"$query\".",
            )
            else -> CatList(cats = cats)
        }
    }
}

@Composable
private fun EmptyState(title: String, detail: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = detail, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CatList(cats: List<Cat>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = cats, key = { cat -> cat.id }) { cat ->
            CatRow(cat = cat)
            HorizontalDivider()
        }
    }
}

@Composable
private fun CatRow(cat: Cat) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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