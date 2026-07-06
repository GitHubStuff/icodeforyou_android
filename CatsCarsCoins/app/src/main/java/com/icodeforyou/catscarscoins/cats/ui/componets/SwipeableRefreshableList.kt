// ui/components/SwipeableRefreshableList.kt
// CatsCarsCoins — spec 24.3.35. Complete file.
package com.icodeforyou.catscarscoins.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * The app's one list surface (spec Phase 3 capstone): a keyed vertical
 * list with pull-to-refresh and an empty slot, generic over the item type.
 * Cats and Coins both hand-rolled this triad (LazyColumn + empty branch +
 * refresh) before extraction; Cars (Phase 4) reuses it.
 *
 * Deliberately unopinionated:
 * - [itemContent] owns each row's layout AND its own separators — the
 *   component bakes in no divider policy.
 * - [emptyContent] is a caller slot, so a screen with several empty states
 *   (Cats: no-data vs no-match) picks the right one before calling.
 * - Empty content is rendered as a single fill-viewport item inside the
 *   LazyColumn, so the pull-to-refresh gesture works even with no rows.
 *
 * @param items current list (already ordered/filtered by the caller).
 * @param key stable identity per item (survives inserts and trims).
 * @param isRefreshing drives the indicator; the caller flips it around
 *        [onRefresh].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeableRefreshableList(
    items: List<T>,
    key: (T) -> Any,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    emptyContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (items.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        emptyContent()
                    }
                }
            } else {
                items(items = items, key = key) { element ->
                    itemContent(element)
                }
            }
        }
    }
}