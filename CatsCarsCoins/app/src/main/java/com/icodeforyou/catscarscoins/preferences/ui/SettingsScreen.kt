// preferences/ui/SettingsScreen.kt
// CatsCarsCoins — spec 24.5.14. Complete file.
// Change from 24.1.23: full revamp — SETTINGS header at the top of the
// safe area, card-per-setting scrolling list (Theme, Polling interval,
// Polling On, Haptics On, Database Tools, Nuke Database). "Polling On"
// is a presentation inversion of the persisted pollingPaused (no data
// change). Database Tools navigates to the DbViewer. Nuke Database is
// one tap, destructive-styled, NO confirmation dialog — by decree.
// Nuke plays res/raw/db_wipe.ogg via rememberSoundEffect (SoundPool).
package com.icodeforyou.catscarscoins.preferences.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.icodeforyou.catscarscoins.R
import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.ui.components.rememberSoundEffect
import com.icodeforyou.catscarscoins.ui.icons.MushroomCloud
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

private val SCREEN_PADDING = 16.dp
private val CARD_SPACING = 12.dp
private val CARD_PADDING = 16.dp
private val HEADER_BOTTOM_SPACING = 12.dp

/** Nuke card in LIGHT mode: blood red, white content. Dark mode keeps
 *  the theme's errorContainer, which already reads as deep red. */
private val NUKE_BLOOD_RED = Color(0xFF8A0303)

/**
 * Forces the slider to snap to 5-second increments.
 */
private const val POLLING_STEP_SIZE = 5

/**
 * Slider detents between the bounds: calculated to step by 5 seconds,
 * excluding the two endpoints (Compose's steps semantics).
 */
private val POLLING_SLIDER_STEPS =
    ((AppPreferences.POLLING_INTERVAL_MAX_SECONDS - AppPreferences.POLLING_INTERVAL_MIN_SECONDS) / POLLING_STEP_SIZE) - 1

/**
 * Settings destination (revamped). The screen is the flow observer,
 * collecting one [AppPreferences] state and dispatching the ViewModel
 * actions. [onDatabaseToolsSelected] is the nav seam — MainActivity
 * pushes DbViewerKey. Stateless content below the ViewModel seam.
 */
@Composable
fun SettingsScreen(
    onDatabaseToolsSelected: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val playWipeSound = rememberSoundEffect(R.raw.db_wipe)

    SettingsContent(
        preferences = preferences,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onPollingIntervalChanged = viewModel::onPollingIntervalChanged,
        onPollingPausedChanged = viewModel::onPollingPausedChanged,
        onHapticsEnabledChanged = viewModel::onHapticsEnabledChanged,
        onDatabaseToolsSelected = onDatabaseToolsSelected,
        onNukeDatabase = {
            playWipeSound()
            viewModel.onNukeDatabase()
        },
    )
}

@Composable
private fun SettingsContent(
    preferences: AppPreferences,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onPollingIntervalChanged: (Int) -> Unit,
    onPollingPausedChanged: (Boolean) -> Unit,
    onHapticsEnabledChanged: (Boolean) -> Unit,
    onDatabaseToolsSelected: () -> Unit,
    onNukeDatabase: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SCREEN_PADDING),
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = HEADER_BOTTOM_SPACING),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(CARD_SPACING),
        ) {
            item {
                SettingsCard {
                    ThemeModeSelector(
                        selected = preferences.themeMode,
                        onSelected = onThemeModeSelected,
                    )
                }
            }
            item {
                SettingsCard {
                    PollingIntervalSlider(
                        intervalSeconds = preferences.pollingIntervalSeconds,
                        onIntervalChanged = onPollingIntervalChanged,
                    )
                }
            }
            item {
                SettingsCard {
                    // Presentation inversion: the persisted value is
                    // pollingPaused; the card reads "Polling On".
                    LabeledSwitch(
                        label = "Polling On",
                        checked = !preferences.pollingPaused,
                        useHaptics = preferences.hapticsEnabled,
                        onCheckedChange = { on -> onPollingPausedChanged(!on) },
                    )
                }
            }
            item {
                SettingsCard {
                    LabeledSwitch(
                        label = "Haptics On",
                        checked = preferences.hapticsEnabled,
                        useHaptics = preferences.hapticsEnabled,
                        onCheckedChange = onHapticsEnabledChanged,
                    )
                }
            }
            item {
                DatabaseToolsCard(onClick = onDatabaseToolsSelected)
            }
            item {
                NukeDatabaseCard(
                    useHaptics = preferences.hapticsEnabled,
                    onNuke = onNukeDatabase,
                )
            }
        }
    }
}

/** Shared card chrome: full width, padded interior column. */
@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING),
        ) {
            content()
        }
    }
}

/** Navigation card: pushes the DbViewer. */
@Composable
private fun DatabaseToolsCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Database Tools",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Inspect tables and raw rows",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open Database Tools",
            )
        }
    }
}

/**
 * Destructive card. One tap wipes — no confirmation dialog, by decree.
 * Buzz decision uses the haptics value at tap time (§17 semantics).
 */
@Composable
private fun NukeDatabaseCard(
    useHaptics: Boolean,
    onNuke: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    // Theme detection via the resolved scheme (the app theme can override
    // the system setting, so isSystemInDarkTheme() would be wrong here).
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val containerColor =
        if (isLightTheme) NUKE_BLOOD_RED else MaterialTheme.colorScheme.errorContainer
    val contentColor =
        if (isLightTheme) Color.White else MaterialTheme.colorScheme.onErrorContainer

    Card(
        onClick = {
            if (useHaptics) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            onNuke()
        },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CARD_PADDING),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nuke Database",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Wipes every table instantly. No confirmation.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Icon(
                imageVector = MushroomCloud,
                contentDescription = "Nuke Database",
            )
        }
    }
}

@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelected: (ThemeMode) -> Unit,
) {
    Column {
        Text(text = "Theme", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ThemeMode.entries.forEachIndexed { index, themeMode ->
                SegmentedButton(
                    selected = themeMode == selected,
                    onClick = { onSelected(themeMode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemeMode.entries.size,
                    ),
                ) {
                    Text(text = themeMode.label())
                }
            }
        }
    }
}

@Composable
private fun PollingIntervalSlider(
    intervalSeconds: Int,
    onIntervalChanged: (Int) -> Unit,
) {
    var sliderPosition by remember(intervalSeconds) {
        mutableFloatStateOf(intervalSeconds.toFloat())
    }

    Column {
        Text(
            text = "Polling interval: ${sliderPosition.roundToInt()} s",
            style = MaterialTheme.typography.titleMedium,
        )
        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = {
                onIntervalChanged(sliderPosition.roundToInt())
            },
            valueRange = AppPreferences.POLLING_INTERVAL_MIN_SECONDS.toFloat()..
                    AppPreferences.POLLING_INTERVAL_MAX_SECONDS.toFloat(),
            steps = POLLING_SLIDER_STEPS,
        )
    }
}

/**
 * Switch row. Per spec §17 semantics, the buzz decision uses the haptics
 * value at tap time — so turning haptics OFF buzzes (it was on when
 * tapped) and turning it ON from off does not.
 */
@Composable
private fun LabeledSwitch(
    label: String,
    checked: Boolean,
    useHaptics: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        Switch(
            checked = checked,
            onCheckedChange = { nowChecked ->
                if (useHaptics) {
                    haptics.performHapticFeedback(
                        if (nowChecked) {
                            HapticFeedbackType.ToggleOn
                        } else {
                            HapticFeedbackType.ToggleOff
                        },
                    )
                }
                onCheckedChange(nowChecked)
            },
        )
    }
}

private fun ThemeMode.label(): String = when (this) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "System"
}