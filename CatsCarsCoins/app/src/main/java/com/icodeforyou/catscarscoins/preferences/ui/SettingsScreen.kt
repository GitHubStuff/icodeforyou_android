// preferences/ui/SettingsScreen.kt
// CatsCarsCoins — spec 24.1.23. Complete file.
package com.icodeforyou.catscarscoins.preferences.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

private val SCREEN_PADDING = 16.dp
private val SECTION_SPACING = 24.dp

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
 * Settings destination (Phase 1 write path). The screen is the flow
 * observer (spec §17: it draws the settings), collecting one
 * [AppPreferences] state and dispatching the four SettingsViewModel
 * actions. Stateless content below the ViewModel seam.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()

    SettingsContent(
        preferences = preferences,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onPollingIntervalChanged = viewModel::onPollingIntervalChanged,
        onPollingPausedChanged = viewModel::onPollingPausedChanged,
        onHapticsEnabledChanged = viewModel::onHapticsEnabledChanged,
    )
}

@Composable
private fun SettingsContent(
    preferences: AppPreferences,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onPollingIntervalChanged: (Int) -> Unit,
    onPollingPausedChanged: (Boolean) -> Unit,
    onHapticsEnabledChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(SECTION_SPACING),
    ) {
        ThemeModeSelector(
            selected = preferences.themeMode,
            onSelected = onThemeModeSelected,
        )
        PollingIntervalSlider(
            intervalSeconds = preferences.pollingIntervalSeconds,
            onIntervalChanged = onPollingIntervalChanged,
        )
        LabeledSwitch(
            label = "Pause polling",
            checked = preferences.pollingPaused,
            useHaptics = preferences.hapticsEnabled,
            onCheckedChange = onPollingPausedChanged,
        )
        LabeledSwitch(
            label = "Haptics",
            checked = preferences.hapticsEnabled,
            useHaptics = preferences.hapticsEnabled,
            onCheckedChange = onHapticsEnabledChanged,
        )
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