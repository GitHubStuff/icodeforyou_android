// preferences/ui/SettingsViewModel.kt
// CatsCarsCoins — spec 24.1.21. Complete file.
package com.icodeforyou.catscarscoins.preferences.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.icodeforyou.catscarscoins.preferences.domain.AppPreferences
import com.icodeforyou.catscarscoins.preferences.domain.PreferencesRepository
import com.icodeforyou.catscarscoins.preferences.domain.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Upstream collection survives a config change (rotation re-subscribes
 * within the window) but stops when nothing observes for this long.
 */
private const val SUBSCRIPTION_STOP_TIMEOUT_MS = 5_000L

/**
 * Write path for the four persisted preferences (spec: theme, polling
 * 5–30 s, pause, haptics). Exposes the full [AppPreferences] as state —
 * the Settings screen draws all four controls from one object — and four
 * intent-named actions that delegate to the repository. No logic lives
 * here: clamping belongs to the data layer per the 24.1.2 contract, and
 * the tests (24.1.20) verify it arrives clamped anyway.
 */
class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<AppPreferences> = preferencesRepository.preferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_STOP_TIMEOUT_MS),
            initialValue = AppPreferences.DEFAULTS,
        )

    fun onThemeModeSelected(themeMode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(themeMode)
        }
    }

    fun onPollingIntervalChanged(seconds: Int) {
        viewModelScope.launch {
            preferencesRepository.setPollingIntervalSeconds(seconds)
        }
    }

    fun onPollingPausedChanged(paused: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPollingPaused(paused)
        }
    }

    fun onHapticsEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHapticsEnabled(enabled)
        }
    }
}