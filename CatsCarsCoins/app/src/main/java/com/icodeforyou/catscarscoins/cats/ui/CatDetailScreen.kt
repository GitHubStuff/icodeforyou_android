// cats/ui/CatDetailScreen.kt
// CatsCarsCoins — spec 24.3.31 + back affordance. Complete file.
// Change: explicit on-screen back arrow (system back — edge swipe or the
// 3-button triangle — always worked via NavDisplay onBack, but nothing on
// screen said so). [onBack] raises to the shell, which pops the stack.
package com.icodeforyou.catscarscoins.cats.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.icodeforyou.catscarscoins.cats.domain.Cat
import com.icodeforyou.catscarscoins.preferences.hapticsEnabled
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

private val SCREEN_PADDING = 16.dp
private val SECTION_SPACING = 12.dp
private val IMAGE_CORNER = 12.dp

/**
 * Cat detail (Phase 3 UI). Resolves its ViewModel with the [catId] as a
 * Koin parameter — the app's first parameterized koinViewModel. Renders
 * the cached breed facts (no fetch); a null cat (stale id) shows a
 * not-found message. Stateless below the seam.
 */
@Composable
fun CatDetailScreen(
    catId: String,
    onBack: () -> Unit,
    viewModel: CatDetailViewModel = koinViewModel { parametersOf(catId) },
) {
    val cat by viewModel.cat.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        BackHeader(onBack = onBack)

        when (val current = cat) {
            null -> NotFound()
            else -> CatDetailContent(cat = current)
        }
    }
}

/**
 * On-screen back affordance; system back (edge swipe / ◁) also works but
 * does not buzz — only our own tap handler can. Haptics follow the §17
 * AppButton semantics exactly: the preference is read at tap time via the
 * hapticsEnabled() snapshot accessor, and the buzz type is Confirm.
 */
@Composable
private fun BackHeader(onBack: () -> Unit) {
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = {
                if (hapticsEnabled()) {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                }
                onBack()
            },
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
            )
        }
    }
}

@Composable
private fun NotFound() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SCREEN_PADDING),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Cat not found", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "It may have been cleared. Go back and pick another.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun CatDetailContent(cat: Cat) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        AsyncImage(
            model = cat.imageUrl,
            contentDescription = cat.breed.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(IMAGE_CORNER)),
            contentScale = ContentScale.Crop,
        )

        Text(text = cat.breed.name, style = MaterialTheme.typography.headlineSmall)

        Fact(label = "Origin", value = cat.breed.origin)
        Fact(label = "Life span", value = "${cat.breed.lifeSpan} years")
        Fact(label = "Temperament", value = cat.breed.temperament)

        Text(text = "About", style = MaterialTheme.typography.titleMedium)
        Text(text = cat.breed.description, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}